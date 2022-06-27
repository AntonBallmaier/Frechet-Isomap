package frechetDistance;

/**
 * The integral discrete fr&#xE9;chet distance between two {@link Polygon}s.
 *
 * @author Anton Ballmaier
 * @see Polygon
 */
public class IntegralDiscreteFrechet extends AnyDiscreteFrechet {
	private static IntegralDiscreteFrechet instance;

	/**
	 * Creates, or if already present just returns the global instance of this
	 * class.
	 *
	 * @return the single instance
	 */
	public static IntegralDiscreteFrechet getInstance() {
		if (instance == null) {
			instance = new IntegralDiscreteFrechet();
		}
		return instance;
	}

	/**
	 * Constructs a new {@link DiscreteFrechet} object.
	 */
	private IntegralDiscreteFrechet() {
	}

	/**
	 * Accumulates the given values by returning their sum. The inherited method
	 * <code>distance</code> will therefore yield the integral discrete fr&#xE9;chet
	 * distance.
	 *
	 * @param a the minimal cost of any preceding pair of vertices
	 * @param b the distance of two vertices
	 * @return the sum of the given values
	 */
	@Override
	protected double accumulate(double a, double b) {
		return a + b;
	}
}
