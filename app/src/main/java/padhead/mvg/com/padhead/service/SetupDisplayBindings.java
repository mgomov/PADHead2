package padhead.mvg.com.padhead.service;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import padhead.mvg.com.padhead.R;
import padhead.mvg.com.padhead.service.PADHeadOverlayService.ORB;

/**
 * Convenience binding for each orb slot in the orb setup view
 * Author: Maxim Gomov
 */
public class SetupDisplayBindings extends BoardBinding {
	/**
	 * Accessor for the overlay service
	 */
	PADHeadOverlayService service;

	public SetupDisplayBindings(PADHeadOverlayService service, View v, int rows, int cols) {
		super(v, R.id.ll_setup_orb_container, rows, cols);
		this.service = service;
		reset();
	}

	@Override
	protected Button configBindingElement(LinearLayout.LayoutParams params, Context ctx) {
		Button bindingElement = new Button(ctx);
		bindingElement.setLayoutParams(params);
		bindingElement.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
		bindingElement.setAlpha(0);
		bindingElement.setText("" + ORB.NONE.rc);


		View.OnTouchListener listener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
//				Rect rect = new Rect();
//				v.getHitRect(rect);
//
//				final float x = event.getX() + rect.left;
//				final float y = event.getY() + rect.top;
//
//				if (!rect.contains((int) x, (int) y)) {
//					Log.d("Touch", "Left the premises");
//
//					return false;
//				}

				if (service.activeOrb == null) return true;

				service.setTripleTap(false);
				Button b = (Button) v;
				b.setText("" + service.getActiveOrb().c);
				b.setTextColor(Color.parseColor(service.getActiveOrb().color));
				b.setBackgroundColor(Color.WHITE);
				b.setAlpha(0.45f);
				return true;
			}
		};

		bindingElement.setOnTouchListener(listener);

		return bindingElement;
	}

	/**
	 * Fill the open orb slots with the specified orb
	 */
	public void fill(PADHeadOverlayService.ORB type) {
		for (int i = 0; i < binding.length; i++) {
			for (int j = 0; j < binding[0].length; j++) {
				Button b = binding[i][j];
				if (b.getText().equals("" + ORB.NONE.rc)) {
					b.setText("" + type.c);
					b.setTextColor(Color.parseColor(type.color));
					b.setBackgroundColor(Color.WHITE);
					b.setAlpha(0.45f);
				}
			}
		}
	}

	/**
	 * Reset the entire binding's orbs
	 */
	public void reset() {
		for (int i = 0; i < binding.length; i++) {
			for (int j = 0; j < binding[0].length; j++) {
				Button b = binding[i][j];
				b.setText("" + ORB.NONE.rc);
				b.setTextColor(Color.TRANSPARENT);
				b.setBackgroundColor(Color.TRANSPARENT);
			}
		}
	}

	/**
	 * Serializes the setup state for the solver to work with
	 */
	public String serialize() {
		String s = "";

		for (Button[] outer : binding) {
			for (Button b : outer) {
				s += b.getText();
			}
		}

		s = s.replace(ORB.RED.c, ORB.RED.rc);
		s = s.replace(ORB.BLUE.c, ORB.BLUE.rc);
		s = s.replace(ORB.GREEN.c, ORB.GREEN.rc);
		s = s.replace(ORB.YELLOW.c, ORB.YELLOW.rc);
		s = s.replace(ORB.PURPLE.c, ORB.PURPLE.rc);
		s = s.replace(ORB.HEART.c, ORB.HEART.rc);
		s = s.replace(ORB.NONE.c, ORB.NONE.rc);

		return s;
	}
}
