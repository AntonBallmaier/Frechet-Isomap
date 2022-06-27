package embedding;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import embedding.measures.DirectMeasure;

class DirectEmbedderTest extends DirectEmbedder<Integer> {
	private static final DirectMeasure<Integer> measure = (a, b) -> Math.abs(a - b);
	private static final Integer[] values = new Integer[] { 0, 1, 2, 3, 4, 5, 6 };

	private DirectEmbedderTest() {
		super(values, measure);
	}

	@Test
	void embeddingQualityTest() {

		this.useLandmarks(true);
		this.setLandmarkCount(5);
		assertEquals(0, embeddingQuality(1), 0.1);

		// reuse only previous calculation
		this.setLandmarkCount(3);
		assertEquals(0, embeddingQuality(1), 0.1);

		// exactly reuse previous calculation
		this.setLandmarkCount(5);

		assertEquals(0, embeddingQuality(1), 0.1);

		// reuse some previous calculation
		this.useLandmarks(false);
		assertEquals(0, embeddingQuality(1), 0.1);
	}

	@Test
	void embeddingTest() {
		this.useLandmarks(false);
		final double[] embedding = embed(1)[0];
		final double sgn = Math.signum(embedding[0]);
		assertArrayEquals(new double[] { sgn * 3, sgn * 2, sgn * 1, 0, sgn * -1, sgn * -2, sgn * -3 }, embedding, 0.01);
	}

}
