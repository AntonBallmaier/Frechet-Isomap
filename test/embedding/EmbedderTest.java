package embedding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import embedding.measures.DirectMeasure;

class EmbedderTest {

	@Test
	void exceptionsTest() {
		final DirectMeasure<Integer> measure = (a, b) -> Math.abs(a - b);
		assertThrows(IllegalArgumentException.class, () -> new DirectEmbedder<>(new Integer[] { 1, 2, 3 }, null));
		assertThrows(IllegalArgumentException.class, () -> new DirectEmbedder<>(null, measure));

		final Embedder<Integer> e = new DirectEmbedder<>(new Integer[] { 1, 2, 3 }, measure, 3);

		assertThrows(IllegalArgumentException.class, () -> e.embed(0));

	}

	@Test
	void testLandmarkCount() {
		final DirectMeasure<Integer> measure = (a, b) -> Math.abs(a - b);
		final Embedder<Integer> e = new DirectEmbedder<>(new Integer[] { 1, 2, 3 }, measure, 3);
		e.setLandmarkCount(2);
		assertEquals(2, e.getLandmarkCount());
		assertThrows(IllegalArgumentException.class, () -> e.setLandmarkCount(4));
		assertThrows(IllegalArgumentException.class, () -> e.setLandmarkCount(1));
	}

}
