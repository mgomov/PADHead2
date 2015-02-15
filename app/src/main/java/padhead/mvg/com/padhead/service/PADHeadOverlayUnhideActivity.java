package padhead.mvg.com.padhead.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

// TODO implement
public class PADHeadOverlayUnhideActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent i = new Intent("mvg.com.padhead.UNHIDE");
		i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		i.addCategory(Intent.CATEGORY_DEFAULT);

		sendBroadcast(i);
		finish();
	}
    
}
