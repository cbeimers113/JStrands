package natsel.environment;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;

import javafx.util.Pair;
import natsel.organism.OrgImg;
import natsel.organism.Organism;
import natsel.organism.Species;
import natsel.utils.Graph;

public class Map {

	public static final int CELL = 25;
	public static final int CONTROL_SPACE = 5;

	private static final Random rand = new Random();
	private static final DecimalFormat df = new DecimalFormat("#.##");

	public static int width;
	public static int height;

	private int carryingCapacity = 25;
	private int numFood = 200;
	private int numTraps = 35;

	private ArrayList<Food> food;
	private ArrayList<Trap> traps;
	private ArrayList<Organism> organisms;
	private ArrayList<String> history;
	private Stack<Organism> waitlist;
	private Organism best;

	public String[] stats;

	private float scarcity;

	private boolean drawPaths;

	public Map(int sWidth, int sHeight) {
		df.setRoundingMode(RoundingMode.CEILING);
		width = (sWidth - CONTROL_SPACE * CELL) / CELL;
		height = sHeight / CELL;
		food = new ArrayList<Food>();
		traps = new ArrayList<Trap>();
		initFood();
		initTraps();
		organisms = new ArrayList<Organism>();
		history = new ArrayList<String>();
		stats = new String[] {
				"", "", "", "", "", "", "", ""
		};
		waitlist = new Stack<Organism>();
	}

	private void initFood() {
		int adj = 0;
		for (int i = 0; i < numFood; i++) {
			int x = rand.nextInt(3 * width / 4) + width / 8;
			int y = rand.nextInt(3 * height / 4) + height / 8;
			if (!isFoodAt(x, y) && !isTrapAt(x, y)) {
				food.add(new Food(x, y, (float) (rand.nextInt(100) + 10) / 100f));
				adj++;
			}
		}
		numFood = adj;
	}

	private void initTraps() {
		for (int i = 0; i < numTraps; i++) {
			int x = rand.nextInt(3 * width / 4) + width / 8;
			int y = rand.nextInt(3 * height / 4) + height / 8;
			if (!isFoodAt(x, y) && !isTrapAt(x, y)) traps.add(new Trap(x, y, (float) (rand.nextInt(40) + 1) / 100f));
		}
	}

	private void replenish() {
		int maxFood = Math.min(numFood, (int) ((1f - scarcity) * (numFood - 1)));
		for (int i = 0; i < maxFood; i++)
			food.get(i).setEaten(false);
	}

	private boolean isFoodAt(int x, int y) {
		for (Iterator<Food> iter = food.iterator(); iter.hasNext();) {
			Food f = iter.next();
			if (f.getX() == x && f.getY() == y) return true;
		}
		return false;
	}

	private boolean isTrapAt(int x, int y) {
		for (Iterator<Trap> iter = traps.iterator(); iter.hasNext();) {
			Trap t = iter.next();
			if (t.getX() == x && t.getY() == y) return true;
		}
		return false;
	}

	private void createOrganism(Organism o) {
		organisms.add(o);
		if (best == null) best = o;
		if (!history.contains(o.getSpecies())) history.add(o.getSpecies());
	}

	public void spawn(int amnt) {
		for (int i = 0; i < amnt; i++) {
			int sx = -1;
			int sy = -1;
			if (rand.nextBoolean()) { // spawn along vertical
				sy = rand.nextInt(height);
				sx = rand.nextBoolean() ? 0 : (width - 1);
			} else { // spawn along horizontal
				sy = rand.nextBoolean() ? 0 : (height - 1);
				sx = rand.nextInt(width);
			}
			for (Organism o : organisms)
				if (o.getHome().x == sx && o.getHome().y == sy) return;
			createOrganism(new Organism(rand.nextInt(3) + 1, rand.nextInt(3) + 1, rand.nextInt(3) + 1, rand.nextFloat(), rand.nextFloat(), sx, sy));
		}
	}

	public void reproduce(Organism parent) {
		int sx = -1;
		int sy = -1;
		if (rand.nextBoolean()) { // spawn along vertical
			sy = rand.nextInt(height);
			sx = rand.nextBoolean() ? 0 : (width - 1);
		} else { // spawn along horizontal
			sy = rand.nextBoolean() ? 0 : (height - 1);
			sx = rand.nextInt(width);
		}
		for (Organism o : organisms)
			if (o.getHome().x == sx && o.getHome().y == sy) return;
		Organism child;
		createOrganism(child = new Organism(parent.getSize() + rand.nextInt(3) - 1, parent.getSense() + rand.nextInt(3) - 1, parent.getConf() + rand.nextInt(3) - 1, parent.getStrength() + rand.nextFloat() / 4 - 0.125f, parent.getIntel() + rand.nextFloat() / 4 - 0.125f, sx, sy));
		child.setBrain(parent.getBrain());
	}

	public void update() {
		for (Iterator<Organism> iter = organisms.iterator(); iter.hasNext();) {
			Organism o = iter.next();
			if (o.isDead()) iter.remove();
			else {
				o.update(food, traps, organisms);
			}
		}
	}

	public int getPopulation() {
		return organisms.size();
	}

	public void refresh() {
		ArrayList<Organism> toReproduce = new ArrayList<Organism>();
		for (Iterator<Organism> iter = organisms.iterator(); iter.hasNext();) {
			Organism o = iter.next();
			if (!o.isFed() || o.isDead()) iter.remove();
			else if (o.hasSurplus()) toReproduce.add(o);
		}
		replenish();
		int sampleSize = 0;
		float bSize = 0;
		float bSpeed = 0;
		float bStamina = 0;
		float bSense = 0;
		float bAdv = 0;
		float bShell = 0;
		float bPrep = 0;
		for (Organism o : organisms) {
			sampleSize++;
			o.refresh();
			bSize += o.getSize();
			bSpeed += o.getSpeed();
			bStamina += o.getStamina();
			bSense += o.getSense();
			bAdv += o.getConf();
			bShell += o.getStrength();
			bPrep += o.getIntel();
		}
		for (Organism o : toReproduce) {
			sampleSize++;
			reproduce(o);
			bSize += o.getSize();
			bSpeed += o.getSpeed();
			bStamina += o.getStamina();
			bSense += o.getSense();
			bAdv += o.getConf();
			bShell += o.getStrength();
			bPrep += o.getIntel();
		}
		while (waitlist.size() > 0) {
			sampleSize++;
			Organism o;
			reproduce(o = waitlist.pop());
			bSize += o.getSize();
			bSpeed += o.getSpeed();
			bStamina += o.getStamina();
			bSense += o.getSense();
			bAdv += o.getConf();
			bShell += o.getStrength();
			bPrep += o.getIntel();
		}
		bSize /= sampleSize;
		bSpeed /= sampleSize;
		bStamina /= sampleSize;
		bSense /= sampleSize;
		bAdv /= sampleSize;
		bShell /= sampleSize;
		bPrep /= sampleSize;
		stats[Graph.INDX_SIZE] = df.format(bSize);
		stats[Graph.INDX_SPEED] = df.format(bSpeed);
		stats[Graph.INDX_STAMINA] = df.format(bStamina);
		stats[Graph.INDX_SENSE] = df.format(bSense);
		stats[Graph.INDX_CONF] = df.format(bAdv);
		stats[Graph.INDX_POP] = "" + getPopulation();
		stats[Graph.INDX_STRENGTH] = df.format(bShell);
		stats[Graph.INDX_INTEL] = df.format(bPrep);
		scarcity = (float) Math.min((float) Math.min(getPopulation(), carryingCapacity) / (float) carryingCapacity, 0.99f);
	}

	public int getPopBySpecies(String species) {
		int i = 0;
		for (Iterator<Organism> iter = organisms.iterator(); iter.hasNext();)
			if (iter.next().getSpecies() == species) i++;
		return i;
	}

	public Organism getSample(String species) {
		for (Organism o : organisms)
			if (o.getSpecies().equals(species)) return o;
		return null;
	}

	public String[] getTopSpecies() {
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<Pair<String, Integer>> topSpecs = new ArrayList<Pair<String, Integer>>();
		Integer[] specs = new Integer[256];
		for (int i = 0; i < specs.length; i++)
			specs[i] = 0;
		try {
			for (Organism o : organisms)
				for (int i = 0; i < specs.length; i++)
					if (o.getSpecies().equals(Species.SPEC_NAMES[i])) specs[i]++;
		} catch (ConcurrentModificationException e) {
			return new String[] {
					"Dead"
			};
		}
		for (int i = 0; i < specs.length; i++)
			if (specs[i] > 0) topSpecs.add(new Pair<String, Integer>(Species.SPEC_NAMES[i], specs[i]));
		for (int i = 0; i < 5; i++) {
			if (topSpecs.size() == 0) continue;
			int h = 0;
			int index = 0;
			String species = "";
			for (Pair<String, Integer> p : topSpecs)
				if (p.getValue() > h) {
					h = p.getValue();
					species = p.getKey();
					index = topSpecs.indexOf(p);
				}
			topSpecs.remove(index);
			result.add(species);
		}
		String[] res = new String[result.size()];
		for (int i = 0; i < res.length; i++)
			res[i] = result.get(i);
		if (result.size() > 0) best = getSample(result.get(0));
		return res;
	}

	public Organism getBest() {
		return best;
	}

	public boolean isSpeciesAlive(String species) {
		for (Organism o : organisms)
			if (o.getSpecies().equals(species)) return true;
		return false;
	}

	public void render(Graphics g) {
		g.setColor(new Color(0x737373));
		g.fillRect(0, 0, width * CELL, height * CELL);
		// g.setColor(Color.LIGHT_GRAY);
		for (int y = 0; y < height; y++)
			g.drawLine(0, y * CELL, width * CELL, y * CELL);
		for (int x = 0; x < width; x++)
			g.drawLine(x * CELL, 0, x * CELL, height * CELL);
		for (Organism o : organisms) {
			if (o.isDead()) continue;
			int dirX = o.getDirection().x;
			int dirY = o.getDirection().y;
			int xAnimOffs = (int) (o.getAnimProgress() * CELL * (dirX == Organism.EAST ? 1 : dirX == Organism.NEUTRAL ? 0 : -1));
			int yAnimOffs = (int) (o.getAnimProgress() * CELL * (dirY == Organism.SOUTH ? 1 : dirY == Organism.NEUTRAL ? 0 : -1));
			int yOffs = o.getY() * CELL + yAnimOffs - CELL / 2;
			int xOffs = o.getX() * CELL + xAnimOffs - CELL / 2;
			g.setColor(new Color(Graph.COL_SENSE));
			int sns = o.getSense() * CELL;
			for (int y = -sns; y <= sns; y++)
				for (int x = -sns; x <= sns; x++)
					if (x * x + y * y <= sns * sns && x * x + y * y >= (sns - 2) * (sns - 2)) g.fillRect(o.getX() * CELL + CELL / 2 + x + xAnimOffs, o.getY() * CELL + CELL / 2 + y + yAnimOffs, 2, 2);
			OrgImg img = o.getAppearance();
			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					int c = img.getPixel(x, y);
					if (c == 0) continue;
					g.setColor(new Color(c));
					g.fillRect(x + xOffs, y + yOffs + 5, 1, 1);
				}
			}
			if (drawPaths) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						float v = o.getBrain().visual[x][y];
						if (v == 0) continue;
						int c = (int) (Math.abs(v) * 0xFF);
						if (c > 0xFF) c = 0xFF;
						if (c < 0x05) c = 0x05;
						g.setColor(new Color(v > 0 ? 0 : c, v > 0 ? c : 0, 0));
						g.fillOval(x * CELL + CELL / 2 - 3, y * CELL + CELL / 2 - 3, 6, 6);
					}
				}
				g.setColor(Color.ORANGE);
				ArrayList<Point> path = o.getPathHome();
				for (Point p : path)
					g.fillRect(p.x * CELL + CELL / 2 - 3, p.y * CELL + CELL / 2 - 3, 6, 6);
				Point dest = o.getDestination();
				if (dest.x != -1 && dest.y != -1) g.drawLine(o.getX() * CELL + CELL / 2 + xAnimOffs, o.getY() * CELL + CELL / 2 + yAnimOffs, dest.x * CELL + CELL / 2, dest.y * CELL + CELL / 2);
			}
			g.setColor(Color.BLACK);
			g.setFont(new Font("Ubuntu", Font.BOLD, 14));
			g.drawString(o.getSpecies(), o.getX() * CELL + CELL / 2 - g.getFontMetrics().stringWidth(o.getSpecies()) / 2 + xAnimOffs, o.getY() * CELL + yAnimOffs - CELL / 2);
			g.setColor(new Color(0x00BB00));
			g.fillRect(xOffs, yOffs - 20, (int) (img.getWidth() * o.getHealth()), 5);
		}
		g.setColor(new Color(0xffbb00));
		for (Food f : food) {
			if (f.isEaten()) continue;
			g.setColor(new Color(0x00, (int) (f.getHealing() * 0xDD), 0x00));
			g.fillRect(f.getX() * CELL + 5, f.getY() * CELL + 5, 15, 15);
		}
		for (Trap t : traps) {
			g.setColor(new Color((int) (t.getDamage() * 0xDD), 0x00, 0x00));
			g.fillPolygon(new int[] {
					t.getX() * CELL + 5, t.getX() * CELL + CELL / 2, t.getX() * CELL + CELL - 5
			}, new int[] {
					t.getY() * CELL + CELL - 5, t.getY() * CELL + 5, t.getY() * CELL + CELL - 5
			}, 3);
		}
	}

	public boolean allTaken() {
		return getPopulation() == 2 * (width - 3) + 2 * (height - 1);
	}

	public ArrayList<Organism> getOrganisms() {
		return organisms;
	}

	public void addToWaitlist(Organism o) {
		waitlist.push(o);
	}

	public int getFood() {
		return numFood;
	}

	public void setFood(int f) {
		this.numFood = f;
	}

	public ArrayList<String> getHistory() {
		return history;
	}

	public boolean isDrawingPaths() {
		return drawPaths;
	}

	public void setDrawingPaths(boolean drawPaths) {
		this.drawPaths = drawPaths;
	}
}
