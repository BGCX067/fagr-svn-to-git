package il.ac.tau.arielgue.fagr.statistics.aggregators;

import il.ac.tau.arielgue.fagr.Outcrosser;
import il.ac.tau.yoavram.pes.statistics.aggregators.StandardDeviation;

import org.apache.log4j.Logger;

public class StandardDeviationFitness extends StandardDeviation<Outcrosser> {
	protected static final Logger logger = Logger
			.getLogger(StandardDeviationFitness.class);

	@Override
	protected double extractData(Outcrosser o) {
		return o.getFitness();
	}
}
