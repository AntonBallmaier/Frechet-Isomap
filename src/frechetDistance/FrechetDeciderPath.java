package frechetDistance;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.BinaryOperator;

/**
 * A decider for the fr&#xE9;chet decision problem using a path finding
 * approach.
 *
 * <p>
 * The decision problem is the question whether the fr&#xE9;chet distance of two
 * polygons is less or equal a given distance. This implementation of a
 * {@link FrechetDecider} uses my path finding approach.
 *
 * <p>
 * For that the free space and reachability is only calculated for a cell if
 * necessary. This is generally faster than {@linkplain FrechetDeciderTabular}.
 *
 * @author Anton Ballmaier
 *
 */
public class FrechetDeciderPath extends FrechetDecider {
	private final BinaryOperator<Integer> cellIndexer;
	private Map<Integer, ReachableCell> seen;

	/**
	 * Constructs a new {@link FrechetDeciderPath} object for the two given
	 * {@link Polygon}s. This can solve their decision problem for any distance.
	 *
	 * @param p the first polygon
	 * @param q the second polygon
	 * @throws IllegalArgumentException if the given polygons have different
	 *                                  dimensionality
	 */
	public FrechetDeciderPath(Polygon p, Polygon q) {
		super(p, q);
		cellIndexer = (i, j) -> i + j * p.length;
	}

	@Override
	public boolean decideDistance(double distance) {

		// Quick test: Are start and end points close enough?
		if (endpointsToFar(distance)) {
			return false;
		}

		seen = new HashMap<>();
		final Stack<ReachableCell> stack = new Stack<>();

		final ReachableCell start = new ReachableCell(0, 0, p, q);
		seen.put(cellIndexer.apply(0, 0), start);
		stack.push(start);

		while (!stack.isEmpty()) {
			final ReachableCell current = stack.pop();

			// Test if the cell is the top right corner of the FSD
			if (current.i == p.length - 2 && current.j == q.length - 2) {
				// If top right cell is reachable the top right corner is reachable as well,
				// because the top right corner was checked in the beginning
				return true;
			}

			current.calculateFreeSpace(distance);

			// Calculate next reachable cells
			final ReachableCell right = stepRight(current), up = stepUp(current), diagonal = stepDiagonal(current);

			// Add next cells to the stack in the correct order
			for (final ReachableCell next : prioritize(right, up, diagonal)) {
				stack.push(next);
				seen.put(cellIndexer.apply(next.i, next.j), next);
			}
		}
		return false;
	}

	/**
	 * Sorts the given {@link ReachableCell}s in ascending priority. Only actual
	 * {@link ReachableCell}s will be included in the {@link List},
	 * <code>null</code> values are being dropped.
	 *
	 * <p>
	 * If present the diagonal {@link ReachableCell} always has the highest
	 * priority. Of the other two cells the one closer to the diagonal of the free
	 * space diagram has higher priority.
	 *
	 * @param right    the right cell relative to the current
	 * @param up       the top cell relative to the current
	 * @param diagonal the diagonal cell relative to the current
	 * @return the list of the non-<code>null</code> cells in ascending priority
	 */
	private List<ReachableCell> prioritize(ReachableCell right, ReachableCell up, ReachableCell diagonal) {
		final List<ReachableCell> priority = new LinkedList<>();
		if (right == null) {
			if (up != null) {
				priority.add(up);
			}
		} else if (up == null) {
			priority.add(right);
		} else {
			// Calculate which of top and right cell are closer to the diagonal
			// The values don't represent actual distances
			final double rightDeviation = Math.abs((right.i + 0.5) / (p.length - 1) - (right.j + 0.5) / (q.length - 1)),
					topDeviation = Math.abs((up.i + 0.5) / (p.length - 1) - (up.j + 0.5) / (q.length - 1));

			// Closer value gets pushed later so that it gets popped earlier
			if (rightDeviation > topDeviation) {
				priority.add(right);
				priority.add(up);
			} else {
				priority.add(up);
				priority.add(right);
			}
		}
		// Give diagonal always the highest priority
		if (diagonal != null) {
			priority.add(diagonal);
		}

		return priority;
	}

	/**
	 * Tries to take a diagonal step from the current cell and returns the reached
	 * cell of possible.
	 * <p>
	 * The following cases will result in returning <code>null</code> however:
	 * <ul>
	 * <li>stepping diagonal would result in leaving the free space diagram
	 * <li>the top right corner of the current cell is not within it's free space
	 * <li>the diagonal cell has already been visited without restriction
	 * </ul>
	 *
	 * If the cell was already visited with a restriction the same
	 * {@link ReachableCell} object created before will be returned with updated
	 * restriction.
	 *
	 * @param current the current cell
	 * @return the diagonal cell or <code>null</code>
	 */
	private ReachableCell stepDiagonal(ReachableCell current) {
		ReachableCell next = null;
		if (current.isDiagonalReachable()) {
			next = seen.get(cellIndexer.apply(current.i + 1, current.j + 1)); // Try to find saved cell
			if (next == null) {
				next = new ReachableCell(current.i + 1, current.j + 1, p, q);
			} else if (next.restriction != 0) {
				next.restriction = 0;
			} else {
				next = null;
			}
		}
		return next;
	}

	/**
	 * Tries to take a step right from the current cell and returns the reached cell
	 * of possible.
	 * <p>
	 * The following cases will result in returning <code>null</code> however:
	 * <ul>
	 * <li>stepping right would result in leaving the free space diagram
	 * <li>the current cell has no right free interval
	 * <li>the right cell can't be reached due to the current restriction
	 * <li>the right cell has already been visited with same or less significant
	 * restriction
	 * </ul>
	 *
	 * If the cell was already visited with a more significant restriction the same
	 * {@link ReachableCell} object created before will be returned with updated
	 * restriction.
	 *
	 * @param current the current cell
	 * @return the right cell or <code>null</code>
	 */
	private ReachableCell stepRight(ReachableCell current) {
		ReachableCell next = null;
		if (current.isRightReachable()) {
			next = seen.get(cellIndexer.apply(current.i + 1, current.j)); // Try to find saved cell
			final double restriction = Math.max(current.restriction, current.rightInterval[0]);

			if (next == null) {
				// Cell has not been visited before
				next = new ReachableCell(current.i + 1, current.j, p, q);
				next.restriction = restriction;
			} else if (next.restriction < 0) {
				// Cell has been visited from below
				next.restriction = 0;
			} else if (restriction < next.restriction) {
				// Cell has been visited from the left with lower restriction.
				next.restriction = restriction;
			} else {
				// Cell has been visited before with the same (or greater) restriction. Ignore
				// it.
				next = null;
			}
		}
		return next;
	}

	/**
	 * Tries to take a step up from the current cell and returns the reached cell of
	 * possible.
	 * <p>
	 * The following cases will result in returning <code>null</code> however:
	 * <ul>
	 * <li>stepping up would result in leaving the free space diagram
	 * <li>the current cell has no up free interval
	 * <li>the top cell can't be reached due to the current restriction
	 * <li>the top cell has already been visited with same or less significant
	 * restriction
	 * </ul>
	 *
	 * If the cell was already visited with a more significant restriction the same
	 * {@link ReachableCell} object created before will be returned with updated
	 * restriction.
	 *
	 * @param current the current cell
	 * @return the top cell or <code>null</code>
	 */
	private ReachableCell stepUp(ReachableCell current) {
		ReachableCell next = null;
		if (current.isTopReachable()) {
			next = seen.get(cellIndexer.apply(current.i, current.j + 1)); // Try to find saved cell
			final double restriction = Math.min(current.restriction, -current.topInterval[0]);

			if (next == null) {
				// Cell has not been visited before
				next = new ReachableCell(current.i, current.j + 1, p, q);
				next.restriction = restriction;
			} else if (next.restriction > 0) {
				// Cell has been visited from the left
				next.restriction = 0;
			} else if (restriction > next.restriction) {
				// Cell has been visited from below with lower restriction.
				next.restriction = restriction;
			} else {
				// Cell has been visited before with the same (or greater) restriction. Ignore
				// it.
				next = null;
			}
		}
		return next;
	}
}
