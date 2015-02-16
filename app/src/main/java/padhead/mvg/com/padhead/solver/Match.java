package padhead.mvg.com.padhead.solver;

/**
 * Describes an orb match
 */
public class Match {
	/**
	 * The orb type that's been matched
	 */
	private char orb;

	/**
	 * Length of orbs matched
	 */
	private int count;

	public Match(char o, int c) {
		orb = o;
		count = c;
	}

	public char getType() {
		return orb;
	}

	public int getCount() {
		return count;
	}

	public char getOrb() {
		return orb;
	}

	public boolean eq(Match other) {
		if (orb == other.orb && count == other.count) {
			return true;
		}

		return false;
	}
}
