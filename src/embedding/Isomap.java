package embedding;

import embedding.measures.InterruptionMeasure;
import embedding.measures.Measure;
import embedding.nearestNeighbors.ConnectedComponents;
import embedding.nearestNeighbors.KNearestNeighborGraph;
import embedding.nearestNeighbors.NearestNeighborDescend;
import embedding.shortestPaths.AdjacencyList;
import embedding.shortestPaths.AllPairShortestPaths;
import embedding.shortestPaths.DijkstraAPSP;
import embedding.shortestPaths.FloydWarshallAPSP;

/**
 * An {@link Isomap} is an {@link Embedder} that uses the Isomap algorithm for
 * distance calculations. This algorithm measures the distances of the element
 * along the neighborhood graph of the elements.
 *
 * <p>
 * This implementation currently only uses k-nearest neighbor graphs. The
 * parameter k bust be given in the constructor and can be changed later.
 *
 * <p>
 * This implementation attempts to reuse as much of previous calculations as
 * reasonable. This results in much faster computations in certain cases. If you
 * want to calculate a lot of embeddings, you should do that in the following
 * order to use this speedup most effectively:
 *
 *
 *
 * <pre>
 * for (int k = kMax; k &gt;= mMin; k -= ...) {
 * 	this.setNearestNeighborCount(k);
 * 	for (int l = lMax; l&gt;= lMin; l -= ...) {
 * 		this.setLandmarkCount(l);
 * 		embed(dimension);
 * 	}
 * }
 * </pre>
 *
 *
 *
 * @author Anton Ballmaier
 *
 * @param <T> the type of objects to be embedded. The used {@link Measure} must
 *            also be applicable for this type.
 */
public class Isomap<T> extends Embedder<T> {
	/**
	 * Saved geodesic distances based on the current value of k. Gets flushed if the
	 * landmark count is changed and might therefore be <code>null</code>.
	 */
	private double[][] geodesicDistances;

	/**
	 * Parameter for the k-nearest neighbor graph construction. Every element is
	 * connected with the k most similar elements.
	 */
	private int k;

	/**
	 * The object used for k-nearest neighbor graph constructor.
	 */
	private final KNearestNeighborGraph<T> knn;

	/**
	 * Constructs a new {@link Isomap} object using the given elements,
	 * {@link Measure} and nearest neighbor count.
	 *
	 * <p>
	 * No landmarks will be used by default. See
	 * {@link Embedder#Embedder(Object[], Measure)} more details of the default
	 * landmark settings.
	 *
	 * @param elements the elements that are meant to be embedded
	 * @param measure  the measure used for the distance calculations
	 * @param k        the number of nearest neighbors every element is meant to be
	 *                 connected to
	 * @throws IllegalArgumentException if any of the arguments is <code>null</code>
	 */
	public Isomap(T[] elements, Measure<T> measure, int k) {
		super(elements, measure);
		knn = new NearestNeighborDescend<>(k, this.elements, this.measure);
	}

	/**
	 * Constructs a new {@link Isomap} object using the given elements,
	 * {@link Measure}, nearest neighbor and landmark count. Landmarks will be used
	 * by default.
	 *
	 * @param elements  the elements that are meant to be embedded
	 * @param measure   the measure used for the distance calculations
	 * @param k         the number of nearest neighbors every element is meant to be
	 *                  connected to
	 * @param landmarks the number of landmark points
	 * @throws IllegalArgumentException if any of the arguments is <code>null</code>
	 */
	public Isomap(T[] elements, Measure<T> measure, int k, int landmarks) {
		this(elements, measure, k);
		this.setLandmarkCount(landmarks);
		this.useLandmarks(true);
	}

	/**
	 * Returns the nearest neighbor count currently used.
	 *
	 * @return the nearest neighbor count
	 */
	public int getNearestNeighborCount() {
		return k;
	}

	/**
	 * Sets the nearest neighbor count (neighborhood size of each element) to the
	 * given value. If this count is changed, all saved geodesic distances are
	 * deleted and must be recalculated for the new value.
	 *
	 * @param k the new nearest neighbor count
	 * @throws IllegalArgumentException if the given nearest neighbor count is
	 *                                  greater than the maximum neighborhood size
	 *                                  (number of elements-1) or less than 1.
	 * @see Isomap#geodesicDistances
	 */
	public void setNearestNeighborCount(int k) {
		if (k == this.k) {
			return;
		}
		if (k > this.elements.length - 1) {
			throw new IllegalArgumentException(
					String.format("Cannot have k=%d using only %d elements", k, elements.length));
		}
		if (k < 1) {
			throw new IllegalArgumentException("Neighborhood size count must be at least 1");
		}
		this.k = k;
		this.knn.setNeighborhoodSize(k);
		this.geodesicDistances = null; // Shortest paths need to be recalculated
	}

	/**
	 * Calculates the geodesic distances along the neighborhood graph between the
	 * starting points and all other elements.
	 *
	 * <p>
	 * The first entries of <code>elements</code> are used as starting points. The
	 * returned distances are positive and symmetric, since the neighborhood graph
	 * is mirrored after construction.
	 *
	 * @param startingPoints the number of starting points
	 * @return the geodesic distances from starting points to all other entries.
	 *         <code>requiredDistances(startingPoints)[i][j]</code> is the distance
	 *         from the j<sup>th</sup> starting point to the i<sup>th</sup> element.
	 * @see KNearestNeighborGraph#nnGraph()
	 * @see AllPairShortestPaths#shortestPaths()
	 */
	@Override
	protected double[][] requiredDistances(int startingPoints) {
		// Only rebuild neighborhood graph and shortest paths if needed. Previous
		// calculations might make these calculations redundant.
		if (geodesicDistances == null || geodesicDistances[0].length != startingPoints) {

			// the knn object also uses previous calculations if possible.
			final AdjacencyList adjacency = knn.nnGraph();

			// Join components
			final InterruptionMeasure<Integer> indexMeasure = (a, b, max) -> measure.distanceCapped(elements[a],
					elements[b], max);
			new ConnectedComponents(adjacency).connect(indexMeasure);

			if (elements.length > 210 || startingPoints < elements.length) {
				geodesicDistances = new DijkstraAPSP(adjacency).shortestPaths(startingPoints);
			} else {
				geodesicDistances = new FloydWarshallAPSP(adjacency).shortestPaths();
			}
		}
		return geodesicDistances;
	}
}