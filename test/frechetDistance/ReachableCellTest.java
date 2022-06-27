package frechetDistance;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReachableCellTest {

	@Test
	void isFreeTest() {
		final Polygon a = new Polygon(new double[][] { { 0, 0 }, { 1, 1 } });
		final Polygon b = new Polygon(new double[][] { { 0, 1 }, { 1, 0 } });
		final Polygon c = new Polygon(new double[][] { { -5, 3 }, { 0, 0 }, { 1, 1 }, { -5, 3 }, { 5, 3 } });
		final Polygon d = new Polygon(new double[][] { { 3, 6 }, { -5, 3 }, { 0, 1 }, { 1, 0 }, { 2, 0 } });

		ReachableCell cell = new ReachableCell(0, 0, a, b);
		cell.calculateFreeSpace(0.8);
		assertFalse(cell.isRightReachable());
		assertFalse(cell.isTopReachable());
		assertFalse(cell.isDiagonalReachable());

		cell = new ReachableCell(1, 2, c, d);
		cell.calculateFreeSpace(0.8);
		assertTrue(cell.isRightReachable());
		assertTrue(cell.isTopReachable());
		assertFalse(cell.isDiagonalReachable());
		cell.restriction = 0.9;
		assertFalse(cell.isRightReachable());
		assertTrue(cell.isTopReachable());
		cell.restriction = -0.9;
		assertTrue(cell.isRightReachable());
		assertFalse(cell.isTopReachable());
		cell.restriction = -0.5;
		assertTrue(cell.isTopReachable());

		cell.restriction = 0;
		cell.calculateFreeSpace(1.2);
		assertTrue(cell.isDiagonalReachable());

		cell.calculateFreeSpace(0.5);
		assertFalse(cell.isRightReachable());
		assertFalse(cell.isTopReachable());
		assertFalse(cell.isDiagonalReachable());
	}
}
