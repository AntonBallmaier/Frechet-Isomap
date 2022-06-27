package frechetDistance;

/**
 * Representative of a reachable cell within the free space diagram of two
 * Polygons.
 *
 * @author Anton Ballmaier
 *
 */
class ReachableCell {

	/**
	 * Index of the cell within the free space diagram
	 */
	public final int i, j;

	/**
	 * Current restriction of the cell
	 */
	public double restriction = 0;

	/**
	 * Free space interval of the cell within the free space diagram
	 */
	public double[] rightInterval, topInterval;

	private double freeSpaceCalculated = -1;
	private final Polygon p, q;

	/**
	 * Constructs a new {@link ReachableCell} object.
	 *
	 * @param i the cells index as to the polygon p
	 * @param j the cells index as to the polygon q
	 * @param p the first polygon
	 * @param q the second polygon
	 */
	public ReachableCell(int i, int j, Polygon p, Polygon q) {
		this.i = i;
		this.j = j;
		this.p = p;
		this.q = q;
	}

	/**
	 * Calculates the right and top free intervals of this cell and saves them to
	 * <code>rightInterval</code> and <code>leftInterval</code>.
	 *
	 * @param distance the distance of the free space diagram
	 */
	public void calculateFreeSpace(double distance) {
		// Only calculate free intervals if cell has not been calculated before.
		// This can happen if a cell gets visited for the second time.
		if (freeSpaceCalculated < 0 || freeSpaceCalculated != distance) {
			rightInterval = FrechetDecider.freeInterval(p, i + 1, q, j, distance);
			topInterval = FrechetDecider.freeInterval(q, j + 1, p, i, distance);
			freeSpaceCalculated = distance;
		}
	}

	/**
	 * Check whether the cell diagonal up and right is reachable from this cell.
	 * <p>
	 * The distance used in the free space must be set using
	 * {@linkplain #calculateFreeSpace(double)} first.
	 *
	 * @return <code>true</code> if the top right corner of this cell is within it's
	 *         free space, <code>false</code> otherwise
	 */
	public boolean isDiagonalReachable() {
		return isRightFree() && isTopFree() && rightInterval[1] == 1;
	}

	/**
	 * Check whether the cell to the right is reachable from this cell, also taking
	 * into account the restriction.
	 * <p>
	 * The distance used in the free space must be set using
	 * {@linkplain #calculateFreeSpace(double)} first.
	 *
	 * @return <code>true</code> if a cell to the right reachable,
	 *         <code>false</code> otherwise
	 */
	public boolean isRightReachable() {
		return isRightFree() && rightInterval[1] >= restriction;
	}

	/**
	 * Check whether the cell to the top is reachable from this cell, also taking
	 * into account the restriction.
	 * <p>
	 * The distance used in the free space must be set using
	 * {@linkplain #calculateFreeSpace(double)} first.
	 *
	 * @return <code>true</code> if a cell to the top reachable, <code>false</code>
	 *         otherwise
	 */
	public boolean isTopReachable() {
		return isTopFree() && topInterval[1] >= -restriction;
	}

	/**
	 * Check whether the cell to the right is reachable from this cell, not taking
	 * the restriction into account.
	 * <p>
	 * The distance used in the free space must be set using
	 * {@linkplain #calculateFreeSpace(double)} first.
	 *
	 * @return <code>true</code> if a cell to the right is still within the free
	 *         space diagram and the right free interval of this cell isn't empty,
	 *         <code>false</code> otherwise
	 */
	private boolean isRightFree() {
		return i < p.length - 2 && rightInterval != null;
	}

	/**
	 * Check whether the cell to the top is reachable from this cell, not taking the
	 * restriction into account.
	 * <p>
	 * The distance used in the free space must be set using
	 * {@linkplain #calculateFreeSpace(double)} first.
	 *
	 * @return <code>true</code> if a cell to the top is still within the free space
	 *         diagram and the top free interval of this cell isn't empty,
	 *         <code>false</code> otherwise
	 */
	private boolean isTopFree() {
		return j < q.length - 2 && topInterval != null;
	}
}