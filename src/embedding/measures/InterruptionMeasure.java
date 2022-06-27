package embedding.measures;

/**
 * An {@link InterruptionMeasure} imposes a distance function on some collection
 * of objects. {@link Measure}s can be used to define the dissimilarity of
 * specific objects.
 *
 * <p>
 * In the case of the {@link InterruptionMeasure}, only the interrupting
 * distance calculation must be implemented. The exact distance calculation
 * defaults to the capped distance calculation with infinity as the maximum.
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
@FunctionalInterface
public interface InterruptionMeasure<T> extends Measure<T> {
	@Override
	public default double distance(T a, T b) {
		return distanceCapped(a, b, Double.POSITIVE_INFINITY);
	}
}