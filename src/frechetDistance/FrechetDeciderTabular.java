package frechetDistance;

/**
 * A decider for the fr&#xE9;chet decision problem using a tabular approach.
 *
 * <p>
 * The decision problem is the question whether the fr&#xE9;chet distance of two
 * polygons is less or equal a given distance. This implementation of a
 * {@link FrechetDecider} uses the typical tabular approach.
 *
 * <p>
 * For that the free space and reachability for every cell of the free space
 * diagram are calculated.
 *
 * @author Anton Ballmaier
 *
 */
public class FrechetDeciderTabular extends FrechetDecider {
	/**
	 * Representation of a single Cell of free space / reachability diagram. A Cell
	 * is a simple container for two <code>double</code> arrays. Each of these
	 * arrays represents an interval and should therefore be of length 2.
	 */
	private static class Cell {
		public double[] bottom;
		public double[] left;
	}

	/**
	 * Constructs a new {@link FrechetDeciderTabular} object for the two given
	 * {@link Polygon}s. This can solve their decision problem for any distance.
	 *
	 * @param p the first polygon
	 * @param q the second polygon
	 * @throws IllegalArgumentException if the given polygons have different
	 *                                  dimensionality
	 */
	public FrechetDeciderTabular(Polygon p, Polygon q) {
		super(p, q);
	}

	@Override
	public boolean decideDistance(double distance) {
		if (endpointsToFar(distance)) {
			return false;
		}

		final Cell[][] reachability = reachabilityDiagram(freeSpaceDiagram(distance));

		// Top right reachable?
		return (reachability[p.length - 1][q.length - 2].left) != null
				&& (reachability[p.length - 1][q.length - 2].left[1] == 1);
	}

	/**
	 * Calculates the free space diagram for a given distance and two polygons.
	 *
	 * <p>
	 * The free space diagram contains information about the positions on both
	 * polygons that are within reach to each other with respect to the defined
	 * distance. The returned {@link Cell}<code>[][]</code> marks the interval of
	 * points on the left and bottom edge for which the distance is within the
	 * defined distance. Since the free space in each cell is a convex shape this
	 * information is sufficient. If the entire interval is not "free" the
	 * corresponding array will be <code>null</code>.
	 *
	 * @param distance the distance for the free space diagram
	 * @return the free space diagram resulting from the given distance
	 */
	private Cell[][] freeSpaceDiagram(double distance) {
		final Cell[][] freeSpace = new Cell[p.length][q.length];
		for (int i = 0; i < p.length; i++) {
			for (int j = 0; j < q.length; j++) {
				freeSpace[i][j] = new Cell();
			}
		}

		for (int i = 0; i < p.length - 1; i++) {
			for (int j = 0; j < q.length - 1; j++) {
				freeSpace[i][j].left = FrechetDecider.freeInterval(p, i, q, j, distance);
				freeSpace[i][j].bottom = FrechetDecider.freeInterval(q, j, p, i, distance);
			}
			// Calculate top
			freeSpace[i][q.length - 1].bottom = FrechetDecider.freeInterval(q, q.length - 1, p, i, distance);
		}
		// Calculate right
		for (int j = 0; j < q.length - 1; j++) {
			freeSpace[p.length - 1][j].left = FrechetDecider.freeInterval(p, p.length - 1, q, j, distance);
		}
		return freeSpace;
	}

	/**
	 * Calculates the reachability diagram for a free space diagram.
	 *
	 * <p>
	 * The free space diagram contains information about the positions on both
	 * polygons that are in reach of each other. By simply inspecting the free space
	 * diagram the reachability can be calculated.
	 *
	 * @param freeSpace the free space diagram of two polygons
	 * @return the reachability diagram of given free space diagram
	 */
	private Cell[][] reachabilityDiagram(Cell[][] freeSpace) {
		final int p = freeSpace.length;
		final int q = freeSpace[0].length;
		final Cell[][] reachability = new Cell[p][q];
		for (int i = 0; i < p; i++) {
			for (int j = 0; j < q; j++) {
				reachability[i][j] = new Cell();
			}
		}
		// First row / column
		for (int i = 0; i < p - 1; i++) {
			reachability[i][0].bottom = freeSpace[i][0].bottom;
			if (freeSpace[i][0].bottom == null || freeSpace[i][0].bottom[1] != 1) {
				break;
			}
		}
		for (int j = 0; j < q - 1; j++) {
			reachability[0][j].left = freeSpace[0][j].left;
			if (freeSpace[0][j].left == null || freeSpace[0][j].left[1] != 1) {
				break;
			}
		}
		// Dynamically calculate rest of the reachability diagram
		for (int i = 0; i < p - 1; i++) {
			for (int j = 0; j < q - 1; j++) {
				if (null == freeSpace[i + 1][j].left) {
					reachability[i + 1][j].left = null;
				} else if (null != reachability[i][j].bottom) {
					reachability[i + 1][j].left = freeSpace[i + 1][j].left;
				} else if (null == reachability[i][j].left) {
					reachability[i + 1][j].left = null;
				} else if (reachability[i][j].left[0] > freeSpace[i + 1][j].left[1]) {
					reachability[i + 1][j].left = null;
				} else {
					reachability[i + 1][j].left = new double[] {
							Math.max(reachability[i][j].left[0], freeSpace[i + 1][j].left[0]),
							freeSpace[i + 1][j].left[1] };
				}

				if (null == freeSpace[i][j + 1].bottom) {
					reachability[i][j + 1].bottom = null;
				} else if (null != reachability[i][j].left) {
					reachability[i][j + 1].bottom = freeSpace[i][j + 1].bottom;
				} else if (null == reachability[i][j].bottom) {
					reachability[i][j + 1].bottom = null;
				} else if (reachability[i][j].bottom[0] > freeSpace[i][j + 1].bottom[1]) {
					reachability[i][j + 1].bottom = null;
				} else {
					reachability[i][j + 1].bottom = new double[] {
							Math.max(reachability[i][j].bottom[0], freeSpace[i][j + 1].bottom[0]),
							freeSpace[i][j + 1].bottom[1] };
				}
			}
		}
		return reachability;
	}
}
