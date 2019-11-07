package natsel.utils;

import java.awt.Color;
import java.awt.Graphics;

import natsel.Main;

public abstract class Button {

	protected String text;
	protected String toggleText;
	protected String renderedText;
	protected Main sim;

	private int x;
	private int y;
	private int w;
	private int h;
	private int xOffs;
	private int minimizedOffs = 600;

	private boolean hover;
	private boolean enabled;

	public Button(String text, Main sim) {
		this.text = text;
		this.sim = sim;
		renderedText = text;
		enable();
	}

	public Button(String text, Main sim, String toggleText) {
		this.text = text;
		this.sim = sim;
		this.toggleText = toggleText;
		renderedText = text;
		enable();
	}

	public void render(int x, int y, Graphics g) {
		g.setFont(Graph.FONT_LABEL);
		this.x = x;
		this.y = y;
		w = Graph.stringWidth(renderedText, g) + 4;
		h = 25;
		g.setColor(hover ? Color.DARK_GRAY : Color.GRAY);
		g.fillRoundRect(x + xOffs - 2 - (sim.isShowingGraphs() ? minimizedOffs : 0), y - 2, w, h, 15, 15);
		g.setColor(hover ? Color.GRAY : Color.DARK_GRAY);
		g.drawString(renderedText, x + xOffs - (sim.isShowingGraphs() ? minimizedOffs : 0), y + 16);
		g.drawRoundRect(x + xOffs - 2 - (sim.isShowingGraphs() ? minimizedOffs : 0), y - 2, w, h, 15, 15);
	}

	public void update(int mx, int my) {
		mx = mx + (sim.isShowingGraphs() ? minimizedOffs : 0);
		if (enabled) hover = mx >= x + xOffs && mx <= x + xOffs + w && my >= y && my <= y + h;
	}

	public void tryAction(int mx, int my) {
		if (hover) action();
	}

	public void disable() {
		enabled = false;
	}

	public void enable() {
		enabled = true;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	protected void toggleText() {
		if (renderedText.equals(text)) renderedText = toggleText;
		else
			renderedText = text;
	}

	public int getWidth() {
		return w;
	}

	public int getHeight() {
		return h;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public abstract void action();
}
