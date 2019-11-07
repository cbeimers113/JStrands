package natsel.organism;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Species {

	public static String[] SPEC_NAMES = new String[256];

	public static String getSpecies(Organism o) {
		String strand = (o.getSize() > Organism.MAX_SIZE / 2 ? "1" : "0") + (o.getSpeed() > Organism.MAX_SPEED / 2 ? "1" : "0") + (o.getStamina() > Organism.MAX_STAMINA / 2 ? "1" : "0") + (o.getSense() < Organism.MAX_SENSE / 4 ? "00" : o.getSense() > Organism.MAX_SENSE / 4 && o.getSense() < Organism.MAX_SENSE / 2 ? "01" : o.getSense() > Organism.MAX_SENSE / 2 && o.getSense() < 3 * Organism.MAX_SENSE / 4 ? "10" : "11") + (o.getConf() > Organism.MAX_CONF / 2f / 2 ? "1" : "0") + (o.getStrength() > 0.5f ? "1" : "0") + (o.getIntel() > Organism.MAX_CONF / 2f ? "1" : "0");
		int r = 0;
		for (int i = strand.length() - 1; i >= 0; i--)
			if (strand.charAt(i) == '1') r += Math.pow(2, strand.length() - 1 - i);
		return SPEC_NAMES[r];
	}

	public static void loadSpeciesNames() {
		BufferedReader br = new BufferedReader(new InputStreamReader(Species.class.getResourceAsStream("/species.txt")));
		String line;
		try {
			int i = 0;
			while ((line = br.readLine()) != null)
				SPEC_NAMES[i++] = line;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
