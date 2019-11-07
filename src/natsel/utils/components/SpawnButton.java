package natsel.utils.components;

import java.util.Random;

import natsel.Main;
import natsel.environment.Map;
import natsel.organism.Organism;
import natsel.utils.Button;

public class SpawnButton extends Button {

	private static final Random rand = new Random();

	public SpawnButton(Main sim) {
		super("Spawn Organism", sim);
	}

	public void action() {
		int sx = -1;
		int sy = -1;
		boolean spotTaken = true;
		while (spotTaken) {
			if (rand.nextBoolean()) { // spawn along vertical
				sy = rand.nextInt(Map.height);
				sx = rand.nextBoolean() ? 0 : (Map.width - 1);
			} else { // spawn along horizontal
				sy = rand.nextBoolean() ? 0 : (Map.height - 1);
				sx = rand.nextInt(Map.width);
			}
			for (Organism o : sim.getMap().getOrganisms())
				if (o.getHome().x != sx && o.getHome().y != sy) spotTaken = false;
			if (sim.getMap().getPopulation() == 0) spotTaken = false;
		}
		sim.getMap().addToWaitlist(new Organism(rand.nextInt(3) + 1, rand.nextInt(3) + 1, rand.nextInt(3) + 1, rand.nextFloat(), rand.nextFloat(), sx, sy));
		if (allTaken()) disable();
	}

	private boolean allTaken() {
		return sim.getMap().getPopulation() == 2 * (Map.width - 3) + 2 * (Map.height - 1);
	}
}
