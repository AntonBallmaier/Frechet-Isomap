package runner;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import data.GeographicGridPolygon;
import data.WeatherGridDataPolygonsFactory;

/**
 * A {@link CoverageMap} is used to generate a map-like image representation
 * from a {@link List} of {@link GeographicGridPolygon}. Every pixel of the
 * generated image corresponds to a single geographic location for which a
 * polygon is provided.
 *
 * <p>
 * The image always has the same resolution, based on the size of the grid
 * weather dataset. Positions for which no data is given will result in
 * transparent pixels. Therefore this can be used to test filters provided to
 * the {@link WeatherGridDataPolygonsFactory}.
 *
 * <p>
 * This is also useful to visualize the effect of different
 * {@link WeatherPolygonPainter}s.
 *
 * @author Anton Ballmaier
 *
 */
public class CoverageMap {
	/**
	 * The height of resulting images. This is dependent on the number of longitude
	 * steps in the original dataset.
	 */
	private static int HEIGHT = WeatherGridDataPolygonsFactory.LONGITUDE_STEPS;

	/**
	 * The width of resulting images. This is dependent on the number of latitude
	 * steps in the original dataset.
	 */
	private static int WIDTH = WeatherGridDataPolygonsFactory.LATIDUTE_STEPS;

	/**
	 * The image buffer on which the coverage map is drawn.
	 */
	private final BufferedImage img;

	/**
	 * The painter providing the colors of the resulting image
	 */
	private final WeatherPolygonPainter painter;

	/**
	 * The {@link List} of used {@link GeographicGridPolygon} instances to draw to
	 * the map.
	 */
	private final List<GeographicGridPolygon> polygons;

	/**
	 * Constructs a new {@link CoverageMap} object using the given polygons and the
	 * {@link WeatherPolygonPainter} for providing them colors.
	 *
	 * @param polygons the {@link GeographicGridPolygon} instances to draw
	 * @param painter  the painter providing colors for the given polygons.
	 */
	public CoverageMap(List<GeographicGridPolygon> polygons, WeatherPolygonPainter painter) {
		this.polygons = polygons;
		this.painter = painter;
		this.img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * Draws and saves the coverage map as an image to the given directory. No file
	 * ending must be provided.
	 *
	 * @param imgPath the path and file name where to save the image.
	 */
	public void draw(String imgPath) {
		for (final GeographicGridPolygon p : polygons) {
			setPixel(p.getX(), p.getY(), painter.colorOf(p));
		}
		saveImg(imgPath);
	}

	/**
	 * Saves the content of the image buffer as an image to the given directory. No
	 * file ending must be provided.
	 *
	 * @param imgPath the path and file name where to save the image
	 */
	private void saveImg(String imgPath) {
		final File f = new File(imgPath + ".png");
		if (!f.exists()) {
			f.mkdirs();
		}
		try {
			ImageIO.write(img, "png", f);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sets a specific pixel of the image buffer to the given color.
	 *
	 * @param x   the x coordinate of the pixel
	 * @param y   the y coordinate of the pixel
	 * @param rgb the color to set the pixel to
	 */
	private void setPixel(int x, int y, int[] rgb) {
		y = HEIGHT - 1 - y;
		final int color = (255 << 24) | (rgb[0] << 16) | (rgb[1] << 8) | (rgb[2]);
		img.setRGB(x, y, color);
	}
}
