package padhead.mvg.com.padhead.overlay;

/**
 * Created by Max on 12/25/2014. Modified.
 */

/*
Copyright 2011 jawsware international

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;

import padhead.mvg.com.padhead.R;
import padhead.mvg.com.padhead.overlay.OverlayService;
import padhead.mvg.com.padhead.overlay.OverlayView;

public class OverlayTouchless extends OverlayView {
	public OverlayTouchless(OverlayService service) {
		super(service, R.layout.overlay_touchless, 1, 0);

	}

	protected void setupLayoutParams(){
		WindowManager wm = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		setMinimumHeight(height / 2);
		super.setupLayoutParams();
	}


	public int getGravity() {
		return Gravity.TOP + Gravity.RIGHT;
	}

	@Override
	protected void onInflateView() {
	}

	@Override
	protected void refreshViews() {
	}

	@Override
	protected void onTouchEvent_Up(MotionEvent event) {

	}

	@Override
	protected void onTouchEvent_Move(MotionEvent event) {

	}

	@Override
	protected void onTouchEvent_Press(MotionEvent event) {

	}

	@Override
	public boolean onTouchEvent_LongPress() {
		return false;
	}
}