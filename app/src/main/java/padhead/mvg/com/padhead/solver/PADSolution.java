package padhead.mvg.com.padhead.solver;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Container for a full board solution
 */
public class PADSolution {
	private float weight;
	private boolean done;
	private PADBoard board;
	private RowColumn cursor;
	private ArrayList<Integer> path;
	private ArrayList<Match> matches;
	private RowColumn initCursor;

	//TODO: [DONE] make_solution impl.
	public PADSolution(PADBoard b) {
		done = false;
		weight = 0;
		board = b;
		cursor = new RowColumn(0, 0);
		path = new ArrayList<Integer>();
		matches = new ArrayList<Match>();
	}

	//TODO: [DONE] copy_solution_with_cursor impl.
	public PADSolution(PADSolution sol, int i, int j) {
		board = sol.getBoard().copy();
		cursor = new RowColumn(i, j);
		initCursor = new RowColumn(i, j);
		path = new ArrayList<Integer>(sol.getPath());
		done = sol.isDone();
		weight = sol.getWeight();
		matches = new ArrayList<Match>();
	}

	public PADSolution(PADSolution sol, int i, int j, RowColumn initc) {
		board = sol.getBoard().copy();
		cursor = new RowColumn(i, j);
		initCursor = initc.copy();
		path = new ArrayList<Integer>();
		for (Integer itgr : sol.getPath()) {
			path.add(new Integer(itgr.intValue()));
		}
		done = sol.isDone();
		weight = sol.getWeight();
		matches = new ArrayList<Match>();
		for (Match mat : sol.getMatches()) {
			matches.add(mat);
		}
	}

	public void setDone() {
		done = true;
	}

	public boolean isDone() {
		return done;
	}

	// TODO: [DONE]non-shallow copy
	public PADSolution copy() {
		return new PADSolution(this, cursor.row, cursor.col, initCursor);
	}

	public PADBoard getBoard() {
		return board;
	}

	public RowColumn getCursor() {
		return cursor;
	}

	public void setCursor(RowColumn nc) {
		cursor = nc;
	}

	public ArrayList<Integer> getPath() {
		return path;
	}

	public void setWeight(float f) {
		weight = f;
	}

	public float getWeight() {
		return weight;
	}

	public ArrayList<Match> getMatches() {
		return matches;
	}

	public void setMatches(ArrayList<Match> nmatches) {
		matches = nmatches;
	}

	public RowColumn getInitCursor() {
		return initCursor;
	}

	public boolean cmpMatches(PADSolution other) {
		if (this.matches.size() != other.matches.size()) {
			return false;
		}
		Comparator<Match> cmp = new Comparator<Match>() {
			public int compare(Match s1, Match s2) {
				if (s1.getType() < s2.getType()) {
					return -1;
				} else if (s1.getType() > s2.getType()) {
					return 1;
				}

				if (s1.getCount() < s2.getCount()) {
					return -1;
				} else if (s2.getCount() > s2.getCount()) {
					return 1;
				} else {
					return 0;
				}
			}
		};

		Comparator<Match> cmp2 = new Comparator<Match>() {
			public int compare(Match s1, Match s2) {
				if (s1.getType() < s2.getType()) {
					return -1;
				} else if (s1.getType() > s2.getType()) {
					return 1;
				}

				if (s1.getCount() < s2.getCount()) {
					return -1;
				} else if (s2.getCount() > s2.getCount()) {
					return 1;
				} else {
					return 0;
				}
			}
		};

		Collections.sort(matches, cmp);

		Collections.sort(other.matches, cmp2);
		String s1 = "";
		String s2 = "";
		for (int i = 0; i < matches.size(); i++) {
			s1 += matches.get(i).getType() + "" + matches.get(i).getCount() + " ";

		}
		for (int i = 0; i < other.matches.size(); i++) {
			s2 += other.matches.get(i).getType() + "" + other.matches.get(i).getCount() + " ";
		}

		return s1.equals(s2);
	}

	public String toString() {
		DecimalFormat df = new DecimalFormat("##.##");
		df.setRoundingMode(RoundingMode.DOWN);
		String s = "";
		s += "w: " + (df.format(weight).length() <= 4 ? df.format(weight) + " " : df.format(weight)) + "\t Start: (" + initCursor.row + ", " + initCursor.col
				+ ")\tPath:\t";
		for (Integer i : path) {
			s += "  " + dir(i);
		}
		s += "\n\t\t";
		s += "Combos: ";
		for (Match m : matches) {
			s += m.getCount() + "" + m.getType() + "   ";
		}
		s += '\n';
		return s;
	}

	private String dir(int i) {
		if (i == 0) {
			return "rt";
		} else if (i == 1) {
			return "up/rt";
		} else if (i == 2) {
			return "dn";
		} else if (i == 3) {
			return "dn/lt";
		} else if (i == 4) {
			return "lt";
		} else if (i == 5) {
			return "up/lt";
		} else if (i == 6) {
			return "up";
		} else {
			return "up/rt";
		}
	}

}
