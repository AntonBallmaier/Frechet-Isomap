package frechetDistance;

/**
 * The typical discrete fr&#xE9;chet distance between two {@link Polygon}s.
 *
 * @author Anton Ballmaier
 * @see Polygon
 */
public class DiscreteFrechet extends AnyDiscreteFrechet {
	private static DiscreteFrechet instance;

	/**
	 * Creates, or if already present just returns the global instance of this
	 * class.
	 *
	 * @return the single instance
	 */
	public static DiscreteFrechet getInstance() {
		if (instance == null) {
			instance = new DiscreteFrechet();
		}
		return instance;
	}

	/**
	 * Constructs a new {@link DiscreteFrechet} object.
	 */
	private DiscreteFrechet() {
	}

	/**
	 * Accumulates the given values by returning their maximum. This class's
	 * <code>distance</code> method will therefore yield the typical discrete
	 * fr&#xE9;chet distance.
	 *
	 * @param a the minimal cost of any preceding pair of vertices
	 * @param b the distance of two vertices
	 * @return the maximum of the given values
	 */
	@Override
	protected double accumulate(double a, double b) {
		return Math.max(a, b);
	}
}