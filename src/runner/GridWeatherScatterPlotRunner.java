package runner;

import java.util.Map;
import java.util.function.Function;

import data.GeographicPolygon;
import data.WeatherDataPolygonsFactory;
import data.WeatherFeature;
import data.WeatherGridDataPolygonsFactory;
import embedding.DirectEmbedder;
import embedding.Embedder;
import embedding.Isomap;
import embedding.measures.Measure;
import frechetDistance.ApproxFrechet;
import frechetDistance.IntegralDiscreteFrechet;
import frechetDistance.Polygon;

/**
 * This class contains a runner used to display the 3d embedding of
 * {@link Polygon}s generated from the grid weather dataset.
 *
 * <p>
 * Multiple embedding presets can be selected to view different interesting
 * results. These presets are based on different {@link Embedder}s and
 * {@link Measure}s.
 *
 * @author Anton Ballmaier
 *
 */
public class GridWeatherScatterPlotRunner extends WeatherScatterPlot {
	/**
	 * Enumeration of the presets available. Each one corresponds to a different
	 * embedding method.
	 *
	 * @author Anton Ballmaier
	 *
	 */
	public static enum Preset {
		ISOMAP((polygons) -> {
			final Isomap<Polygon> embedder = new Isomap<>(polygons, new ApproxFrechet(), 15, 500);
			embedder.embed(1);
			embedder.setNearestNeighborCount(3);
			return embedder;
		}), MDS((polygons) -> new DirectEmbedder<>(polygons, IntegralDiscreteFrechet.getInstance(), 500));

		/**
		 * The internal function allowing for polymorphism. When calling the
		 * {@link #getEmbedder(Polygon[])} method method, this function is called
		 * internally.
		 */
		private final Function<Polygon[], Embedder<Polygon>> getEmbedder;

		/**
		 * Creates a new {@link Preset} from the given getEmbedder
		 * <code>Function</code>.
		 *
		 * @param getEmbedder a <code>Function</code> generating an {@link Embedder}
		 *                    from the given {@link Polygon}s.
		 */
		Preset(Function<Polygon[], Embedder<Polygon>> getEmbedder) {
			this.getEmbedder = getEmbedder;
		}

		/**
		 * Constructs an {@link Embedder} for the given {@link Polygon}s. This embedder
		 * is dependent on the specific {@link Preset} at play.
		 *
		 * @param polygons the {@link Polygon}s to construct the {@link Embedder} for
		 * @return the constructed {@link Embedder} fitting this {@link Preset}
		 */
		public Embedder<Polygon> getEmbedder(Polygon[] polygons) {
			return getEmbedder.apply(polygons);
		}
	}

	/**
	 * Main method making this class a runner for the scatter plot it implements.
	 *
	 * <p>
	 * A {@link GridWeatherScatterPlotRunner}-Instance is initialized and used to
	 * plot the embedding of the grid weather dataset. The used {@link Preset} can
	 * be changed to view the different predefined embedding methods.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		final WeatherScatterPlot gwsp = new GridWeatherScatterPlotRunner(Preset.MDS);
		gwsp.setDataFilter(WeatherGridDataPolygonsFactory.gridFilter(3)); // Remove this line to get the full dataset.
																			// Calculation would take a long time.
		gwsp.plot();
		gwsp.animateColor(10);
	}

	/**
	 * The preset for the embedding used for this scatter plot
	 */
	private final Preset preset;

	/**
	 * Constructs a new {@link GridWeatherScatterPlotRunner} object using the given
	 * {@link Preset} for embedding. The point size of the embedded points is fixed
	 * to 1.
	 *
	 * @param preset the {@link Preset} defining the {@link Embedder} used to
	 *               generate points from the dataset
	 */
	public GridWeatherScatterPlotRunner(Preset preset) {
		super(1);
		this.preset = preset;
	}

	/**
	 * Constructs a factory for {@link GeographicPolygon}s using the given weighted
	 * {@link WeatherFeature}s. The {@link Polygon} will be constructed from grid
	 * weather data.
	 *
	 *
	 * @param features the weighted {@link WeatherFeature}s present in the generated
	 *                 {@link GeographicPolygon}s.
	 * @return the {@link WeatherGridDataPolygonsFactory} used to generate the
	 *         dataset to be embedded
	 */
	@Override
	protected WeatherDataPolygonsFactory getDataFactory(Map<WeatherFeature, Double> features) {
		return new WeatherGridDataPolygonsFactory(features);
	}

	/**
	 * Returns an {@link Embedder} used to map the {@link Polygon}s to points. The
	 * specific {@link Embedder} is based on the {@link #preset}.
	 *
	 * @param polygons the {@link Polygon}s to construct an {@link Embedder} for
	 * @return the constructed {@link Embedder}
	 */
	@Override
	protected Embedder<Polygon> getEmbedder(Polygon[] polygons) {
		return preset.getEmbedder(polygons);
	}
}
