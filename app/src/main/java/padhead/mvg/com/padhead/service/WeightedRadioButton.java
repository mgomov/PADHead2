package padhead.mvg.com.padhead.service;

import android.content.Context;
import android.widget.RadioButton;

/**
 * Created by Max on 2/15/2015.
 */
public class WeightedRadioButton extends RadioButton {
	private float rnw;
	private float rmw;
	private float bnw;
	private float bmw;
	private float gnw;
	private float gmw;
	private float lnw;
	private float lmw;
	private float dnw;
	private float dmw;
	private float hnw;
	private float hmw;
	private float jnw;
	private float jmw;

	public WeightedRadioButton(Context context) {
		super(context);
	}

	public void setWeights(float RNW, float RMW, float BNW, float BMW, float GNW, float GMW, float LNW, float LMW, float PNW, float PMW, float HNW, float HMW, float JNW, float JMW) {
		rnw = RNW;
		rmw = RMW;
		bnw = BNW;
		bmw = BMW;
		gnw = GNW;
		gmw = GMW;
		lnw = LNW;
		lmw = LMW;
		dnw = PNW;
		dmw = PMW;
		hnw = HNW;
		hmw = HMW;
		jnw = JNW;
		jmw = JMW;
	}

	public float rnw() {
		return rnw;
	}

	public float rmw() {
		return rmw;
	}

	public float bnw() {
		return bnw;
	}

	public float bmw() {
		return bmw;
	}

	public float gnw() {
		return gnw;
	}

	public float gmw() {
		return gmw;
	}

	public float lnw() {
		return lnw;
	}

	public float lmw() {
		return lmw;
	}

	public float dnw() {
		return dnw;
	}

	public float dmw() {
		return dmw;
	}

	public float hnw() {
		return hnw;
	}

	public float hmw() {
		return hmw;
	}

	public float jnw() {
		return jnw;
	}

	public float jmw() {
		return jmw;
	}
}
