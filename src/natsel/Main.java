package natsel;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;

import natsel.environment.Map;
import natsel.organism.Species;
import natsel.utils.Button;
import natsel.utils.Graph;
import natsel.utils.components.CloseButton;
import natsel.utils.components.GraphButton;
import natsel.utils.components.PathfindingButton;
import natsel.utils.components.PauseButton;
import natsel.utils.components.SpawnButton;

public class Main extends Canvas implements Runnable, KeyListener, MouseListener, MouseMotionListener {

	private static final int BTN_PAUSE = 0;
	private static final int BTN_SPAWN = 1;
	private static final int BTN_PATHFINDING = 2;
	private static final int BTN_GRAPHS = 3;

	private static final int COL_PANEL = 0x499aba;

	public static final int DAY_LENGTH = 500;

	public static Graph graph;

	private static final long serialVersionUID = 1L;

	private static final String TITLE = "7Strands Evolution Simulator";

	private JFrame frame;
	private Thread thread;
	private Map map;
	private BufferedImage overworld;
	private ArrayList<Button> controls;
	private Dimension maximized;
	private Dimension minimized;

	private volatile boolean paused;
	private boolean running;
	private boolean render;
	private boolean scaling;
	private boolean drawGraphs;

	private int ticks;
	private int generations;
	private int mx;
	private int my;

	private float mapScale;
	private float[] stats;

	public Main() {
		render = true;
		map = new Map(width(), height());
		map.spawn(1);
		stats = new float[map.stats.length];
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		frame = new JFrame();
		frame.add(this);
		frame.addKeyListener(this);
		frame.addMouseListener(this);
		frame.addMouseMotionListener(this);
		frame.setPreferredSize(maximized = new Dimension(width(), height()));
		frame.setUndecorated(true);
		frame.pack();
		frame.setVisible(render);
		frame.setLocation(new Point(0, 0));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(TITLE);
		frame.setResizable(false);
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/icon.png")));
		frame.pack();
		generations = 1;
		mapScale = 1f;
		overworld = new BufferedImage(Map.width * Map.CELL, Map.height * Map.CELL, BufferedImage.TYPE_INT_ARGB);
		controls = new ArrayList<Button>();
		controls.add(new PauseButton(this));
		controls.add(new SpawnButton(this));
		controls.add(new PathfindingButton(this));
		controls.add(new GraphButton(this));
		controls.add(new CloseButton(this));
		minimized = new Dimension(getWidth() - 600, getHeight());
		start();
	}

	private synchronized void start() {
		if (running) return;
		running = true;
		thread = new Thread(this);
		thread.start();
	}

	public synchronized void stop() {
		if (!running) return;
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			save();
			System.exit(0);
		}
	}

	@Deprecated
	private void save() {

	}

	public void run() {
		while (running) {
			update();
			render();
		}
	}

	public void fixFrame() {
		frame.setPreferredSize(drawGraphs ? maximized : minimized);
		frame.pack();
	}

	private void update() {
		if (!paused) {
			map.update();
			if (++ticks % DAY_LENGTH == 0) {
				map.refresh();
				generations++;
				ticks = 0;
				for (int i = 0; i < map.stats.length; i++)
					try {
						float f = Float.parseFloat(map.stats[i]);
						if (i == Graph.INDX_STRENGTH || i == Graph.INDX_INTEL) stats[i] = -100.f * f;
						else
							stats[i] = -f;
					} catch (NumberFormatException e) {
						stats[i] = 0;
					}
				graph.updateStats(stats);
				controls.get(BTN_SPAWN).setEnabled(!map.allTaken());
			}
		}
		for (Button button : controls)
			button.update(mx, my);
		if (scaling) {
			if (mapScale > 0.25f) mapScale -= 0.01f;
			else
				drawGraphs = true;
		} else {
			if (mapScale < 1f) mapScale += 0.01f;
			drawGraphs = false;
		}
	}

	private void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		g.setColor(new Color(COL_PANEL));
		g.fillRect(0, 0, getWidth(), getHeight());
		if (true) {
			Graphics owg = overworld.getGraphics();
			map.render(owg);
			owg.dispose();
			g.drawImage(overworld, 0, 0, (int) (mapScale * Map.width * Map.CELL), (int) (mapScale * Map.height * Map.CELL), null);
			g.setColor(Color.YELLOW);
			g.fillRect(5, (int) (mapScale * (getHeight() - 20)) - 5, (int) (((float) (DAY_LENGTH - ticks) / (float) DAY_LENGTH) * (Map.width * Map.CELL * mapScale - 10)), (int) (mapScale * 15));
		}
		for (int i = 0; i < controls.size(); i++)
			controls.get(i).render(Map.width * Map.CELL + 5, 5 + i * 30, g);
		if (drawGraphs) {
			g.setColor(new Color(Graph.COL_TEXT));
			g.drawRoundRect((int) (Map.width * Map.CELL * mapScale) + 3, 2, (int) (Map.width * Map.CELL * mapScale) - 22, (int) (Map.height * Map.CELL * mapScale) - 4, 25, 25);
			graph.graphTraits(g, 0, 275);
			graph.graphRankings(g, (int) (Map.width * Map.CELL * mapScale) + 25, (int) (Map.height * Map.CELL * mapScale) + 5);
			graph.graphVisualCortex(g, (int) (Map.width * Map.CELL * mapScale) * 2, 275);
			graph.graphTopSpec(g, (int) (Map.width * Map.CELL * mapScale) * 2, 40);
			graph.graphHistory(g, (int) (Map.width * Map.CELL * mapScale) * 2, 450);
		}
		g.dispose();
		bs.show();
	}

	public void toggleRender() {
		render = !render;
		frame.setVisible(render);
	}

	public void togglePause() {
		paused = !paused;
	}

	public void toggleShowGraphs() {
		scaling = !scaling;
	}

	private int width() {
		int w = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		return w;
	}

	private int height() {
		int h = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		return h;
	}

	public int getGeneration() {
		return generations;
	}

	public boolean isPaused() {
		return paused;
	}

	public boolean isShowingGraphs() {
		return scaling && drawGraphs;
	}

	public Map getMap() {
		return map;
	}

	public void focus() {
		requestFocus();
	}

	public void keyTyped(KeyEvent e) {

	}

	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			if (scaling) controls.get(BTN_GRAPHS).action();
			break;
		case KeyEvent.VK_DOWN:
			if (!scaling) controls.get(BTN_GRAPHS).action();
			break;
		case KeyEvent.VK_SPACE:
			controls.get(BTN_PAUSE).action();
			break;
		case KeyEvent.VK_P:
			controls.get(BTN_PATHFINDING).action();
			break;
		case KeyEvent.VK_S:
			controls.get(BTN_SPAWN).action();
			break;
		}
	}

	public void keyReleased(KeyEvent e) {

	}

	public void mouseDragged(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
	}

	public void mouseMoved(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
	}

	public void mouseClicked(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
		for (Button button : controls)
			button.tryAction(mx, my);
	}

	public void mousePressed(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
	}

	public void mouseReleased(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public static void main(String[] args) {
		Species.loadSpeciesNames();
		Main sim = new Main();
		graph = new Graph(sim);
	}

}
