package padhead.mvg.com.padhead.solver;

/**
 * Class to store a row/column pair
 */
public class RowColumn {
	public int row;
	public int col;

	public RowColumn(int x, int y) {
		row = x;
		col = y;
	}

	public RowColumn copy() {
		return new RowColumn(row, col);
	}

	public boolean eq(RowColumn other) {
		if (this.row == other.row && this.col == other.col) {
			return true;
		}
		return false;
	}
}
