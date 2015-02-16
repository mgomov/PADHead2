package padhead.mvg.com.padhead.solver;

/**
 * Defines an orb and it's weight for normal combos (3-4) and mass combos (5+)
 */
public class OrbWeight {
	/**
	 * Orb type
	 */
	private char type;

	/**
	 * 3-4 combo weight
	 */
	private float normalWeight;

	/**
	 * 5+ combo weight
	 */
	private float massWeight;

	public OrbWeight(char t, float nw, float mw) {
		type = t;
		normalWeight = nw;
		massWeight = mw;
	}

	public float getMassWeight() {
		return massWeight;
	}

	public float getNormalWeight() {
		return normalWeight;
	}

	public void setNormalWeight(float nw) {
		normalWeight = nw;
	}

	public void setMassWeight(float mw) {
		massWeight = mw;
	}


	public char getType() {
		return type;
	}
}
