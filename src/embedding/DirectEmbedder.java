package embedding;

import java.util.stream.IntStream;

import embedding.measures.Measure;

/**
 * A {@link DirectEmbedder} is an {@link Embedder} that simply uses
 * the measure for directly calculating the distances. Therefore the
 * {@link DirectEmbedder} implements (landmark) MDS.
 *
 * <p>
 * This implementation can reuses all previous distance calculations. This
 * results in much faster computations if using the {@link DirectEmbedder}
 * multiple times. However, this can cause an instance to save a lot of
 * unnecessary data.
 *
 * @author Anton Ballmaier
 *
 * @param <T>the type of objects to be embedded. The used {@link Measure}
 *               must also be applicable for this type.
 */
public class DirectEmbedder<T> extends Embedder<T> {
	/**
	 * All previously calculated distances
	 */
	private double[][] savedDistances;

	/**
	 * Constructs a new {@link DirectEmbedder} object using the given elements
	 * and {@link Measure}.
	 *
	 * <p>
	 * No landmarks will be used by default. See
	 * {@link Embedder#Embedder(Object[], Measure)} more details of the default
	 * landmark settings.
	 *
	 * @param elements the elements that are meant to be embedded
	 * @param measure  the measure used for the distance calculations
	 * @throws IllegalArgumentException if any of the arguments is <code>null</code>
	 */
	public DirectEmbedder(T[] elements, Measure<T> measure) {
		super(elements, measure);
		savedDistances = null;
	}

	/**
	 * Constructs a new {@link DirectEmbedder} object using the given elements,
	 * {@link Measure} and landmark count. Landmarks will be used by default.
	 *
	 * @param elements  the elements that are meant to be embedded
	 * @param measure   the measure used for the distance calculations
	 * @param landmarks the number of landmark points
	 * @throws IllegalArgumentException if any of the arguments is <code>null</code>
	 */
	public DirectEmbedder(T[] elements, Measure<T> measure, int landmarks) {
		super(elements, measure, landmarks);
		savedDistances = null;
	}

	/**
	 * Calculated the direct distences between the starting points and all other
	 * elements.
	 *
	 * <p>
	 * The first entries of <code>elements</code> are used as starting points. The
	 * returned distances are positive and symmetric.
	 *
	 * @param startingPoints the number of starting points
	 * @return the direct distances from starting points to all other entries.
	 *         <code>requiredDistances(startingPoints)[i][j]</code> is the distance
	 *         from the j<sup>th</sup> starting point to the i<sup>th</sup> element.
	 */
	@Override
	protected double[][] requiredDistances(int startingPoints) {
		final int precalculated;
		if (savedDistances == null) {
			precalculated = 0;
		} else {
			precalculated = savedDistances[0].length;
		}
		double[][] directDistances;

		if (precalculated != startingPoints) {
			directDistances = new double[elements.length][startingPoints];

			if (precalculated > startingPoints) {
				// Copy distances
				for (int i = 0; i < elements.length; i++) {
					for (int j = 0; j < startingPoints; j++) {
						directDistances[i][j] = savedDistances[i][j];
					}
				}
			} else { // precalculated < startingPoints
				// Copy / measure distances below diagonal

				IntStream.range(0, elements.length).parallel().forEach(i -> {
					int jTo = Math.min(precalculated, i);
					for (int j = 0; j < jTo; j++) {
						directDistances[i][j] = savedDistances[i][j];
					}
					jTo = Math.min(startingPoints, i);
					for (int j = precalculated; j < jTo; j++) {
						directDistances[i][j] = measure.distance(elements[i], elements[j]);
					}
				});

				// Use Symmetry
				for (int i = 0; i < startingPoints; i++) {
					for (int j = i + 1; j < startingPoints; j++) {
						directDistances[i][j] = directDistances[j][i];
					}
				}
				// save new bigger distance matrix
				savedDistances = directDistances;
			}
		} else {
			directDistances = savedDistances;
		}
		return directDistances;
	}
}
