package natsel.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;

import natsel.Main;
import natsel.environment.Map;
import natsel.organism.Brain;
import natsel.organism.OrgImg;
import natsel.organism.Organism;

public class Graph {

	private static final int[] SPEC_COLS = new int[] {
			0xf54242, 0xcef542, 0x42f569, 0x42ddf5, 0x5442f5
	};

	public static final int COL_SIZE = 0xC7C9D4;
	public static final int COL_SPEED = 0xF5E149;
	public static final int COL_STAMINA = 0x428A30;
	public static final int COL_SENSE = 0x75A4F0;
	public static final int COL_CONF = 0xF78254;
	public static final int COL_STRENGTH = 0xC2ECF2;
	public static final int COL_INTEL = 0xE0C2F2;
	public static final int COL_POP = 0xA3EAFF;
	public static final int COL_TEXT = 0xFCCF50;

	public static final int INDX_SIZE = 0;
	public static final int INDX_SPEED = 1;
	public static final int INDX_STAMINA = 2;
	public static final int INDX_SENSE = 3;
	public static final int INDX_CONF = 4;
	public static final int INDX_STRENGTH = 5;
	public static final int INDX_INTEL = 6;
	public static final int INDX_POP = 7;
	public static final int INDX_MAX = 8;

	public static final int DNA_SIZE = 4;
	public static final int DNA_SPEED = 5;
	public static final int DNA_STAMINA = 8;
	public static final int DNA_SENSE = 4;
	public static final int DNA_CONF = 5;
	public static final int DNA_STRENGTH = 7;
	public static final int DNA_INTEL = 7;

	public static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 15);

	private Main sim;
	private String topSpecies;
	private GraphProperties[] graphs;

	private int x;
	private int xOffs;

	private boolean extinct;

	public Graph(Main sim) {
		this.sim = sim;
		graphs = new GraphProperties[INDX_MAX];
		graphs[INDX_SIZE] = new GraphProperties(new ArrayList<Point>(), new Color(COL_SIZE), "Size");
		graphs[INDX_SPEED] = new GraphProperties(new ArrayList<Point>(), new Color(COL_SPEED), "Speed");
		graphs[INDX_STAMINA] = new GraphProperties(new ArrayList<Point>(), new Color(COL_STAMINA), "Stamina");
		graphs[INDX_SENSE] = new GraphProperties(new ArrayList<Point>(), new Color(COL_SENSE), "Sense");
		graphs[INDX_CONF] = new GraphProperties(new ArrayList<Point>(), new Color(COL_CONF), "Confidence");
		graphs[INDX_STRENGTH] = new GraphProperties(new ArrayList<Point>(), new Color(COL_STRENGTH), "Strength");
		graphs[INDX_INTEL] = new GraphProperties(new ArrayList<Point>(), new Color(COL_INTEL), "Intellect");
		graphs[INDX_POP] = new GraphProperties(new ArrayList<Point>(), new Color(COL_POP), "Population");
		x = 0;
		xOffs = 90;
		sim.focus();
	}

	public void updateStats(float[] stats) {
		for (int i = 0; i < INDX_MAX; i++)
			graphs[i].getStats().add(new Point(x, (int) stats[i]));
		x++;
		extinct = graphs[INDX_POP].getStats().get(graphs[INDX_POP].getStats().size() - 1).y == 0;
	}

	public void graphTraits(Graphics g, int drawX, int drawY) {
		int boxWidth = 600;
		int boxHeight = 80;
		g.setColor(new Color(0x4c6a75));
		g.fillRoundRect(drawX + 50, drawY, boxWidth + 110, boxHeight * (INDX_MAX + 1) + 40, 25, 25);
		for (int i = 0; i < INDX_MAX; i++) {
			GraphProperties graph = graphs[i];
			ArrayList<Point> stats = graph.getStats();
			int min = getMinStat(stats);
			int max = getMaxStat(stats);
			g.setColor(graph.getColor());
			int dx = drawX + xOffs - 15;
			int dy = drawY + 5 + i * (boxHeight + 15);
			g.drawRoundRect(dx + xOffs - 15, dy, boxWidth, boxHeight, 25, 25);
			g.drawRoundRect(dx + xOffs - 15 + 1, dy + 1, boxWidth - 2, boxHeight - 2, 20, 23);
			g.drawString(graph.getTitle() + ":", dx - 15, dy + 15);
			g.drawString((i == INDX_POP ? "Cur: " : "Avg:") + (stats.size() == 0 ? 0 : -stats.get(stats.size() - 1).y), dx - 15, dy + 30);
			g.drawString("Max: " + max, dx - 15, dy + 45);
			g.drawString("Min: " + min, dx - 15, dy + 60);
			int lx = -1, ly = -1;
			int index = 0;
			for (Iterator<Point> iter = stats.iterator(); iter.hasNext();) {
				Point p = iter.next();
				int plotYOffs = max > 0 ? (int) ((boxHeight - 10) * (((float) (-p.y - min)) / ((float) (max - min)))) : 0;
				int plotXOffs = (index > 0) ? (int) ((boxWidth - 30) * ((float) p.x / (float) (stats.size() - 1))) : 0;
				if (index > 0) g.drawLine(dx + xOffs + lx, dy + boxHeight - 5 - ly, dx + xOffs + plotXOffs, dy + boxHeight - 5 - plotYOffs);
				lx = plotXOffs;
				ly = plotYOffs;
				g.fillOval(dx + xOffs + plotXOffs - 2, dy + boxHeight - 5 - plotYOffs - 2, 4, 4);
				index++;
			}
		}
	}

	public void graphRankings(Graphics g, int drawX, int drawY) {
		if (extinct) return;
		g.setColor(Color.LIGHT_GRAY);
		g.setFont(new Font("Arial", Font.BOLD, 14));
		String[] specs = sim.getMap().getTopSpecies();
		int space = 0;
		int i;
		for (i = 0; i < specs.length; i++) {
			g.setColor(new Color(SPEC_COLS[i]));
			drawSpecGraph(drawX + (space += (i > 0 ? stringWidth(specs[i - 1], g) : 0)) + 15 * i, drawY - 20, specs[i], g);
		}
	}

	private void drawSpecGraph(int xOffs, int yOffs, String species, Graphics g) {
		g.drawString(species, xOffs, yOffs);
		int n = sim.getMap().getPopBySpecies(species);
		g.fillRect(xOffs + stringWidth(species, g) / 2 - 10, yOffs - 15 - n, 20, n);
	}

	public void graphTopSpec(Graphics g, int drawX, int drawY) {
		Organism o = sim.getMap().getBest();
		topSpecies = o.getSpecies();
		OrgImg img = new OrgImg(o);
		String label = "Top Species:";
		int lblWidth = g.getFontMetrics().stringWidth(label);
		g.setColor(new Color(COL_TEXT));
		g.setFont(FONT_LABEL);
		if (!extinct) g.drawString(label, drawX, drawY - 20);
		if (!extinct) for (int y = 0; y < img.getHeight(); y++)
			for (int x = 0; x < img.getWidth(); x++) {
				int c = img.getPixel(x, y);
				if (c == 0) continue;
				g.setColor(new Color(c));
				g.fillRect(drawX + lblWidth / 2 - img.getWidth() / 2 + x, drawY + y - 5, 1, 1);
			}
		if (!extinct) drawDNA(o, g, drawX + lblWidth + 15, drawY);
	}

	private void drawDNA(Organism o, Graphics g, int drawX, int drawY) {
		g.setColor(new Color(COL_TEXT));
		g.drawString(topSpecies = extinct ? "Extinct" : topSpecies, drawX + stringWidth("0000000", g) / 2 - stringWidth(topSpecies, g) / 2, drawY - 20);
		String[] traits = new String[] {
				fixStrandTo("" + Integer.toBinaryString(o.getSize()), DNA_SIZE), fixStrandTo("" + Integer.toBinaryString(o.getSpeed()), DNA_SPEED), fixStrandTo("" + Integer.toBinaryString(o.getStamina()), DNA_STAMINA), fixStrandTo("" + Integer.toBinaryString(o.getSense()), DNA_SENSE), fixStrandTo("" + Integer.toBinaryString(o.getConf()), DNA_CONF), fixStrandTo("" + Integer.toBinaryString((int) (o.getStrength() * 100)), DNA_STRENGTH), fixStrandTo("" + Integer.toBinaryString((int) (o.getIntel() * 100)), DNA_INTEL)
		};
		Color[] colors = new Color[] {
				new Color(COL_SIZE), new Color(COL_SPEED), new Color(COL_STAMINA), new Color(COL_SENSE), new Color(COL_CONF), new Color(COL_STRENGTH), new Color(COL_INTEL)
		};
		for (int t = 0; t < traits.length; t++) {
			String trait = traits[t];
			g.setColor(colors[t]);
			for (int i = 0; i < trait.length(); i++)
				g.drawString("" + trait.charAt(i), drawX + t * 8, drawY + i * 15);
		}
	}

	public void graphHistory(Graphics gx, int drawX, int drawY) {
		int spacing = 17;
		gx.setColor(new Color(COL_TEXT));
		gx.setFont(FONT_LABEL);
		gx.drawString("Speciation History", drawX, drawY + 15);
		ArrayList<String> history = sim.getMap().getHistory();
		int s = history.size();
		for (int i = 0; i < s; i++) {
			String species = history.get(i);
			float m = (float) i / (float) s;
			int r = (int) (0x46 * m);
			int g = (int) (0xE8 * m);
			int b = (int) (0x56 * m);
			gx.setColor(new Color(r, g, b));
			if (sim.getMap().isSpeciesAlive(species)) gx.fillRect(drawX, drawY + (i + 1) * (spacing + 1) + 5, 50, spacing - 2);
			gx.drawString(species, drawX + 55, drawY + (i + 2) * (spacing + 1));
			if (topSpecies.equals(species)) {
				gx.setColor(new Color(COL_TEXT));
				gx.drawRect(drawX, drawY + (i + 1) * (spacing + 1) + 4, stringWidth(species, gx) + 57, spacing - 1);
			}
		}
	}

	public void graphVisualCortex(Graphics g, int drawX, int drawY) {
		g.setColor(new Color(COL_TEXT));
		g.setFont(FONT_LABEL);
		String label = extinct ? "Extinct" : (topSpecies + "\'s Visual Cortex");
		g.drawString(label, drawX + 3 * Map.width / 2 - stringWidth(label, g) / 2, drawY - 15);
		g.drawRoundRect(drawX - 5, drawY - 5, Map.width * 3 + 10, Map.height * 3 + 10, 25, 25);
		float[][] vis = Brain.getAvgVisual(sim.getMap().getOrganisms(), topSpecies);
		for (int y = 0; y < Map.height; y++) {
			for (int x = 0; x < Map.width; x++) {
				float v = vis[x][y];
				if (v == 0) continue;
				int c = (int) (Math.abs(v) * 0xFF);
				if (c > 0xFF) c = 0xFF;
				if (c < 0x05) c = 0x05;
				g.setColor(new Color(v > 0 ? 0 : c, v > 0 ? c : 0, 0));
				g.fillOval(drawX + x * 3, drawY + y * 3, 3, 3);
			}
		}
	}

	public static String fixStrandTo(String strand, int length) {
		while (strand.length() < length)
			strand = "0" + strand;
		return strand;
	}

	public static int stringWidth(String string, Graphics g) {
		return g.getFontMetrics().stringWidth(string);
	}

	private int getMaxStat(ArrayList<Point> stats) {
		int max = 0;
		for (Point p : stats)
			if (p.y < max) max = p.y;
		return -max;
	}

	private int getMinStat(ArrayList<Point> stats) {
		int min = Integer.MIN_VALUE;
		for (Point p : stats) {
			if (p.y > min) min = p.y;
		}
		return -min;
	}
}
