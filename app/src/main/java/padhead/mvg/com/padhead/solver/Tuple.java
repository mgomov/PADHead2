package padhead.mvg.com.padhead.solver;

public class Tuple<X, Y> {
	public final X x;
	public final Y y;

	public Tuple(X x, Y y) {
		this.x = x;
		this.y = y;
	}

	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	public Tuple<X, Y> cpy() {
		return new Tuple<X, Y>(x, y);
	}
}
