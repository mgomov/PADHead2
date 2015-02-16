package padhead.mvg.com.padhead.solver;

import padhead.mvg.com.padhead.service.PADHeadOverlayService;

/**
 * Representation of a board
 */
public class PADBoard {
	private char[][] board;
	public final int rows = PADHeadOverlayService.rows;
	public final int cols = PADHeadOverlayService.cols;

	public PADBoard() {
		board = new char[rows][cols];
	}

	public PADBoard(String in) {
		board = new char[rows][cols];

		int idx = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				board[i][j] = (in.charAt(idx));
				idx++;
			}
		}
	}

	public PADBoard(char[][] obs) {
		board = new char[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				board[i][j] = obs[i][j];
			}
		}
	}

	public char[][] getBoard() {
		return board;
	}

	public PADBoard copy() {
		return new PADBoard(board);
	}


	public String toString() {
		String s = "\nBoard:\n";

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				s += (board[i][j] + "\t");
			}
			s += "\n";
		}

		return s;
	}
}
