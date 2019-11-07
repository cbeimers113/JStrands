package natsel.environment;

public class Food {

	private int x;
	private int y;

	private float healing;

	private boolean eaten;

	public Food(int x, int y, float healing) {
		this.x = x;
		this.y = y;
		this.healing = healing;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public float getHealing() {
		return healing;
	}

	public boolean isEaten() {
		return eaten;
	}

	public void setEaten(boolean eaten) {
		this.eaten = eaten;
	}
}
