package embedding.nearestNeighbors;

import java.util.PriorityQueue;

import embedding.measures.Measure;
import embedding.shortestPaths.AdjacencyList;

public class NearestNeighborBruteForce<T> extends KNearestNeighborGraph<T> {

	public NearestNeighborBruteForce(int k, T[] nodes, Measure<T> measure) {
		super(k, nodes, measure);
	}

	@Override
	public AdjacencyList nnGraph() {
		final AdjacencyList adjacency = new AdjacencyList(nodes.length);
		final double[] distances = new double[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			final PriorityQueue<Integer> nearestNeighbors = new PriorityQueue<>(
					(a, b) -> (int) Math.signum(distances[b] - distances[a]));
			for (int j = 0; j < nodes.length; j++) {
				if (i == j) {
					continue;
				}
				distances[j] = measure.distance(nodes[i], nodes[j]);
				nearestNeighbors.add(j);
				if (nearestNeighbors.size() > k) {
					nearestNeighbors.remove();
				}
			}
			while (!nearestNeighbors.isEmpty()) {
				final int j = nearestNeighbors.poll();
				adjacency.addEdge(i, j, distances[j]);
			}
		}
		return adjacency;
	}
}
