package il.ac.tau.arielgue.fagr.statistics;

import java.io.IOException;

import il.ac.tau.arielgue.fagr.Outcrosser;
import il.ac.tau.yoavram.pes.Simulation;
import il.ac.tau.yoavram.pes.statistics.SimpleDataGatherer;

public class OutcrossingDataGatherer extends SimpleDataGatherer<Outcrosser> {

	@Override
	public void close() throws IOException {
		this.gather();
		super.close();

	}

	@Override
	public int getInterval() {
		if (Simulation.getInstance().getTick() == 1)
			return 1;
		else {
			return super.getInterval();
		}
	}
}
