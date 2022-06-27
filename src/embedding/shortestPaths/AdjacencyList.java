package embedding.shortestPaths;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * An {@link AdjacencyList} represents an undirected weighted graph. This
 * representation is best suited for sparse graphs.
 *
 * <p>
 * The total amount of nodes must be decided at the time of creation. The
 * vertices of a graph with n vertices are the integers 0&leq;i&lt;n. In
 * conjunction with an array of any type this can be used to represent a graph
 * with the elements of that array as vertices by using their indices in the
 * {@link AdjacencyList}.
 *
 * @author Anton Ballmaier
 *
 */
public class AdjacencyList {
	/**
	 * Number of nodes in the represented graph
	 */
	public final int length;

	/**
	 * Representation of the graphs edges.
	 * <center><code>data[i].get(j)</code></center> is the weight of the edge from i
	 * to j.
	 */
	private final Map<Integer, Double>[] data;

	/**
	 * Constructs a new {@link AdjacencyList} object using the given number of
	 * vertices. The vertices of a graph with n vertices are the integers
	 * 0&leq;i&lt;n.
	 *
	 * @param vertices the number of vertices
	 */
	@SuppressWarnings("unchecked")
	public AdjacencyList(int vertices) {
		this.length = vertices;
		data = new Map[vertices];
		for (int i = 0; i < vertices; i++) {
			data[i] = new LinkedHashMap<>();
		}
	}

	/**
	 * Adds an edge to the represented graph weighted with the given distance. Since
	 * the graph is undirected, the order of the first two arguments doesn't make a
	 * difference. If the distance of an aready existing edge is set, the distance
	 * is updated. Setting a distance to <code>Double.POSITIVE_INFINITY</code> will
	 * result in removing that edge.
	 *
	 * @param from     first vertex of the edge
	 * @param to       second vertex of the edge
	 * @param distance weight / distance of the edge
	 * @throws IllegalArgumentException if the given distance is negative
	 * @throws IllegalArgumentException if the given distance is not 0 and the
	 *                                  vertices are the same
	 */
	public void addEdge(int from, int to, double distance) {
		if (distance < 0) {
			throw new IllegalArgumentException("Distances in the represented graph cannot be negative.");
		}
		if (from == to) {
			if (distance != 0) {
				throw new IllegalArgumentException("The distance from one vertex to itself cannot differ from 0.");
			}
			return;
		}
		if (Double.isInfinite(distance)) {
			removeEdge(from, to);
			return;
		}
		data[from].put(to, distance);
		data[to].put(from, distance);
	}

	/**
	 * Returns the weight / distance of an edge. The distance from one vertex to the
	 * same is always 0.
	 *
	 * @param from first vertex of the edge
	 * @param to   second vertex of the edge
	 * @return the weight / distance of the edge between the given vertices or
	 *         <code>Double.POSITIVE_INFINITY</code> if no such edge is defined.
	 */
	public double distance(int from, int to) {
		if (from == to) {
			return 0;
		}
		final Double distance = data[from].get(to);
		if (distance == null) {
			return Double.POSITIVE_INFINITY;
		}
		return distance;
	}

	/**
	 * Returns the neighborhood of a vertex. A vertex is defined as a neighbor of
	 * the given vertex if there is a edge connecting the two vertices.
	 *
	 * @param vertex the vertex
	 * @return the set of neighbors of the given vertex
	 */
	public Set<Integer> neighbors(int vertex) {
		return data[vertex].keySet();
	}

	/**
	 * Removes an edge if present.
	 *
	 * @param from first vertex of the edge
	 * @param to   second vertex of the edge
	 */
	public void removeEdge(int from, int to) {
		data[from].remove(to);
		data[to].remove(from);
	}

	/**
	 * Constructs a <code>double[][]</code> array from this {@link AdjacencyList}
	 * containing the edge weights of every edge in the graph. In any case the entry
	 * of the matrix will be:
	 * <center><code>toAdjacencyMatrix()[i][j] == distance(i, j)</code></center>
	 *
	 * @return the quadratic matrix of distances
	 */
	public double[][] toAdjacencyMatrix() {
		final double[][] matrix = new double[length][length];
		for (int from = 0; from < length; from++) {
			for (int to = 0; to < length; to++) {
				matrix[from][to] = distance(from, to);
			}
		}
		return matrix;
	}
}
