package embedding.measures;

/**
 * An {@link DirectMeasure} imposes a distance function on some collection of
 * objects. {@link Measure}s can be used to define the dissimilarity of specific
 * objects.
 *
 * <p>
 * In the case of the {@link DirectMeasure}, only the non-capped distance
 * calculation must be implemented. The capped distance calculation defaults to
 * calculating the distance first and returning infinity if needed. This should
 * be used if interrupting cannot be used to speed up calculations.
 *
 * <p>
 * See {@link Measure} for further information on measures in general.
 *
 *
 * @version 1.0
 * @author Anton Ballmaier
 *
 * @param <T> The type of objects that may be compared by this measure
 */
public interface DirectMeasure<T> extends Measure<T> {
	@Override
	public default double distanceCapped(T a, T b, double max) {
		final double distance = distance(a, b);
		if (max < distance) {
			return Double.POSITIVE_INFINITY;
		} else {
			return distance;
		}
	};
}