package padhead.mvg.com.padhead.solver;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A solve state, part of an iteration of solve states... contains solutions that get iterated on
 */
public class PADSolveState {
	public PADSolveState(int mxln, boolean allow8Dir, ArrayList<PADSolution> slns, HashMap wgts) {
		maxLength = mxln;
		if (allow8Dir) {
			dirStep = 1;
		} else {
			dirStep = 2;
		}

		solutions = slns;
		weights = wgts;
	}

	private int maxLength;
	private int dirStep;
	private int p;
	private ArrayList<PADSolution> solutions;
	private HashMap weights;

	public int maxLength() {
		return maxLength;
	}

	public int p() {
		return p;
	}

	public void incrementP() {
		p++;
	}

	public void setSolutions(ArrayList<PADSolution> slns) {
		solutions = slns;
	}

	public ArrayList<PADSolution> getSolutions() {
		return solutions;
	}

	public HashMap weights() {
		return weights;
	}

	public int dirStep() {
		return dirStep;
	}
}
