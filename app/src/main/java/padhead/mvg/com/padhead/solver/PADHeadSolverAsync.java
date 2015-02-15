package padhead.mvg.com.padhead.solver;

import java.util.ArrayList;

import padhead.mvg.com.padhead.service.AsyncSolve;
import padhead.mvg.com.padhead.service.PADHeadOverlayService;

/**
 * Asynchronous wrapper around the solver, for progress bar hooks
 * Author: Maxim Gomov */
public class PADHeadSolverAsync extends PADHeadSolver{
	protected AsyncSolve async;
	int progress = 0;
	public void solve(String input, int maxPathLengthL, boolean allow8Dir, AsyncSolve asyncTask, ArrayList<OrbWeight> externWeights, int maxSimp, int rows, int cols) {
		async = asyncTask;
		for(OrbWeight w : externWeights){
			System.out.println(w.getType() + " " + w.getNormalWeight() + " " + w.getMassWeight());
		}
		super.solve(input, maxPathLengthL, allow8Dir, externWeights, maxSimp, rows, cols);
	}

	protected void solveBoard(PADBoard board){
		progress += 10;
		async.doPublish(progress);
		super.solveBoard(board);
	}

	protected void solveBoardStep(PADSolveState solveState) {
		progress += 5;
		if(progress > 85){
			progress = 85;
		}
		async.doPublish(progress);
		super.solveBoardStep(solveState);
	}

	protected void finish(ArrayList<PADSolution> solutions){
		progress = 90;
		async.doPublish(progress);
		super.finish(solutions);
		progress = 100;
		async.doPublish(progress);
	}

	protected ArrayList<PADSolution> simplifySolutions(ArrayList<PADSolution> solutions){
		progress = 95;
		async.doPublish(progress);
		return super.simplifySolutions(solutions);
	}
}
