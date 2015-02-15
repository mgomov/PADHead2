package padhead.mvg.com.padhead.overlay;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.WindowManager;

import padhead.mvg.com.padhead.R;

/**
 * Created by Max on 12/29/2014.
 */
public class OverlayTouchHidden extends OverlayView {
	public OverlayTouchHidden(OverlayService service) {

		super(service, R.layout.overlay_touch_hidden, 1, 1);
	}
	@Override
	protected void setupLayoutParams() {
		int type;
		if (__grabClicks == 0) {
			type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
		} else {
			type = WindowManager.LayoutParams.TYPE_PHONE;
		}
		layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
				type,

				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
						| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

				, PixelFormat.TRANSLUCENT);

		layoutParams.gravity = getLayoutGravity();

		onSetupLayoutParams();


	}

}
