package natsel.utils.components;

import natsel.Main;
import natsel.utils.Button;

public class GraphButton extends Button {

	public GraphButton(Main sim) {
		super("Show Graphs", sim, "Hide Graphs");
	}

	public void action() {
		sim.toggleShowGraphs();
		sim.fixFrame();
		toggleText();
	}
}
