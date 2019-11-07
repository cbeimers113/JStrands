package natsel.organism;

import java.awt.Point;
import java.util.ArrayList;

import natsel.environment.Map;

public class Brain {

	public float[][] visual;

	public ArrayList<Point> history;

	public Brain() {
		visual = new float[Map.width][Map.height];
		history = new ArrayList<Point>();
	}

	public static float[][] getAvgVisual(ArrayList<Organism> pop) {
		return getAvgVisual(pop, null);
	}

	public static float[][] getAvgVisual(ArrayList<Organism> pop, String species) {
		float[][] vis = new float[Map.width][Map.height];
		float s = pop.size();
		for (Organism o : pop)
			if (species == null || o.getSpecies().equals(species)) for (int y = 0; y < Map.height; y++)
				for (int x = 0; x < Map.width; x++)
					vis[x][y] += o.getBrain().visual[x][y];
		for (int y = 0; y < Map.height; y++)
			for (int x = 0; x < Map.width; x++)
				vis[x][y] /= s;
		return vis;
	}
}
