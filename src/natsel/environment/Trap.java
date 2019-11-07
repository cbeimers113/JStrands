package natsel.environment;

public class Trap {

	private int x;
	private int y;

	private float damage;

	public Trap(int x, int y, float damage) {
		this.x = x;
		this.y = y;
		this.damage = damage;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public float getDamage() {
		return damage;
	}
}
