package embedding.measures;

/**
 * A {@link Measure} imposes a distance function on some collection of objects.
 * {@link Measure}s can be used to define the dissimilarity of specific objects.
 *
 * <p>
 * The distance imposed by a {@link Measure} &delta; on a set of elements
 * <i>S</i> is said to be consistent with the following rules for all <i>a</i>,
 * <i>b</i> &isin; <i>S</i>:
 * <ul>
 * <li>&delta;(a,a) = 0</li>
 * <li>&delta;(a,b) &geq; 0</li>
 * <li>&delta;(a,b) = &delta;(b,a)</li>
 * </ul>
 *
 * <p>
 * In case the exact distance of two objects is does only matter if it is less
 * or equal a previously known threshold, the measurement can also be capped at
 * a maximum value. In many cases this allows for more efficient calculations.
 *
 * @version 1.0
 * @author Anton Ballmaier
 *
 * @param <T> The type of objects that may be compared by this measure
 */
public interface Measure<T> {
	/**
	 * Calculates the distance of the given objects.
	 *
	 * @param a the first object
	 * @param b the second object
	 * @return their distance
	 */
	public double distance(T a, T b);

	/**
	 * Calculates the distance of the given objects if small enough.
	 *
	 * @param a   the first object
	 * @param b   the second object
	 * @param max the maximum distance of interest
	 * @return their distance, or <code>Double.POSITIVE_INDIFINITY</code> in case
	 *         their distance is larger than the given maximum
	 */
	public double distanceCapped(T a, T b, double max);
}
