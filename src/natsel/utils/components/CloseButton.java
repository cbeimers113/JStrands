package natsel.utils.components;

import natsel.Main;
import natsel.utils.Button;

public class CloseButton extends Button {

	public CloseButton(Main sim) {
		super("Exit", sim);
	}

	public void action() {
		sim.stop();
	}
}
