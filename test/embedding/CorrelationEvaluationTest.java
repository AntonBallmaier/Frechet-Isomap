package embedding;

import static org.junit.Assert.assertArrayEquals;

import org.junit.jupiter.api.Test;

class CorrelationEvaluationTest {

	// Other methods are tested in other test cases

	@Test
	void embeddingDistanceTest() {
		final double[][] embedding = new double[][] { { 1, 1, 0, 0 }, { 1, 0, 1, 0 } };
		final double sqrt2 = Math.sqrt(2);
		assertArrayEquals(new double[][] { { 0, 1, 1 }, { 1, 0, sqrt2 }, { 1, sqrt2, 0 }, { sqrt2, 1, 1 } },
				CorrelationEvaluation.embeddingDistances(embedding, 3));
	}
}
