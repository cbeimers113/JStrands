package natsel.organism;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;

import natsel.environment.Food;
import natsel.environment.Map;
import natsel.environment.Trap;
import natsel.utils.Graph;

public class Organism {

	private static final Random rand = new Random();

	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public static final int NEUTRAL = 4;

	private static final int SCAN_DIR_N = 0;
	private static final int SCAN_DIR_NE = 1;
	private static final int SCAN_DIR_E = 2;
	private static final int SCAN_DIR_SE = 3;
	private static final int SCAN_DIR_S = 4;
	private static final int SCAN_DIR_SW = 5;
	private static final int SCAN_DIR_W = 6;
	private static final int SCAN_DIR_NW = 7;
	private static final int SCAN_DIR_NONE = 8;

	private static final int MIN_SIZE = 1;
	private static final int MIN_SENSE = 1;
	private static final int MIN_CONF = 1;
	private static final float MIN_STRENGTH = 0f;
	private static final float MIN_INTEL = 0f;

	public static final int MAX_SIZE = (2 << Graph.DNA_SIZE) - 1;
	public static final int MAX_SPEED = (2 << Graph.DNA_SPEED) - 1;
	public static final int MAX_STAMINA = (2 << Graph.DNA_STAMINA) - 1;
	public static final int MAX_SENSE = (2 << Graph.DNA_SENSE) - 1;
	public static final int MAX_CONF = (2 << Graph.DNA_CONF) - 1;

	public static final float MAX_STRENGTH = 1f;
	public static final float MAX_INTEL = 1f;

	private static boolean ALLOW_HUNTING = true;

	private int homeX, homeY;
	private int x, y;
	private int destX, destY;
	private int prevX, prevY;
	private int steps;
	private int meals;
	private int age;
	private int dirX;
	private int dirY;

	// Traits
	private int size;
	private int speed;
	private int stamina;
	private int sense;
	private int conf;
	private float strength;
	private float intel;

	private boolean fed;
	private boolean surplus;
	private boolean storage;
	private boolean dead;
	private boolean goingHome;

	private float wait;
	private float health;

	private Brain brain;
	private Point next;
	private Stack<Point> pathHome;

	public Organism(int size, int sense, int conf, float strength, float intel, int sx, int sy) {
		x = homeX = sx;
		y = homeY = sy;
		destX = destY = -1;
		this.size = Math.max(MIN_SIZE, Math.min(size, MAX_SIZE));
		this.speed = (int) (1f / ((1f / (float) MAX_SPEED) * this.size));
		this.stamina = (int) ((float) MAX_STAMINA * ((16f - (128f / ((float) this.size + 8f))) / 10.5f)) + rand.nextInt(20);
		this.sense = Math.max(MIN_SENSE, Math.min(sense, MAX_SENSE));
		this.conf = Math.max(MIN_CONF, Math.min(conf, MAX_CONF));
		this.strength = Math.max(MIN_STRENGTH, Math.min(strength, MAX_STRENGTH));
		this.intel = Math.max(MIN_INTEL, Math.min(intel, MAX_INTEL));
		health = strength;
		brain = new Brain();
		pathHome = new Stack<Point>();
		setWait();
	}

	public void update(ArrayList<Food> food, ArrayList<Trap> traps, ArrayList<Organism> orgs) {
		if (isDead() || (goingHome && x == homeX && y == homeY)) return;
		steps++;
		if (surplus && !goingHome) {
			destX = homeX;
			destY = homeY;
			goingHome = true;
			findPathHome();
		} else if (goingHome) {
			if (next == null) next = pathHome.pop();
			move(next.x - x, next.y - y, food, traps, orgs);
		} else {
			if (destX == -1 || destY == -1) getTarget(food, traps, orgs);
			int deltaX = destX - x;
			int deltaY = destY - y;
			if (deltaX != 0 || deltaY != 0) move(deltaX == 0 ? 0 : (deltaX / Math.abs(deltaX)), deltaY == 0 ? 0 : (deltaY / Math.abs(deltaY)), food, traps, orgs);
			else
				dirX = dirY = NEUTRAL;
			if (x == destX && y == destY && !goingHome) {
				if (surplus) return;
				destX = -1;
				destY = -1;
			}
		}
		updateBrain(food, traps);
	}

	private void updateBrain(ArrayList<Food> food, ArrayList<Trap> traps) {
		Food f = getFoodAt(x, y, food);
		if (f != null && !f.isEaten()) {
			feed();
			f.setEaten(true);
			health += f.getHealing();
			health = Math.min(health, strength);
			brain.visual[x][y] += f.getHealing();
			for (Point p : brain.history) {
				float dist = (float) Math.sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y));
				brain.visual[p.x][p.y] += 0.1f * (1f / (dist + 1));
			}
		}
		Trap t = getTrapAt(x, y, traps);
		if (t != null) {
			health -= t.getDamage();
			health = Math.max(health, 0f);
			brain.visual[x][y] -= t.getDamage();
			for (Point p : brain.history) {
				float dist = (float) Math.sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y));
				brain.visual[p.x][p.y] -= 0.1f * (1f / (dist + 1));
			}
		}
	}

	private void memoryLoss() {
		for (int yy = 0; yy < Map.height; yy++)
			for (int xx = 0; xx < Map.width; xx++) {
				float v = brain.visual[xx][yy];
				float delta = (1f - intel) * 0.01f;
				if (v < 0) v += delta;
				else if (v > 0) v -= delta;
				if (Math.abs(v) < 0.01f) v = 0;
				brain.visual[xx][yy] = v;
			}
	}

	private void getTarget(ArrayList<Food> food, ArrayList<Trap> traps, ArrayList<Organism> orgs) {
		if (destX == -1 || destY == -1) {
			while (destX < 0 || destX >= Map.width || destY < 0 || destY >= Map.height) {
				if (rand.nextFloat() + 0.1f > intel) {
					int advX = x < 20 ? 1 : x > 60 ? -1 : rand.nextInt(3) - 1;
					int advY = y < 10 ? 1 : y > 30 ? -1 : rand.nextInt(3) - 1;
					destX = x + advX * sense;
					destY = y + advY * sense;
				} else if (rand.nextFloat() > conf) {
					ArrayList<ScanPoint> options = new ArrayList<ScanPoint>();
					ArrayList<Point> optimized = new ArrayList<Point>();
					float maxReward = -1_000f;
					for (int yy = -sense; yy <= sense; yy++) {
						if (y + yy < 0 || y + yy >= Map.height) continue;
						for (int xx = -sense; xx <= sense; xx++) {
							if (x + xx < 0 || x + xx >= Map.width) continue;
							float reward = brain.visual[x + xx][y + yy];
							if (x + xx != x && y + yy != y && x + xx != prevX && y + yy != prevY) {
								options.add(new ScanPoint(reward, new Point(x + xx, y + yy)));
								if (reward > maxReward) maxReward = reward;
							}
						}
					}
					for (ScanPoint sp : options)
						if (sp.value == maxReward) optimized.add(sp.point);
					Point chosen = optimized.size() == 0 ? new Point(x, y) : optimized.get(rand.nextInt(optimized.size()));
					destX = chosen.x;
					destY = chosen.y;
				} else {
					ArrayList<ScanPoint> points = new ArrayList<ScanPoint>();
					float dist = sense * 2 + 1;
					for (int yy = -sense; yy <= sense; yy++) {
						int yOffs = y + yy;
						if (yOffs < 0 || yOffs >= Map.height) continue;
						for (int xx = -sense; xx <= sense; xx++) {
							int xOffs = x + xx;
							if (xOffs < 0 || xOffs >= Map.width) continue;
							if (xOffs == x && yOffs == y) continue;
							for (Food f : food)
								if (f.getX() == xOffs && f.getY() == yOffs) {
									float delta = (float) Math.sqrt((xOffs - x) * (xOffs - x) + (yOffs - y) * (yOffs - y));
									points.add(new ScanPoint(delta, new Point(xOffs, yOffs)));
									if (delta < dist) dist = delta;
								}
						}
					}
					if (points.size() > 0) {
						ArrayList<Point> options = new ArrayList<Point>();
						for (ScanPoint sp : points)
							if (sp.value == dist) options.add(sp.point);
						Point chosen = options.get(rand.nextInt(options.size()));
						destX = chosen.x;
						destY = chosen.y;
					}
				}
			}
		}
		if (ALLOW_HUNTING) for (Organism o : orgs) {
			double dist = Math.sqrt((o.getX() - x) * (o.getX() - x) + (o.getY() - y) * (o.getY() - y));
			if (!o.getSpecies().equals(getSpecies()) && dist <= sense && o.getSize() < size) {
				if (o.x == x && o.y == y) {
					o.dead = true;
					health = strength;
					feed();
					destX = -1;
					destY = -1;
				} else {
					destX = o.x;
					destY = o.y;
				}
			}
		}
	}

	private void findPathHome() {
		int simX = x;
		int simY = y;
		while (true) {
			int dir = getScanDir(simX, simY, homeX, homeY);
			if (dir == SCAN_DIR_NONE) break;
			Point[] scanned = getScannedTiles(dir);
			float maxReward = -1_000f;
			Point target = new Point(((homeX - simX) == 0 ? simX : simX + (homeX - simX) / Math.abs(homeX - simX)), ((homeY - simY) == 0 ? simY : simY + (homeY - simY) / Math.abs(homeY - simY)));
			for (Point p : scanned) {
				int targetX = simX + p.x;
				int targetY = simY + p.y;
				if (targetX < 0 || targetX >= Map.width || targetY < 0 || targetY >= Map.height) continue;
				float v = brain.visual[targetX][targetY];
				if (v > maxReward && !hasScanned(targetX, targetY)) {
					target = new Point(targetX, targetY);
					maxReward = v;
				}
			}
			pathHome.push(target);
			simX = target.x;
			simY = target.y;
		}
		Collections.reverse(pathHome);
	}

	private int getScanDir(int x, int y, int tx, int ty) {
		int dir = -1;
		if (x > tx) {
			if (y > ty) {
				dir = SCAN_DIR_NW;
			} else if (y < ty) {
				dir = SCAN_DIR_SW;
			} else {
				dir = SCAN_DIR_W;
			}
		} else if (x < tx) {
			if (y > ty) {
				dir = SCAN_DIR_NE;
			} else if (y < ty) {
				dir = SCAN_DIR_SE;
			} else {
				dir = SCAN_DIR_E;
			}
		} else {
			if (y > ty) {
				dir = SCAN_DIR_N;
			} else if (y < ty) {
				dir = SCAN_DIR_S;
			} else {
				dir = SCAN_DIR_NONE;
			}
		}
		return dir;
	}

	private Point[] getScannedTiles(int scanDir) {
		switch (scanDir) {
		case SCAN_DIR_N:
			return new Point[] {
					new Point(-1, -1), new Point(0, -1), new Point(1, -1)
			};
		case SCAN_DIR_NE:
			return new Point[] {
					new Point(0, -1), new Point(1, -1), new Point(1, 0)
			};
		case SCAN_DIR_E:
			return new Point[] {
					new Point(1, -1), new Point(1, 0), new Point(1, 1)
			};
		case SCAN_DIR_SE:
			return new Point[] {
					new Point(1, 0), new Point(1, 1), new Point(0, 1)
			};
		case SCAN_DIR_S:
			return new Point[] {
					new Point(-1, 1), new Point(0, 1), new Point(1, 1)
			};
		case SCAN_DIR_SW:
			return new Point[] {
					new Point(-1, 0), new Point(-1, 1), new Point(0, 1)
			};
		case SCAN_DIR_W:
			return new Point[] {
					new Point(-1, 1), new Point(-1, 0), new Point(-1, 1)
			};
		case SCAN_DIR_NW:
			return new Point[] {
					new Point(-1, 0), new Point(-1, -1), new Point(0, -1)
			};
		}
		return null;
	}

	private boolean hasScanned(int x, int y) {
		ArrayList<Point> path = new ArrayList<Point>(pathHome);
		for (Point p : path)
			if (p.x == x && p.y == y) return true;
		return false;
	}

	private void move(int dx, int dy, ArrayList<Food> food, ArrayList<Trap> traps, ArrayList<Organism> orgs) {
		dirX = dx == 1 ? EAST : dx == -1 ? WEST : NEUTRAL;
		dirY = dy == 1 ? SOUTH : dy == -1 ? NORTH : NEUTRAL;
		if (steps % (int) wait == 0) steps = 0;
		else
			return;
		if (steps % stamina == 0) wait += 0.05f;
		if (x + dx >= 0 && x + dx < Map.width) {
			prevX = x;
			x += dx;
		}
		if (y + dy >= 0 && y + dy < Map.height) {
			prevY = y;
			y += dy;
		}
		if (goingHome && !(x == homeX && y == homeY)) {
			next = pathHome.pop();
		} else if (!goingHome) {
			brain.history.add(new Point(x, y));
			getTarget(food, traps, orgs);
		}
	}

	private Trap getTrapAt(int x, int y, ArrayList<Trap> traps) {
		for (Iterator<Trap> iter = traps.iterator(); iter.hasNext();) {
			Trap t = iter.next();
			if (t.getX() == x && t.getY() == y) return t;
		}
		return null;
	}

	private Food getFoodAt(int x, int y, ArrayList<Food> food) {
		for (Iterator<Food> iter = food.iterator(); iter.hasNext();) {
			Food f = iter.next();
			if (f.getX() == x && f.getY() == y) return f;
		}
		return null;
	}

	private void setWait() {
		wait = (float) ((11.0 - (10.0 / (1.0 + Math.pow(Math.E, -0.5 * (speed - 5))))));
		if (wait == 0) wait = 1;
	}

	public void feed() {
		if (surplus) return;
		meals++;
		if (meals > size / 3) fed = true;
		if (fed && meals > 2 * size / 3) surplus = true;
		else if (fed && meals < 2 * size / 3 && rand.nextFloat() < intel) storage = true;
		steps = 0;
	}

	public void refresh() {
		destX = -1;
		destY = -1;
		steps = 0;
		meals = 0;
		x = homeX;
		y = homeY;
		age++;
		brain.history.clear();
		health = strength;
		pathHome.clear();
		next = null;
		goingHome = false;
		fed = false;
		surplus = false;
		dead = false;
		if (storage) {
			meals++;
			storage = false;
		}
		setWait();
		memoryLoss();
	}

	public void goHome() {
		findPathHome();
		goingHome = true;
	}

	public int getSize() {
		return size;
	}

	public int getSpeed() {
		return speed;
	}

	public int getStamina() {
		return stamina;
	}

	public int getSense() {
		return sense;
	}

	public int getConf() {
		return conf;
	}

	public int getX() {
		return x;
	}

	public int getAge() {
		return age;
	}

	public int getY() {
		return y;
	}

	public boolean isFed() {
		return fed;
	}

	public boolean isDead() {
		return dead || health == 0f;
	}

	public boolean hasSurplus() {
		return surplus && x == homeX && y == homeY;
	}

	public float getStrength() {
		return strength;
	}

	public float getIntel() {
		return intel;
	}

	public float getHealth() {
		return health;
	}

	public Point getHome() {
		return new Point(homeX, homeY);
	}

	public Point getDestination() {
		return new Point(destX, destY);
	}

	public Point getDirection() {
		return new Point(dirX, dirY);
	}

	public OrgImg getAppearance() {
		return new OrgImg(this);
	}

	public String getSpecies() {
		return Species.getSpecies(this);
	}

	public Brain getBrain() {
		return brain;
	}

	public void setBrain(Brain brain) {
		this.brain = brain;
	}

	public float getAnimProgress() {
		return (float) steps / wait;
	}

	public ArrayList<Point> getPathHome() {
		return new ArrayList<Point>(pathHome);
	}

	private static class ScanPoint {

		private float value;

		private Point point;

		private ScanPoint(float value, Point point) {
			this.value = value;
			this.point = point;
		}
	}
}
