package data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ReadElevationDataTest {

	@Test
	void getElevationTest() {
		assertEquals(Double.NaN, ReadElevationData.getElevation(0, 0));
		assertEquals(Double.NaN, ReadElevationData.getElevation(100000, 0));
		assertEquals(614d, ReadElevationData.getElevation(200, 200));
		assertEquals(319d, ReadElevationData.getElevation(300, 300));
	}
}
