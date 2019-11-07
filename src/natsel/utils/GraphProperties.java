package natsel.utils;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

public class GraphProperties {

	private ArrayList<Point> stats;
	private Color color;
	private String title;

	public GraphProperties(ArrayList<Point> stats, Color color, String title) {
		this.stats = stats;
		this.color = color;
		this.title = title;
	}

	public ArrayList<Point> getStats() {
		return stats;
	}

	public Color getColor() {
		return color;
	}

	public String getTitle() {
		return title;
	}
}
