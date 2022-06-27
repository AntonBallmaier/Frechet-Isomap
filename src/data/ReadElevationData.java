package data;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Map;

/**
 * This class provides data to extend the grid weather dataset by elevation
 * data. The required information is simply read from a serialized
 * <code>Map</code>.
 *
 * <p>
 * The data itself is originally fetched from
 * <a href="https://www.opentopodata.org/">https://api.opentopodata.org</a>
 * using a by now invalidated API key.
 *
 * @author Anton Ballmaier
 *
 */
public class ReadElevationData {
	/**
	 * The map containing the required elevation data. The x and y coordinates of a
	 * position are combined to form a single integer key used in this map.
	 */
	private static Map<Integer, Integer> elevations;

	/**
	 * Returns the elevation MSL (meters above sea level) of a specific position
	 * given in the grid weather data coordinate system.
	 *
	 * @param x the x coordinate (roughly linearly dependent on longitude)
	 * @param y the y coordinate (roughly linearly dependent on latitude)
	 * @return the elevation MSL at the specified position, or
	 *         <code>Double.NaN</code> if no data is available. This is only the
	 *         case for positions outside of Germany.
	 */
	public static double getElevation(int x, int y) {
		if (x > 1000) {
			return Double.NaN;
		}
		final Integer elevation = fromFile().get(1000 * y + x);
		if (elevation == null) {
			return Double.NaN;
		}
		return elevation;
	}

	/**
	 * Returns the <code>Map</code> containing the required elevation data. If
	 * executed for the first time, this <code>Map</code> is read from a searialized
	 * form.
	 *
	 * @return the elevation <code>Map</code>
	 */
	@SuppressWarnings("unchecked")
	private static Map<Integer, Integer> fromFile() {
		if (elevations == null) {
			try {
				final FileInputStream readData = new FileInputStream("data/elevation/full.ser");
				final ObjectInputStream readStream = new ObjectInputStream(readData);

				elevations = (Map<Integer, Integer>) readStream.readObject();
				readStream.close();
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}

		return elevations;
	}

	// Prevent instantiation
	private ReadElevationData() {
	}
}