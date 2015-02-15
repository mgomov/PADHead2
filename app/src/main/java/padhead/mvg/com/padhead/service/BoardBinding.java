package padhead.mvg.com.padhead.service;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Abstract class for generating bindings
 * Author: Maxim Gomov
 */
public abstract class BoardBinding {
	protected Button[][] binding;

	public BoardBinding(View v, int containerId, int rows, int cols){
		binding = new Button[rows][cols];
		LinearLayout container = (LinearLayout) v.findViewById(containerId);
		LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f / ((float)rows));
		LinearLayout.LayoutParams elementParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f / ((float)cols));

		for(int i = 0; i < rows; i++){
			LinearLayout aRow = new LinearLayout(v.getContext());
			aRow.setOrientation(LinearLayout.HORIZONTAL);
			aRow.setLayoutParams(rowParams);
			container.addView(aRow);
			for(int j = 0; j < cols; j++){
				Button pathElement = new Button(v.getContext());
				configBindingElement(pathElement, elementParams);
				aRow.addView(pathElement);
				binding[i][j] = pathElement;
			}
		}
	}

	/** Applies button styling, and general setup to each element of the binding */
	protected abstract void configBindingElement(Button bindingElement, LinearLayout.LayoutParams params);

}
