package natsel.utils.components;

import natsel.Main;
import natsel.utils.Button;

public class PauseButton extends Button {

	public PauseButton(Main sim) {
		super("Pause", sim, "Play");
	}

	public void action() {
		sim.togglePause();
		toggleText();
	}
}
