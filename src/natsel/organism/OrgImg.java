package natsel.organism;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import natsel.environment.Map;
import natsel.utils.Graph;

public class OrgImg extends BufferedImage {

	private final static int width = Map.CELL * 2;
	private final static int height = Map.CELL * 2;

	private int[] pixels;

	public OrgImg(Organism o) {
		super(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = createGraphics();
		if (o != null) {
			for (int i = 0; i < Graph.INDX_MAX - 1; i++) {
				int c = 0;
				float m = 0f;
				switch (i) {
				case Graph.INDX_SIZE:
					c = Graph.COL_SIZE;
					m = (float) o.getSize() / (float) Organism.MAX_SIZE;
					break;
				case Graph.INDX_SPEED:
					c = Graph.COL_SPEED;
					m = (float) o.getSpeed() / (float) Organism.MAX_SPEED;
					break;
				case Graph.INDX_STAMINA:
					c = Graph.COL_STAMINA;
					m = (float) o.getStamina() / (float) Organism.MAX_STAMINA;
					break;
				case Graph.INDX_SENSE:
					c = Graph.COL_SENSE;
					m = (float) o.getSense() / (float) Organism.MAX_SENSE;
					break;
				case Graph.INDX_CONF:
					c = Graph.COL_CONF;
					m = (float) o.getConf() / (float) Organism.MAX_CONF;
					break;
				case Graph.INDX_STRENGTH:
					c = Graph.COL_STRENGTH;
					m = (float) o.getStrength() / (float) Organism.MAX_STRENGTH;
					break;
				case Graph.INDX_INTEL:
					c = Graph.COL_INTEL;
					m = (float) o.getIntel() / (float) Organism.MAX_INTEL;
					break;
				}
				g.setColor(new Color(c));
				int x = (i + 1) * 6;
				int y = (int) (m * (height - 5));
				g.drawLine(x, 0, x, y);
				g.fillOval(x - 2, y - 2, 4, 4);
			}
		}
		g.dispose();
		pixels = new int[width * height];
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				pixels[x + y * width] = getRGB(x, y);
	}

	public int getPixel(int x, int y) {
		return pixels[x + y * width];
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
