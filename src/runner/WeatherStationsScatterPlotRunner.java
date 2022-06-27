package runner;

import java.util.Map;
import java.util.function.Function;

import data.GeographicPolygon;
import data.WeatherDataPolygonsFactory;
import data.WeatherFeature;
import data.WeatherStationsPolygonsFactory;
import embedding.DirectEmbedder;
import embedding.Embedder;
import embedding.Isomap;
import embedding.measures.Measure;
import frechetDistance.ApproxFrechet;
import frechetDistance.IntegralDiscreteFrechet;
import frechetDistance.Polygon;

/**
 * This class contains a runner used to display the 3d embedding of
 * {@link Polygon}s generated from the weather stations dataset.
 *
 * <p>
 * Multiple embedding presets can be selected to view different interesting
 * results. These presets are based on different {@link Embedder}s and
 * {@link Measure}s.
 *
 * @author Anton Ballmaier
 *
 */
public class WeatherStationsScatterPlotRunner extends WeatherScatterPlot {
	/**
	 * Enumeration of the presets available. Each one corresponds to a different
	 * embedding method.
	 *
	 * @author Anton Ballmaier
	 *
	 */
	public static enum Preset {
		ISOMAP((polygons) -> new Isomap<>(polygons, new ApproxFrechet(), 7)),
		MDS((polygons) -> new DirectEmbedder<>(polygons, IntegralDiscreteFrechet.getInstance()));

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
	 * A {@link WeatherStationsScatterPlotRunner}-Instance is initialized and used
	 * to plot the embedding of the weather station dataset. The used {@link Preset}
	 * can be changed to view the different predefined embedding methods.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		final WeatherScatterPlot wssp = new WeatherStationsScatterPlotRunner(Preset.ISOMAP);
		wssp.plot();
		wssp.animateColor(10);
	}

	/**
	 * The preset for the embedding used for this scatter plot
	 */
	private final Preset preset;

	/**
	 * Constructs a new {@link WeatherStationsScatterPlotRunner} object using the
	 * given {@link Preset} for embedding. The point size of the embedded points is
	 * fixed to 5 since this dataset is quite small.
	 *
	 * @param preset the {@link Preset} defining the {@link Embedder} used to
	 *               generate points from the dataset
	 */
	public WeatherStationsScatterPlotRunner(Preset preset) {
		super(5);
		this.preset = preset;
	}

	/**
	 * Constructs a factory for {@link GeographicPolygon}s using the given weighted
	 * {@link WeatherFeature}s. The {@link Polygon} will be constructed from the
	 * data of weather stations.
	 *
	 *
	 * @param features the weighted {@link WeatherFeature}s present in the generated
	 *                 {@link GeographicPolygon}s.
	 * @return the {@link WeatherStationsPolygonsFactory} used to generate the
	 *         dataset to be embedded
	 */
	@Override
	protected WeatherDataPolygonsFactory getDataFactory(Map<WeatherFeature, Double> features) {
		return new WeatherStationsPolygonsFactory(features);
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
