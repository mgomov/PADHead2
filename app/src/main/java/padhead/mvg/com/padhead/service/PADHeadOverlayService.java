package padhead.mvg.com.padhead.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import padhead.mvg.com.padhead.R;
import padhead.mvg.com.padhead.overlay.OverlayService;
import padhead.mvg.com.padhead.overlay.OverlayTouch;
import padhead.mvg.com.padhead.overlay.OverlayTouchHidden;
import padhead.mvg.com.padhead.overlay.OverlayTouchOrbSetup;
import padhead.mvg.com.padhead.overlay.OverlayTouchSolutions;
import padhead.mvg.com.padhead.overlay.OverlayTouchWeightSettings;
import padhead.mvg.com.padhead.overlay.OverlayTouchless;
import padhead.mvg.com.padhead.solver.OrbWeight;
import padhead.mvg.com.padhead.solver.PADBoard;
import padhead.mvg.com.padhead.solver.PADHeadSolver;
import padhead.mvg.com.padhead.solver.PADSolution;

/**
 * Service that handles the click handlers and general application
 * Author: Maxim Gomov
 */

public class PADHeadOverlayService extends OverlayService {

	/**
	 * If this is toggled, enables debugging statements for the entire application by being or'd with
	 */
	public static boolean debugAll = false;

	/**
	 * Local debugging flag
	 */
	private boolean debug = false;

	/**
	 * A reference to this instance, to pass around between classes and functions that need to
	 * reference this context
	 */
	public static PADHeadOverlayService instance;

	/**
	 * The 'touchless overlay' which displays the running path
	 */
	private OverlayTouchless overlayTouchless;

	/**
	 * Solution for the touchless overlay to path over
	 */
	private PADSolution activeSolution;

	/**
	 * Touchable overlay at the top of the main screen of the application
	 */
	private OverlayTouch overlayTouch;

	/**
	 * Touchable overlay which has a trigger to unhide the overlay
	 */
	private OverlayTouchHidden overlayTouchHidden;

	/**
	 * Touchable overlay responsible for orb setup and general game settings
	 */
	private OverlayTouchOrbSetup overlayTouchOrbSetup;

	/**
	 * Touchable overlay for picking a solution to display
	 */
	private OverlayTouchSolutions overlayTouchSolutions;

	/**
	 * Touchable overlay for setting up orb weights
	 */
	private OverlayTouchWeightSettings overlayTouchWeightSettings;

	/**
	 * Convenience binding for the 'buttons' used to handle path display
	 */
	private PathDisplayBindings pathBinds;

	/**
	 * Convenience binding for the clickable buttons used to handle orb setup
	 */
	private SetupDisplayBindings setupBinds;

	/**
	 * Handles triple tapping on a color to fill when overlayTouchOrbSetup is active
	 */
	private boolean tripleTapFill = false;

	/**
	 * Handles the drop button behavior in overlayTouchOrbSetup
	 */
	private boolean doubleTapReset = false;

	/**
	 * List of weights used to prune less-desirable solutions
	 */
	private ArrayList<OrbWeight> weights;

	/**
	 * Maximum number of moves that the solver will accept
	 */
	private int maxMoves = 26;

	/**
	 * Toggle which determines if 8directional movement is allowed in the solver's pathing
	 */
	private boolean allow8Dir = false;

	/**
	 * Maximumm number of simplified (i.e., discrete) solutions
	 */
	private int maxSimplifiedSolutions = 30;

	/**
	 * Contains the radiobuttons generated from parsing weight_values.json
	 */
	HashMap<Integer, WeightedRadioButton> radioButtons;

	/**
	 * Rows and columns for the board, conceivably extensible past PAD
	 */
	public static int rows = 5;
	public static int cols = 6;

	/**
	 * Convenience enum to handle the definitions of orbs:
	 * <p/>
	 * 'c' is the fancy unicode to display on the buttons for visual cues
	 * <p/>
	 * 'rc' is the actual character representation used for that orb
	 */

	public enum ORB {
		RED('☗', "#ff0000", "#CC0000", 'r'), BLUE('☔', "#0000ff", "#0000CC", 'b'), GREEN('♣', "#00ff00", "#00CC00", 'g'), YELLOW('☀', "#ffff00", "#CCCC00", 'y'), PURPLE('☪', "#9900CC", "#7A00A3", 'p'), HEART('♥', "#ff1493", "#CC1076", 'h'), NONE(' ', "#ffffff", "#ffffff", 'u');
		public final char c;
		public final char rc;
		public String color, backgroundColor;

		ORB(char ch, String cl, String bgcl, char arc) {
			c = ch;
			rc = arc;
			color = cl;
			backgroundColor = bgcl;
		}
	}

	/**
	 * Current active orb for marking the board state in overlayTouchOrbSetup
	 */
	public ORB activeOrb;

	@Override
	public void onCreate() {
		super.onCreate();

		// create the notification to allow the application to be marked as a foreground service,
		// i.e. not get shut down by the system's GC when an app like PAD requests more resources
		Notification notification = new Notification(R.drawable.ic_launcher, "PADHead Overlay is running",
				System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, PADHeadOverlayShowActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(this, "PADHead overlay",
				"Running", pendingIntent);
		startForeground('p' + 'a' + 'd', notification);

		activeOrb = ORB.NONE;
		instance = this;
		overlayTouchless = new OverlayTouchless(this);
		overlayTouch = new OverlayTouch(this);
		overlayTouchHidden = new OverlayTouchHidden(this);
		overlayTouchOrbSetup = new OverlayTouchOrbSetup(this);
		overlayTouchSolutions = new OverlayTouchSolutions(this);
		overlayTouchWeightSettings = new OverlayTouchWeightSettings(this);

		overlayTouchSolutions.setVisibility(View.GONE);
		overlayTouch.setVisibility(View.VISIBLE);
		overlayTouchHidden.setVisibility(View.GONE);
		overlayTouchOrbSetup.setVisibility(View.GONE);
		overlayTouchWeightSettings.setVisibility(View.GONE);

		ProgressBar pb = ((ProgressBar) overlayTouchOrbSetup.findViewById(R.id.pb_orbSetup));
		pb.setMax(100);
		LayoutInflater li = LayoutInflater.from(this);

		// long click on the 'run again' button will hide the path but not the UI
		Button runagain = (Button) (overlayTouch.findViewById(R.id.btn_replay));
		runagain.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				pathBinds.hideAll(true);
				return true;
			}
		});

		// long clicks on the orbs will fill the remainder of the empty board with the color
		Button red = (Button) (overlayTouchOrbSetup.findViewById(R.id.btn_redOrbSelect));
		red.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				setupBinds.fill(ORB.RED);
				return true;
			}
		});

		Button blue = (Button) (overlayTouchOrbSetup.findViewById(R.id.btn_blueOrbSelect));
		blue.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				setupBinds.fill(ORB.BLUE);
				return true;
			}
		});

		Button grn = (Button) (overlayTouchOrbSetup.findViewById(R.id.btn_greenOrbSelect));
		grn.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				setupBinds.fill(ORB.GREEN);
				return true;
			}
		});

		Button prp = (Button) (overlayTouchOrbSetup.findViewById(R.id.btn_darkOrbSelect));
		prp.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				setupBinds.fill(ORB.PURPLE);
				return true;
			}
		});

		Button light = (Button) (overlayTouchOrbSetup.findViewById(R.id.btn_lightOrbSelect));
		light.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				setupBinds.fill(ORB.YELLOW);
				return true;
			}
		});

		Button hrt = (Button) (overlayTouchOrbSetup.findViewById(R.id.btn_healthOrbSelect));
		hrt.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				setupBinds.fill(ORB.HEART);
				return true;
			}
		});


		// long clicking the drop button will reset the board
		Button reset = (Button) (overlayTouchOrbSetup.findViewById(R.id.btn_resetOrbSetup));
		reset.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				tripleTapFill = false;
				doubleTapReset = false;
				setupBinds.reset();
				return true;
			}
		});

		pathBinds = new PathDisplayBindings(overlayTouchless, rows, cols);
		setupBinds = new SetupDisplayBindings(this, overlayTouchOrbSetup, rows, cols);

		/* Block to populate the weights options */

		// the radiogroup for the weight preset selections
		RadioGroup radioGroup = (RadioGroup) overlayTouchWeightSettings.findViewById(R.id.rg_weightPresets);

		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				WeightedRadioButton btn = radioButtons.get(checkedId);
				setWeights(btn.rnw(), btn.rmw(), btn.bnw(), btn.bmw(), btn.gnw(), btn.gmw(), btn.lnw(), btn.lmw(), btn.dnw(), btn.dmw(), btn.hnw(), btn.hmw(), btn.jnw(), btn.jmw());

			}
		});

		radioButtons = new HashMap<Integer, WeightedRadioButton>();

		// base for the generated IDs for each radiobutton, i.e. id = idMask + i
		int idMask = 'P' + 'A' + 'D' + 'H' + 'E' + 'A' + 'D';

		String json = "";
		InputStream inputStream = this.getResources().openRawResource(R.raw.weight_values);
		JSONObject jsonObject = null;

		// read in the weight_values.json file, parse it, and load up the weighted radio buttons into
		// the list
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			String readLine = "";
			while (readLine != null) {
				json += "\n" + readLine;
				readLine = br.readLine();
			}

			jsonObject = new JSONObject(json);
			JSONArray weights = jsonObject.getJSONArray("weights");
			for (int i = 0; i < weights.length(); i++) {
				WeightedRadioButton rb = new WeightedRadioButton(this);
				JSONObject obj = weights.getJSONObject(i);

				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
				rb.setLayoutParams(params);

				rb.setText(obj.getString("name"));
				rb.setWeights((float) obj.getDouble("rnw"), (float) obj.getDouble("rmw"), (float) obj.getDouble("bnw"), (float) obj.getDouble("bmw"), (float) obj.getDouble("gnw"), (float) obj.getDouble("gmw"), (float) obj.getDouble("lnw"), (float) obj.getDouble("lmw"), (float) obj.getDouble("dnw"), (float) obj.getDouble("dmw"), (float) obj.getDouble("hnw"), (float) obj.getDouble("hmw"), (float) obj.getDouble("jnw"), (float) obj.getDouble("jmw"));
				rb.setId(i + idMask);

				radioGroup.addView(rb);
				radioButtons.put(i + idMask, rb);
			}
		} catch (UnsupportedEncodingException e) {
			Log.e("Weights", "Unsupported format for weight_values.json");
		} catch (IOException e) {
			Log.e("Weights", "Unspecified IO error");
		} catch (JSONException e) {
			Log.e("Weights", "JSON Error");
		}

		// orb default weights setup
		weights = new ArrayList<OrbWeight>();
		weights.add(new OrbWeight('r', 1, 3));
		weights.add(new OrbWeight('b', 1, 3));
		weights.add(new OrbWeight('g', 1, 3));
		weights.add(new OrbWeight('y', 1, 3));
		weights.add(new OrbWeight('p', 1, 3));
		weights.add(new OrbWeight('h', 0.3f, 0.3f));
		weights.add(new OrbWeight('u', 0.1f, 0.1f));

		((Button) overlayTouch.findViewById(R.id.btn_replay)).setEnabled(false);
		((Button) overlayTouch.findViewById(R.id.btn_comboList)).setEnabled(false);
	}

	/**
	 * Called when the drop button is hit, drops all matches in the current board setup to expedite
	 * the new board setup
	 */
	public void dropMatches(PADSolution sol) {

		// put the board into the solver and let it process eliminations and drops
		PADHeadSolver slv = new PADHeadSolver();
		PADBoard board = slv.dropMatches(sol);

		// recreate the processed board
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				Button b = setupBinds.binding[i][j];
				ORB curr = ORB.NONE;
				switch (board.getBoard()[i][j]) {
					case 'r':
						curr = ORB.RED;
						break;
					case 'b':
						curr = ORB.BLUE;
						break;
					case 'g':
						curr = ORB.GREEN;
						break;
					case 'p':
						curr = ORB.PURPLE;
						break;
					case 'y':
						curr = ORB.YELLOW;
						break;
					case 'h':
						curr = ORB.HEART;
						break;
					case 'j':
						curr = ORB.NONE;
						break;
				}

				if (curr.rc != ORB.NONE.rc) {
					b.setText("" + curr.c);
					b.setTextColor(Color.parseColor(curr.color));
					b.setBackgroundColor(Color.WHITE);
					b.setAlpha(0.45f);
				} else {
					b.setText("" + ORB.NONE.rc);
					b.setTextColor(Color.TRANSPARENT);
					b.setBackgroundColor(Color.TRANSPARENT);
				}
			}
		}
	}

	/**
	 * Load the weights from the preset into the text fields (from where, upon leaving the settings
	 * menu, they get loaded into the actual weights)
	 */
	public void setWeights(float RNW, float RMW, float BNW, float BMW, float GNW, float GMW, float LNW, float LMW, float PNW, float PMW, float HNW, float HMW, float JNW, float JMW) {
		EditText rnw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_rnw);
		EditText rmw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_rmw);

		EditText bnw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_bnw);
		EditText bmw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_bmw);

		EditText gnw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_gnw);
		EditText gmw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_gmw);

		EditText lnw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_ynw);
		EditText lmw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_ymw);

		EditText pnw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_pnw);
		EditText pmw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_pmw);

		EditText hnw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_hnw);
		EditText hmw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_hmw);

		EditText jnw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_jnw);
		EditText jmw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_jmw);

		rnw.setText(RNW + "");
		rmw.setText(RMW + "");
		bnw.setText(BNW + "");
		bmw.setText(BMW + "");
		gnw.setText(GNW + "");
		gmw.setText(GMW + "");
		lnw.setText(LNW + "");
		lmw.setText(LMW + "");
		pnw.setText(PNW + "");
		pmw.setText(PMW + "");
		hnw.setText(HNW + "");
		hmw.setText(HMW + "");
		jnw.setText(JNW + "");
		jmw.setText(JMW + "");
	}

	/**
	 * Hides all of the overlays
	 */
	public void btnHideOverlay(View v) {
		overlayTouch.setVisibility(View.GONE);
		overlayTouchHidden.setVisibility(View.VISIBLE);
		overlayTouchless.setVisibility(View.GONE);
		//overlayTouchHidden.switchTouchFoucusOff();
	}

	/**
	 * Sets the orb setup overlay to be active
	 */
	public void btnSetupOrbs(View v) {
		overlayTouchOrbSetup.setVisibility(View.VISIBLE);
		overlayTouch.setVisibility(View.GONE);
		overlayTouchless.setVisibility(View.GONE);

		Button btn;
		btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_submitOrbSetup);
		btn.setEnabled(true);
		btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_cancelOrbSetup);
		btn.setEnabled(true);
		btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_resetOrbSetup);
		btn.setEnabled(true);
		btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_orbSettings);
		btn.setEnabled(true);
		btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_redOrbSelect);
		btn.setEnabled(true);
		btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_blueOrbSelect);
		btn.setEnabled(true);
		btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_greenOrbSelect);
		btn.setEnabled(true);
		btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_darkOrbSelect);
		btn.setEnabled(true);
		btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_lightOrbSelect);
		btn.setEnabled(true);
		btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_healthOrbSelect);
		btn.setEnabled(true);

		((TextView) (overlayTouchOrbSetup.findViewById(R.id.tvOrbSetupLog))).setText("");
	}

	/**
	 * Sets the combo list as the active view
	 */
	public void btnComboList(View v) {
		overlayTouchless.setVisibility(View.GONE);
		overlayTouch.setVisibility(View.GONE);
		overlayTouchSolutions.setVisibility(View.VISIBLE);
	}

	/**
	 * Unhides the overlay
	 */
	public void btnShowOverlay(View v) {
		overlayTouch.setVisibility(View.VISIBLE);
		overlayTouchHidden.setVisibility(View.GONE);
		overlayTouchless.setVisibility(View.VISIBLE);
	}

	/**
	 * Sets the main view to be active from the orb setup menu
	 */
	public void btnCancelOrbSetup(View v) {
		doubleTapReset = false;
		overlayTouchOrbSetup.setVisibility(View.GONE);
		overlayTouch.setVisibility(View.VISIBLE);
		overlayTouchless.setVisibility(View.VISIBLE);
	}

	/**
	 * Called from the 'drop' button in the orb setup menu, alternates between dropping orbs and
	 * wiping the board state entirely
	 */
	public void btnResetOrbSetup(View v) {
		if (!doubleTapReset) {
			doubleTapReset = true;
			if (activeSolution != null) {
				dropMatches(activeSolution);
			}
		} else {
			setupBinds.reset();
			doubleTapReset = false;
		}
	}

	/**
	 * Submits the orb setup configuration to the solver with the defined presets
	 */
	public void btnSubmitOrbSetup(View v) {

		// serialize the current orb setup so it can be deserialized into a board state later
		String s = setupBinds.serialize();
		tripleTapFill = false;

		// if the serialized string has the character corresponding to the 'empty' orb, reject the
		// input and give the user an indication
		if (s.contains("" + ORB.NONE.rc)) {
			((TextView) (overlayTouchOrbSetup.findViewById(R.id.tvOrbSetupLog))).setVisibility(View.VISIBLE);
			((TextView) (overlayTouchOrbSetup.findViewById(R.id.tvOrbSetupLog))).setText("Error: Missing orb");
		} else {

			// disable all the buttons so the user doesn't change the board state while it's loading
			// even though it wouldn't actually change the state of the processing
			activeSolution = null;
			((TextView) (overlayTouchSolutions.findViewById(R.id.tv_currentSolution))).setText("Selected Combo:\nnone");
			Button btn;
			btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_submitOrbSetup);
			btn.setEnabled(false);
			btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_cancelOrbSetup);
			btn.setEnabled(false);
			btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_resetOrbSetup);
			btn.setEnabled(false);
			btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_orbSettings);
			btn.setEnabled(false);
			btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_redOrbSelect);
			btn.setEnabled(false);
			btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_blueOrbSelect);
			btn.setEnabled(false);
			btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_greenOrbSelect);
			btn.setEnabled(false);
			btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_darkOrbSelect);
			btn.setEnabled(false);
			btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_lightOrbSelect);
			btn.setEnabled(false);
			btn = (Button) overlayTouchOrbSetup.findViewById(R.id.btn_healthOrbSelect);
			btn.setEnabled(false);
			activeOrb = null;

			// send all the data to the asycnhronous solver
			AsyncSolve asolve = new AsyncSolve(weights, maxSimplifiedSolutions, rows, cols, this, overlayTouchOrbSetup, overlayTouchSolutions);
			asolve.execute(s, maxMoves + "", allow8Dir ? "true" : "false");
		}
	}

	/**
	 * Called by the button in overlayTouchOrbSetup, sets the active orb to red or fills on a triple
	 * tap
	 */
	public void btnRed(View v) {
		doubleTapReset = false;
		if (activeOrb == ORB.RED) {
			if (!tripleTapFill) tripleTapFill = true;
			else
				setupBinds.fill(ORB.RED);
		} else {
			activeOrb = ORB.RED;
			tripleTapFill = false;
		}
	}

	/**
	 * Called by the button in overlayTouchOrbSetup, sets the active orb to blue or fills on a triple
	 * tap
	 */
	public void btnBlue(View v) {
		doubleTapReset = false;
		if (activeOrb == ORB.BLUE) {
			if (!tripleTapFill) tripleTapFill = true;
			else
				setupBinds.fill(ORB.BLUE);
		} else {
			activeOrb = ORB.BLUE;
			tripleTapFill = false;
		}
	}

	/**
	 * Called by the button in overlayTouchOrbSetup, sets the active orb to green or fills on a triple
	 * tap
	 */
	public void btnGreen(View v) {
		doubleTapReset = false;
		if (activeOrb == ORB.GREEN) {
			if (!tripleTapFill) tripleTapFill = true;
			else
				setupBinds.fill(ORB.GREEN);
		} else {
			activeOrb = ORB.GREEN;
			tripleTapFill = false;
		}
	}

	/**
	 * Called by the button in overlayTouchOrbSetup, sets the active orb to light or fills on a triple
	 * tap
	 */
	public void btnLight(View v) {
		doubleTapReset = false;
		if (activeOrb == ORB.YELLOW) {
			if (!tripleTapFill) tripleTapFill = true;
			else
				setupBinds.fill(ORB.YELLOW);
		} else {
			activeOrb = ORB.YELLOW;
			tripleTapFill = false;
		}
	}

	/**
	 * Called by the button in overlayTouchOrbSetup, sets the active orb to dark or fills on a triple
	 * tap
	 */
	public void btnDark(View v) {
		doubleTapReset = false;
		if (activeOrb == ORB.PURPLE) {
			if (!tripleTapFill) tripleTapFill = true;
			else
				setupBinds.fill(ORB.PURPLE);
		} else {
			activeOrb = ORB.PURPLE;
			tripleTapFill = false;
		}
	}

	/**
	 * Called by the button in overlayTouchOrbSetup, sets the active orb to heal or fills on a triple
	 * tap
	 */
	public void btnHeart(View v) {
		doubleTapReset = false;
		if (activeOrb == ORB.HEART) {
			if (!tripleTapFill) tripleTapFill = true;
			else
				setupBinds.fill(ORB.HEART);
		} else {
			activeOrb = ORB.HEART;
			tripleTapFill = false;
		}
	}

	/**
	 * Runs the path again
	 */
	public void btnRunAgain(View v) {
		TracePath path = new TracePath(overlayTouch, overlayTouchless, pathBinds);
		ArrayList<Button> btns = pathBinds.tracePath(activeSolution, false);
		pathBinds.hideAll(true);
		ProgressBar pb = (ProgressBar) overlayTouchless.findViewById(R.id.pb_tl_timer);
		pb.setProgress(100);
		pb.setVisibility(View.VISIBLE);
		path.execute(btns);
	}

	/**
	 * Called from the solution selection view, submits the solution and traces the path
	 */
	public void btnSolutionsSubmit(View v) {
		if (activeSolution == null) return;
		overlayTouch.setVisibility(View.VISIBLE);
		overlayTouchless.setVisibility(View.VISIBLE);
		overlayTouchSolutions.setVisibility(View.GONE);
		pathBinds.hideAll(true);
		TracePath tp = new TracePath(overlayTouch, overlayTouchless, pathBinds);
		((Button) overlayTouch.findViewById(R.id.btn_replay)).setEnabled(true);
		((Button) overlayTouch.findViewById(R.id.btn_comboList)).setEnabled(true);
		tp.execute(pathBinds.tracePath(activeSolution, false));
	}

	/**
	 * Called from overlayTouchOrbSetup, opens the weight settings
	 */
	public void btnWeightSettings(View v) {
		doubleTapReset = false;
		overlayTouchOrbSetup.setVisibility(View.GONE);
		overlayTouchWeightSettings.setVisibility(View.VISIBLE);
	}

	/**
	 * Called from the weights setup view, parses the weights and sets the actual values
	 */
	public void btnWeightsDone(View v) {

		EditText rnw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_rnw);
		EditText rmw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_rmw);

		EditText bnw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_bnw);
		EditText bmw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_bmw);

		EditText gnw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_gnw);
		EditText gmw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_gmw);

		EditText lnw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_ynw);
		EditText lmw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_ymw);

		EditText pnw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_pnw);
		EditText pmw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_pmw);

		EditText hnw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_hnw);
		EditText hmw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_hmw);

		EditText jnw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_jnw);
		EditText jmw = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_jmw);
		try {
			weights.get(0).setNormalWeight(Float.parseFloat(rnw.getText().toString()));
			weights.get(0).setMassWeight(Float.parseFloat(rmw.getText().toString()));

			weights.get(1).setNormalWeight(Float.parseFloat(bnw.getText().toString()));
			weights.get(1).setMassWeight(Float.parseFloat(bmw.getText().toString()));

			weights.get(2).setNormalWeight(Float.parseFloat(gnw.getText().toString()));
			weights.get(2).setMassWeight(Float.parseFloat(gmw.getText().toString()));

			weights.get(3).setNormalWeight(Float.parseFloat(lnw.getText().toString()));
			weights.get(3).setMassWeight(Float.parseFloat(lmw.getText().toString()));

			weights.get(4).setNormalWeight(Float.parseFloat(pnw.getText().toString()));
			weights.get(4).setMassWeight(Float.parseFloat(pmw.getText().toString()));

			weights.get(5).setNormalWeight(Float.parseFloat(hnw.getText().toString()));
			weights.get(5).setMassWeight(Float.parseFloat(hmw.getText().toString()));

			weights.get(6).setNormalWeight(Float.parseFloat(jnw.getText().toString()));
			weights.get(6).setMassWeight(Float.parseFloat(jmw.getText().toString()));
		} catch (NumberFormatException e) {
			//TODO: handle this better
			return;
		}

		overlayTouchOrbSetup.setVisibility(View.VISIBLE);
		overlayTouchWeightSettings.setVisibility(View.GONE);

		EditText mm = (EditText) overlayTouchWeightSettings.findViewById(R.id.et_maxMoves);
		maxMoves = Integer.parseInt(mm.getText().toString());

		allow8Dir = ((CheckBox) overlayTouchWeightSettings.findViewById(R.id.cb_8dir)).isChecked();
	}

	/**
	 * Sets the active solution for path tracing
	 */
	public void setActiveSolution(PADSolution s) {
		activeSolution = s;
	}

	/**
	 * Sets the triple tap state, used outside of the class
	 */
	public void setTripleTap(boolean tt) {
		tripleTapFill = tt;
	}

	public ORB getActiveOrb() {
		return activeOrb;
	}
}
