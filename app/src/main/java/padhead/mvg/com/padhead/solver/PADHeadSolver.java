package padhead.mvg.com.padhead.solver;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import java.util.HashMap;

import padhead.mvg.com.padhead.service.PADHeadOverlayService;

/**
 * A port of the "pndopt" project on github by kennytm
 *
 * https://github.com/kennytm
 *
 * and
 *
 * https://github.com/kennytm/pndopt */
public class PADHeadSolver {
	public boolean debug = true | PADHeadOverlayService.debugAll;

	int rows;
	int cols;

	/** Multiplier for multiple orbs */
	float multiOrbBonus = 0.25f;

	/** Multiplier for combos */
	float comboBonus = 0.25f;

	/** Threshhold for solutions */
	int maxSolutions;

	/** Initial board state */
	PADBoard globalBoard;

	/** Threshhold for solution lengths */
	int maxPathLength;

	/** Toggle for allowing 8directional movement in solution paths */
	boolean allow8Dir;

	/** Map of weights */
	protected HashMap weights;

	/** Accessor for the final solution set */
	public ArrayList<PADSolution> finalSolutions;

	/** Threshhold for the final output of solutions*/
	private int maxSimplifiedSolutions;

	/** Called to initiate solving the given input */
	public void solve(String input, int maxPathLengthL, boolean allow8Dir , ArrayList<OrbWeight> externWeights, int maxSimp, int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		globalBoard = new PADBoard();
		maxSolutions = rows * cols * 8 * 2;
		globalBoard = new PADBoard(input);
		maxPathLength = maxPathLengthL - 1;
		this.allow8Dir = allow8Dir;
		maxSimplifiedSolutions = maxSimp;
		weights = new HashMap();

		for(OrbWeight w : externWeights) {
			weights.put(new Character(w.getType()), new Tuple<Float, Float>(new Float(w.getNormalWeight()), new Float(w.getMassWeight())));
		}
		debug = false;

		solveBoard(globalBoard);
	}

	/** Solve the given board and iterate on it */
	protected void solveBoard(PADBoard board) {
		PADSolution[] solutions = new PADSolution[rows * cols];
		PADSolution seedSolution = new PADSolution(board.copy());

		// evaluate the solution in its current state and populate it with its current combo values
		inPlaceEvaluateSolution(seedSolution);

		// generate a solution for each member of the board, i.e. each tile has an optimal solution
		// which it can have
		for (int i = 0, s = 0; i < rows; ++i) {
			for (int j = 0; j < cols; ++j, ++s) {
				solutions[s] = new PADSolution(seedSolution, i, j);
			}
		}

		// load the solutions into a list
		ArrayList<PADSolution> solns = new ArrayList<PADSolution>();
		for (int i = 0; i < rows * cols; i++) {
			solns.add(solutions[i]);
		}

		// initialize the solve state, which will be the collection that gets iterated repeatedly
		PADSolveState solveState = new PADSolveState(maxPathLength, allow8Dir, solns, weights);

		solveBoardStep(solveState);
	}

	/** Solves one 'board step', i.e. processes a movement for each solution in the current solve state*/
	protected void solveBoardStep(PADSolveState solveState) {

		// if the number of iterations, i.e. moves, has surpassed the user-defined maximum number
		// of moves, the solver is done
		if (solveState.p() >= solveState.maxLength()) {
			finish(solveState.getSolutions());
			return;
		}

		// add to the iteration counter and evolve each solution in the solve state solution set
		solveState.incrementP();
		solveState.setSolutions(evolveSolutions(solveState.getSolutions(), solveState.dirStep()));

		// iterate again on the solve state
		solveBoardStep(solveState);
	}

	/** Simplifies solutions (i.e. consolidate duplicates) and put the final output into finalSolutions */
	protected void finish(ArrayList<PADSolution> solutions) {
		solutions = simplifySolutions(solutions);
		finalSolutions = solutions;
	}

	/** Removes duplicate solutions from the given list */
	protected ArrayList<PADSolution> simplifySolutions (ArrayList<PADSolution> solutions){
		ArrayList<PADSolution> simplified = new ArrayList<PADSolution>();
		simplified.add(solutions.get(0));

		for(int i = 0; i < solutions.size(); i++){
			boolean add  = true;
			PADSolution sol1 = solutions.get(i);
			for(int j = 0; j < simplified.size(); j++){
				PADSolution sol2 = simplified.get(j);
				if((sol1.cmpMatches(sol2))){
					add = false;
					break;
				}
			}
			if(add){
				simplified.add(solutions.get(i));
				if(simplified.size() > maxSimplifiedSolutions){
					return simplified;
				}
			}
		}

		return simplified;
	}

	/** Iterates each set of solutions */
	private ArrayList<PADSolution> evolveSolutions(ArrayList<PADSolution> solutions, int dirStep) {
		ArrayList<PADSolution> newSolutions = new ArrayList<PADSolution>();
		for (PADSolution s : solutions) {
			if (!s.isDone()) {
				// move in each direction and tries the solutions
				// dirstep is defined earlier by the 8dir toggle
				for (int dir = 0; dir < 8; dir += dirStep) {
					if (canMoveOrbInSolution(s, dir)) {
						PADSolution solution = s.copy();
						inPlaceSwapOrbInSolution(solution, dir);
						inPlaceEvaluateSolution(solution);
						newSolutions.add(solution);
					}
				}
				s.setDone();
			}
		}


		for(PADSolution sol : newSolutions){
			solutions.add(sol);
		}

		// sort the solutions by weight to have more desired solutions come to the top, i.e. solutions
		// that the user wants based on the weights given to an orb type
		Collections.sort(solutions, new Comparator<PADSolution>() {
			public int compare(PADSolution s1, PADSolution s2) {
				if (s1.getWeight() > s2.getWeight())
					return -1;
				else if (s1.getWeight() < s2.getWeight())
					return 1;
				else
					return 0;
			}
		});

		// slice off all the solutions outside of the maximum range of solutions
		int sliceidx = maxSolutions;
		if(sliceidx > solutions.size()){
			sliceidx = solutions.size();
		}

		return new ArrayList<PADSolution>(solutions.subList(0, sliceidx));
	}

	/** Check if there can be movement in a direction... essentially, prevent moving backwards and
	 * getting stuck in a loop and prevent moving off of the board */
	private boolean canMoveOrbInSolution(PADSolution solution, int dir) {
		if (solution.getPath().size() > 0) {
			if (solution.getPath().get(solution.getPath().size() - 1) == (dir + 4) % 8) {
				return false;
			}
		}

		return canMoveOrb(solution.getCursor(), dir);
	}

	/** Prevent moving off of the board */
	private boolean canMoveOrb(RowColumn rc, int dir) {
		switch (dir) {
			case 0:
				return rc.col < cols - 1;
			case 1:
				return rc.row < rows - 1 && rc.col < cols - 1;
			case 2:
				return rc.row < rows - 1;
			case 3:
				return rc.row < rows - 1 && rc.col > 0;
			case 4:
				return rc.col > 0;
			case 5:
				return rc.row > 0 && rc.col > 0;
			case 6:
				return rc.row > 0;
			case 7:
				return rc.row > 0 && rc.col < cols - 1;
		}
		return false;
	}

	/** Swap two orbs, e.g. when moving an orb in a solution, two orbs would get swapped. Update the
	 * solution's cursor as well */
	private void inPlaceSwapOrbInSolution(PADSolution solution, int dir) {
		inPlaceOrbSwap(solution.getBoard(), solution.getCursor(), dir);
		solution.setCursor(__rc);
		solution.getPath().add(dir);
	}

	/** A tuple return artifact from the javascript port; returned from inPlaceOrbSwap, utilized in
	 * inPlaceOrbSwapInSolution */
	private RowColumn __rc;

	/** Swap two orbs on the board */
	private PADBoard inPlaceOrbSwap(PADBoard board, RowColumn rc, int dir) {
		RowColumn oldRC = rc.copy();
		inPlaceMoveRc(rc, dir);
		char originalType = board.getBoard()[oldRC.row][oldRC.col];
		board.getBoard()[oldRC.row][oldRC.col] = board.getBoard()[rc.row][rc.col];
		board.getBoard()[rc.row][rc.col] = originalType;
		__rc = rc;
		return board;
	}

	/** Moves a RowColumn in the specified direction */
	void inPlaceMoveRc(RowColumn rc, int dir) {
		switch (dir) {
			case 0:
				rc.col += 1;
				break;
			case 1:
				rc.row += 1;
				rc.col += 1;
				break;
			case 2:
				rc.row += 1;
				break;
			case 3:
				rc.row += 1;
				rc.col -= 1;
				break;
			case 4:
				rc.col -= 1;
				break;
			case 5:
				rc.row -= 1;
				rc.col -= 1;
				break;
			case 6:
				rc.row -= 1;
				break;
			case 7:
				rc.row -= 1;
				rc.col += 1;
				break;
		}
	}

	/** tuple return artifact form the javascript port; returned from findMatches, utilized in
	 * inPlaceEvaluateSolution and other subsequent places*/
	private PADBoard __matchBoard;

	/** Evaluates a given solution for combos and calculates the weights*/
	public PADBoard inPlaceEvaluateSolution(PADSolution solution) {
		PADBoard currentBoard = solution.getBoard().copy();

		// list of all the matches in the solution, indicator of the total matches in the solution
		ArrayList<Match> allMatches = new ArrayList<Match>();

		while (true) {
			ArrayList<Match> matches = findMatches(currentBoard);

			// no matches left to evaluate, so, done
			if (matches.size() == 0) {
				break;
			}

			// remove the matches in the board, then drop all of the orbs down to take their new place
			inPlaceRemoveMatches(currentBoard, __matchBoard);
			inPlaceDropEmptySpaces(currentBoard);

			// add found matches to allMatches
			for(Match m : matches){
				allMatches.add(m);
			}
		}

		// update the solution to reflect the newly found matches
		solution.setWeight(computeWeight(allMatches));
		solution.setMatches(allMatches);
		return currentBoard;
	}

	/** Removes all matches in the solution and drops the empty spaces, for use solely by the service
	 * in order to expedite recreation of the new board state by the user
	 *
	 * NOT used in the actual solver algorithm */
	public PADBoard dropMatches(PADSolution solution) {
		rows = PADHeadOverlayService.rows;
		cols = PADHeadOverlayService.cols;
		if(debug) Log.d("PADHeadSolver debug", "dropMatches");
		PADBoard currentBoard = solution.getBoard().copy();
		while (true) {
			ArrayList<Match> matches = findMatches(currentBoard);
			if (matches.size() == 0) {
				break;
			}
			inPlaceRemoveMatches(currentBoard, __matchBoard);
			inPlaceDropEmptySpaces(currentBoard);
		}
		return currentBoard;
	}

	/** Remove all non-blank orbs from the provided matchBoard, as it's a board containing only matches */
	PADBoard inPlaceRemoveMatches(PADBoard board, PADBoard matchBoard) {
		if(debug) Log.d("PADHeadSolver debug", "inPlaceRemoveMatches");
		for (int i = 0; i < rows; ++i) {
			for (int j = 0; j < cols; ++j) {
				if (matchBoard.getBoard()[i][j] != '\u0000') {
					board.getBoard()[i][j] ='u';
				}
			}
		}
		return board;

	}

	/** Drop all the orbs vertically in the given board, used after removing matches from a board
	 * to simulate natural game progress */
	PADBoard inPlaceDropEmptySpaces(PADBoard board) {
		if(debug) Log.d("PADHeadSolver debug", "inPlaceDropEmptySpaces");
		for (int j = 0; j < cols; ++j) {
			int desti = rows - 1;
			for (int srci = rows - 1; srci >= 0; --srci) {
				if (board.getBoard()[srci][j] != 'u') {
					board.getBoard()[desti][j] = (board.getBoard()[srci][j]);
					--desti;
				}
			}
			for (; desti >= 0; --desti) {
				board.getBoard()[desti][j] = 'u';
			}
		}
		return board;
	}

	/** Compute the weights for the given matches */
	public float computeWeight(ArrayList<Match> matches) {
		float totalWeight = 0f;
		for (Match m : matches) {
			float baseWeight = 0f;
			// determine whether the weighting scheme used should be based on the mass weight or
			// normal amount weight
			if (m.getCount() >= 5) {
				baseWeight = ((Tuple<Float, Float>)(weights.get(new Character(m.getType())))).x;
			} else {
				baseWeight = ((Tuple<Float, Float>)(weights.get(new Character(m.getType())))).y;
			}

			// factor in the user-defined multiorb bonus
			float lmultiOrbBonus = (m.getCount() - 3f) * multiOrbBonus + 1f;

			// sum the weight
			totalWeight += lmultiOrbBonus * baseWeight;
		}
		float lcomboBonus = (matches.size() - 1f) * comboBonus + 1f;

		return totalWeight * lcomboBonus;
	}

	/** Find all orb matches in the given board */
	public ArrayList<Match> findMatches(PADBoard board) {
		PADBoard matchBoard = new PADBoard();
		if(debug) Log.d("findMatches", "debugging findMatches");
		char prev1Orb, prev2Orb, currentOrb;

		// 3 orbs in a row
		prev1Orb = ('u');
		prev2Orb = ('u');
		currentOrb = ('u');

		// check for vertical matches and populate the matchBoard with them
		for (int i = 0; i < rows; ++i) {
			prev1Orb = ('u');
			prev2Orb = ('u');
			for (int j = 0; j < cols; ++j) {
				currentOrb = (board.getBoard()[i][j]);
				if(debug) Log.d("findMatches", "" + currentOrb);
				if (prev1Orb == (prev2Orb) && prev2Orb == (currentOrb) && currentOrb != 'u') {
					matchBoard.getBoard()[i][j] = currentOrb;
					matchBoard.getBoard()[i][j - 1] = currentOrb;
					matchBoard.getBoard()[i][j - 2] = currentOrb;
					if(debug) Log.d("findMatches", "Matchboard marked");
				}
				prev1Orb = prev2Orb;
				prev2Orb = currentOrb;
			}
		}

		// check for horizontal matches and populate the matchBoard with them
		for (int j = 0; j < cols; ++j) {
			prev1Orb  = 'u';
			prev2Orb = 'u';
			for (int i = 0; i < rows; ++i) {
				currentOrb = board.getBoard()[i][j];
				if (prev1Orb == (prev2Orb) && prev2Orb == (currentOrb) && currentOrb != 'u') {
						matchBoard.getBoard()[i][j] = currentOrb;
						matchBoard.getBoard()[i - 1][j] = currentOrb;
						matchBoard.getBoard()[i - 2][j] = currentOrb;
				}
				prev1Orb = prev2Orb;
				prev2Orb = currentOrb;
			}
		}

		PADBoard scratchBoard = matchBoard.copy();

		// populate the list of matches by processing the matches on the matchBoard and taking the type
		// from the locations on the matchBoard
		ArrayList<Match> matches = new ArrayList<Match>();
		for (int i = 0; i < rows; ++i) {
			for (int j = 0; j < cols; ++j) {
				currentOrb  = scratchBoard.getBoard()[i][j];
				if (currentOrb != '\u0000' /*&& currentOrb.getType() != ('u')*/) {
					ArrayList<RowColumn> stack = new ArrayList<RowColumn>();
					stack.add(new RowColumn(i, j));
					int count = 0;
					while (stack.size() > 0) {
						RowColumn n = stack.remove(stack.size() - 1);
						if (scratchBoard.getBoard()[n.row][n.col] == currentOrb) {
							count++;
							scratchBoard.getBoard()[n.row][n.col] = '\u0000';
							if (n.row > 0) {
								stack.add(new RowColumn(n.row - 1, n.col));
							}
							if (n.row < rows - 1) {
								stack.add(new RowColumn(n.row + 1, n.col));
							}
							if (n.col > 0) {
								stack.add(new RowColumn(n.row, n.col - 1));
							}
							if (n.col < cols - 1) {
								stack.add(new RowColumn(n.row, n.col + 1));
							}
						}
					}
					matches.add(new Match(currentOrb, count));
				}
			}
		}

		// tuple return artifact from JS port
		__matchBoard = matchBoard;
		return matches;
	}
}
