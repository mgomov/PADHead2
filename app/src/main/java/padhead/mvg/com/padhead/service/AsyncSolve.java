package padhead.mvg.com.padhead.service;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import padhead.mvg.com.padhead.R;
import padhead.mvg.com.padhead.overlay.OverlayTouchOrbSetup;
import padhead.mvg.com.padhead.overlay.OverlayTouchSolutions;
import padhead.mvg.com.padhead.solver.OrbWeight;
import padhead.mvg.com.padhead.solver.PADHeadSolverAsync;
import padhead.mvg.com.padhead.solver.PADSolution;

/**
 * Asynchronous task to solve a provided board state without stalling the UI thread
 * Author: Maxim Gomov
 */
public class AsyncSolve extends AsyncTask<String, String, ArrayList<PADSolution>> {

	/** Collection of orb weights used to prune worse solutions */
	ArrayList<OrbWeight> weights;

	/** Threshhold for simplified solutions to accept */
	int maxSimplifiedSolutions;

	/** Dimensions of the game board, defined by the overlay service*/
	int rows;
	int cols;

	/** Accessor for the service and some overlays*/
	PADHeadOverlayService serviceAccessor;
	View overlayTouchOrbSetup;
	View overlayTouchSolutions;

	public AsyncSolve(ArrayList<OrbWeight> weights, int maxSimplifiedSolutions, int rows, int cols, PADHeadOverlayService serviceAccessor, View overlayTouchOrbSetup, View overlayTouchSolutions){
		this.weights = weights;
		this.maxSimplifiedSolutions = maxSimplifiedSolutions;
		this.rows = rows;
		this.cols = cols;
		this.serviceAccessor = serviceAccessor;
		this.overlayTouchOrbSetup = overlayTouchOrbSetup;
		this.overlayTouchSolutions = overlayTouchSolutions;
	}


	@Override
	protected void onPreExecute() {
		// hide the 'error log' and show the progress bar
		((TextView) (overlayTouchOrbSetup.findViewById(R.id.tvOrbSetupLog))).setVisibility(View.INVISIBLE);
		((TextView) (overlayTouchOrbSetup.findViewById(R.id.tvOrbSetupLog))).setText("");
		((ProgressBar) overlayTouchOrbSetup.findViewById(R.id.pb_orbSetup)).setVisibility(View.VISIBLE);

	}

	@Override
	protected ArrayList<PADSolution> doInBackground(String... params) {
		// feed the async solver the params and let it handle the UI updates using the accessors provided earlier
		PADHeadSolverAsync solver = new PADHeadSolverAsync();
		solver.solve(params[0], Integer.parseInt(params[1].toString()), params[2].contains("true") ? true : false, this, weights, maxSimplifiedSolutions, rows, cols);
		ArrayList<PADSolution> solutions = solver.finalSolutions;
		return solutions;
	}

	/** Used by the async solver to publish progress to the UI thread */
	public void doPublish(int prog) {
		publishProgress("" + prog);
	}

	@Override
	protected void onProgressUpdate(String... progress) {
		ProgressBar pb = ((ProgressBar) overlayTouchOrbSetup.findViewById(R.id.pb_orbSetup));
		pb.setMax(100);
		pb.setProgress(Integer.parseInt(progress[0]));
	}

	@Override
	protected void onPostExecute(ArrayList<PADSolution> solutions) {
		// open up the solutions browser and populate the adapter for it
		overlayTouchOrbSetup.setVisibility(View.GONE);
		overlayTouchSolutions.setVisibility(View.VISIBLE);
		ListView lv = (ListView) overlayTouchSolutions.findViewById(R.id.lv_solutions);
		ArrayList<PADSolution> trimmed = new ArrayList<PADSolution>();

		for (int i = 0; i < 30; i++) {
			if (i < solutions.size()) {
				trimmed.add(solutions.get(i));
			} else {
				break;
			}
		}

		PADAdapter adapter = new PADAdapter(serviceAccessor, trimmed, serviceAccessor);
		lv.setAdapter(adapter);

		// reset the progress bar for next run around
		ProgressBar pb = ((ProgressBar) overlayTouchOrbSetup.findViewById(R.id.pb_orbSetup));
		((ProgressBar) overlayTouchOrbSetup.findViewById(R.id.pb_orbSetup)).setVisibility(View.INVISIBLE);
		pb.setProgress(0);
	}
}