package embedding.nearestNeighbors;

import embedding.measures.Measure;
import embedding.shortestPaths.AdjacencyList;

/**
 * A {@link KNearestNeighborGraph} object can be used to find the k most similar
 * objects regarding a measure for every single object. All the objects use are
 * provided as an array.
 *
 * <p>
 * Implementations should try to reuse previous calculations so that calculating
 * the neighborhood graph a second time for a different k is quicker than
 * calculating that graph from scratch.
 *
 * @author Anton Ballmaier
 *
 * @param <T> the type of objects to be compared. The used {@link Measure} must
 *            also be applicable for this type.
 */
public abstract class KNearestNeighborGraph<T> {
	/**
	 * Neighborhood size
	 */
	protected int k;

	/**
	 * The measure used to compare the objects
	 */
	protected Measure<T> measure;

	/**
	 * Objects to construct the neighborhood graph out of
	 */
	protected T[] nodes;

	/**
	 * Constructs a new {@link KNearestNeighborGraph} object from the given objects
	 * and measure. This can construct a k nearest neighbor graph.
	 *
	 * @param k       neighborhood size
	 * @param nodes   objects to be compared
	 * @param measure measure to compare the given objects
	 */
	public KNearestNeighborGraph(int k, T[] nodes, Measure<T> measure) {
		this.nodes = nodes;
		this.measure = measure;
		this.k = k;
	}

	/**
	 * Returns the current neighborhood size
	 *
	 * @return the current neighborhood size
	 */
	public int getNeighborhoodSize() {
		return k;
	};

	/**
	 * Constructs a nearest neighbor graph and returns it as {@link AdjacencyList}.
	 *
	 * @return the nearest neighbor graph
	 */
	public abstract AdjacencyList nnGraph();

	/**
	 * Changes the current neighborhood size
	 *
	 * @param k the new neighborhood size
	 * @throws IllegalArgumentException if the given neighborhood size is less than
	 *                                  1
	 */
	public void setNeighborhoodSize(int k) {
		if (k < 1) {
			throw new IllegalArgumentException("The neighborhood size must be at least 1.");
		}
		this.k = k;
	}

}
