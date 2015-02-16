package padhead.mvg.com.padhead.service;

import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.util.ArrayList;

import padhead.mvg.com.padhead.R;

/**
 * Asynchronous task to display the path computed by the solver in a traceable fashion
 * Author: Maxim Gomov
 */
public class TracePath extends AsyncTask<ArrayList<Button>, Button, Void> {
	/**
	 * Time in ms spent per orb
	 */
	long timePerOrb = 0;

	/**
	 * Total amount of elapsed time while this task has been running
	 */
	long globalElapsed = 0;

	/**
	 * Counter for the current step the path is on, which is displayed on the actual path step
	 */
	int itr = 0;

	/**
	 * Accessor to the touchable overlay
	 */
	View overlayTouch;

	/**
	 * Accessor to the touchless overlay, i.e. where the path is displayed for the user to follow
	 */
	View overlayTouchless;

	/**
	 * Binding for the displayed 'path tiles' on the touchless overlay
	 */
	PathDisplayBindings pathBinds;

	public TracePath(View overlayTouch, View overlayTouchless, PathDisplayBindings pathBinds) {
		this.overlayTouch = overlayTouch;
		this.overlayTouchless = overlayTouchless;
		this.pathBinds = pathBinds;
	}

	@Override
	protected void onPreExecute() {
		// lock the 'run again' button so you don't get multiple threads concurrently modifying things
		overlayTouch.findViewById(R.id.btn_replay).setEnabled(false);
		pathBinds.hideAll(true);
		ProgressBar pb = (ProgressBar) overlayTouchless.findViewById(R.id.pb_tl_timer);
		pb.setProgress(100);
		pb.setVisibility(View.VISIBLE);
	}

	@Override
	protected Void doInBackground(ArrayList<Button>... params) {
		ArrayList<Button> buttons = params[0];

		// the default time-to-move for the orbs is 4seconds, so divide the 4 seconds amongst the
		// path evenly and leave a tiny bit of leeway for the user
		timePerOrb = 4000l / (buttons.size() + 1l);

		long endTime = System.currentTimeMillis() + timePerOrb * buttons.size();
		long timeForNextTick = System.currentTimeMillis() + timePerOrb;

		itr = 0;

		// the path snakes around with a trail of 4 tiles, after which it turns a light grey
		Button prev = null;
		Button prev2 = null;
		Button prev3 = null;
		Button prev4 = null;
		long startTime = System.currentTimeMillis();
		long timeForNextTimeUpdate = startTime + 100;

		// each path tile is a button that gets styled, and here the path is received as a collection
		// of tiles that have to be iterated through
		while (buttons.size() > 0) {
			long time = System.currentTimeMillis();

			// update the progress bar more frequently than on a 'per-tile' basis, in order to
			// have a smoother progress bar
			if (time > timeForNextTimeUpdate) {
				globalElapsed = System.currentTimeMillis() - startTime;
				publishProgress();

				// arbitrary figure that won't spam updates but is fast enough to be smooth-ish
				timeForNextTimeUpdate = time + 100;
			}

			// if it's time for the next path tile to be moved to, do the moving
			if (timeForNextTick <= time) {
				itr++;
				Button btn = buttons.remove(0);

				publishProgress(btn, prev, prev2, prev3, prev4);
				prev4 = prev3;
				prev3 = prev2;
				prev2 = prev;
				prev = btn;
				timeForNextTick = time + timePerOrb;
			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Button... progress) {
		// an actual path update happens if the list has any elements at all
		if (progress.length > 1) {
			Button b = progress[0];
			Button p = progress[1];
			Button p2 = progress[2];
			Button p3 = progress[3];
			Button p4 = progress[4];

			// set the very last element of the chain to gray, which it will stay until the path is
			// restarted
			if (p4 != null) {
				p4.setBackgroundColor(Color.GRAY);
				p4.setAlpha(0.65f);
			}

			// subsequent elements are darker to lighter shades of orange, closer to the current tile
			// being darker
			if (p3 != null) {
				p3.setBackgroundColor(Color.parseColor("#FF8800"));
				p3.setAlpha(1f);
			}
			if (p2 != null) {
				p2.setBackgroundColor(Color.parseColor("#FF7700"));
				p2.setAlpha(1f);
			}
			if (p != null) {
				p.setBackgroundColor(Color.parseColor("#FF6600"));
				p.setAlpha(1f);
			}

			// set the current tile to just red
			b.setBackgroundColor(Color.RED);
			b.setAlpha(0.95f);
			b.setVisibility(View.VISIBLE);
			b.setText(itr + "");
		} else {
			// update the progress bar with the elapsed timing
			ProgressBar pb = (ProgressBar) overlayTouchless.findViewById(R.id.pb_tl_timer);
			pb.setProgress(100 - (int) ((((double) globalElapsed) / 4000d) * 100d));
		}
	}

	@Override
	protected void onPostExecute(Void nothing) {

		// when donen, release the 'run again' button and hide the progress bar
		overlayTouch.findViewById(R.id.btn_replay).setEnabled(true);
		ProgressBar pb = (ProgressBar) overlayTouchless.findViewById(R.id.pb_tl_timer);
		pb.setProgress(100);
		pb.setVisibility(View.INVISIBLE);
	}
}