package embedding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import data.ConstructedData;
import embedding.measures.DirectMeasure;
import testUtils.TestUtils;

class IsomapTest {
	private final DirectMeasure<double[]> euclidean = (a, b) -> TestUtils.euclideanDistance(a, b);
	private final double[][] swissRoll = ConstructedData.randomSwissRoll(1000);

	@Test
	void nearestNeighborCountTest() {
		final Isomap<double[]> isomap = new Isomap<>(swissRoll, euclidean, 5, 10);
		isomap.setNearestNeighborCount(7);
		assertEquals(7, isomap.getNearestNeighborCount());
		isomap.setNearestNeighborCount(7);
		assertThrows(IllegalArgumentException.class, () -> isomap.setNearestNeighborCount(0));
		assertThrows(IllegalArgumentException.class, () -> isomap.setNearestNeighborCount(1000));
		isomap.setNearestNeighborCount(2);
	}

	@Test
	void swissRollTest() {
		final Isomap<double[]> isomap = new Isomap<>(swissRoll, euclidean, 5, 50);
		assertEquals(0, isomap.embeddingQuality(2), 0.05);
		isomap.useLandmarks(false);
		assertEquals(0, isomap.embeddingQuality(2), 0.05);
	}

}
