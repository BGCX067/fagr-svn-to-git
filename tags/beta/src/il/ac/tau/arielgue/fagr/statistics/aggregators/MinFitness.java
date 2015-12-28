package il.ac.tau.arielgue.fagr.statistics.aggregators;

import il.ac.tau.arielgue.fagr.Outcrosser;
import il.ac.tau.yoavram.pes.statistics.aggregators.Minimum;

public class MinFitness extends Minimum<Outcrosser> {

	@Override
	protected double extractData(Outcrosser o) {
		return o.getFitness();
	}

}
