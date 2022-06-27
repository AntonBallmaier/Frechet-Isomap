package runner;

import java.util.List;

import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.plot3d.primitives.LightPoint;
import org.jzy3d.plot3d.primitives.ScatterPoint;
import org.jzy3d.plot3d.primitives.axes.layout.IAxeLayout;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.view.View;
import org.jzy3d.plot3d.rendering.view.ViewportMode;

/**
 * A {@link ScatterPlot} displaying a set of points in 3d space. The 3d model
 * can than be viewed from different angles to allow for a good visualization of
 * the structure of the plotted data.
 *
 * <p>
 * Every implementation of this class must provide a method {@link #getPoints()}
 * that returns the points including their colors to be plotted.
 *
 * <p>
 * This class is build upon the jzy3d library providing most of the
 * functionality.
 *
 * @author Anton Ballmaier
 *
 */
public abstract class ScatterPlot extends AbstractAnalysis {
	/**
	 * The size of plotted points in pixels. The points will be drawn as squares
	 * with this value as their side length. Generally, higher values should be used
	 * for smaller data sets.
	 */
	private final float pointSize;

	/**
	 * Constructs a new {@link ScatterPlot} object using the given point size.
	 *
	 * @param pointSize the size of plotted points in pixels. The points will be
	 *                  drawn as squares with this value as their side length.
	 */
	protected ScatterPlot(float pointSize) {
		this.pointSize = pointSize;
	}

	/**
	 * Initialization code used by the jzy3d framework to create a scatter plot.
	 *
	 * <p>
	 * The scatter plot is specified in a way so that it will always remain
	 * correctly proportioned.
	 */
	@Override
	public void init() {
		final ScatterPoint scatter = new ScatterPoint(getPoints(), pointSize);

		chart = AWTChartComponentFactory.chart(Quality.Advanced, "newt");
		chart.getScene().add(scatter);
		final View view = chart.getView();
		view.setSquared(false);
		view.getCamera().setViewportMode(ViewportMode.RECTANGLE_NO_STRETCH);
		final IAxeLayout axe = chart.getAxeLayout();
		axe.setXAxeLabelDisplayed(false);
		axe.setYAxeLabelDisplayed(false);
		axe.setZAxeLabelDisplayed(false);
	}

	/**
	 * Opens a new window containing the 3d scatter plot of this instances points.
	 * The 3d model can than be viewed from different angles to allow for a good
	 * visualization of the structure of the plotted data.
	 */
	public void plot() {
		try {
			AnalysisLauncher.open(this);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the set of <code>LightPoint</code>s to be plotted. Every
	 * {@link LightPoint} contains information about the position and the color of a
	 * single point.
	 *
	 * @return the list of colored points to be plotted
	 */
	protected abstract List<LightPoint> getPoints();
}
