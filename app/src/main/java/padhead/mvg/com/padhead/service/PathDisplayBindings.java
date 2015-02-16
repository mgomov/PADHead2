package padhead.mvg.com.padhead.service;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

import padhead.mvg.com.padhead.R;
import padhead.mvg.com.padhead.solver.PADSolution;
import padhead.mvg.com.padhead.solver.RowColumn;

/**
 * Convenience binding to manage the tiles in the path display portion of the touchless overlay
 * Author: Maxim Gomov
 */
public class PathDisplayBindings extends BoardBinding {
	public PathDisplayBindings(View tlo, int rows, int cols) {
		super(tlo, R.id.ll_touchless_path, rows, cols);

		hideAll(true);
	}

	@Override
	protected Button configBindingElement(LinearLayout.LayoutParams params, Context ctx) {
		Button bindingElement = new Button(ctx);
		bindingElement.setLayoutParams(params);
		bindingElement.setVisibility(View.INVISIBLE);
		bindingElement.setText(" ");
		return bindingElement;
	}

	/**
	 * Enable an individual tile based on coordinate
	 */
	public void enable(int x, int y, String text) {
		Button b = binding[x][y];
		b.setVisibility(View.VISIBLE);
		b.setBackgroundColor(Color.BLUE);
		b.setText(binding[x][y].getText().toString() + " " + text);
	}

	/**
	 * Hide all of the tiles governed by this binding
	 */
	public void hideAll(boolean removeText) {
		for (int i = 0; i < binding.length; i++) {
			for (int j = 0; j < binding[0].length; j++) {
				binding[i][j].setVisibility(View.INVISIBLE);
				binding[i][j].setBackgroundColor(Color.TRANSPARENT);
				if (removeText) binding[i][j].setText("");
			}
		}
	}

	/**
	 * Trace a path from the given solution and then return a list with each button/tile in the order
	 * they're supposed to be toggled in
	 */
	public ArrayList<Button> tracePath(PADSolution solution, boolean doEnable) {
		RowColumn cursor = solution.getInitCursor().copy();
		ArrayList<Button> enabled = new ArrayList<Button>();
		enabled.add(binding[cursor.row][cursor.col]);

		if (doEnable) enable(cursor.row, cursor.col, 1 + "");

		int itr = 2;

		// path is defined by integers for the direction to move next in
		for (Integer i : solution.getPath()) {
			switch (i) {
				case 0:
					cursor.col++;
					break;
				case 1:
					cursor.col++;
					cursor.row--;
					break;
				case 2:
					cursor.row++;
					break;
				case 3:
					cursor.row++;
					cursor.col--;
					break;
				case 4:
					cursor.col--;
					break;
				case 5:
					cursor.col--;
					cursor.row--;
					break;
				case 6:
					cursor.row--;
					break;
				case 7:
					cursor.row--;
					cursor.col++;
					break;
			}
			if (cursor.row > -1 && cursor.col > -1)
				enabled.add(binding[cursor.row][cursor.col]);
			if (doEnable) enable(cursor.row, cursor.col, "" + itr);
			itr++;

		}
		return enabled;
	}

}
