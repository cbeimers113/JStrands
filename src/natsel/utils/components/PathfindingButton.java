package natsel.utils.components;

import natsel.Main;
import natsel.utils.Button;

public class PathfindingButton extends Button {

	public PathfindingButton(Main sim) {
		super("Show Pathfinding", sim, "Hide Pathfinding");
	}

	public void action() {
		sim.getMap().setDrawingPaths(!sim.getMap().isDrawingPaths());
		toggleText();
	}
}
