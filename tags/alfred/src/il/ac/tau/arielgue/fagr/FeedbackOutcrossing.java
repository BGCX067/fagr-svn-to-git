package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.filters.Filter;
import il.ac.tau.yoavram.pes.utils.RandomUtils;

import org.apache.log4j.Logger;

public class FeedbackOutcrossing implements OutcrossingStrategy {
	private static final long serialVersionUID = -575206636670385648L;
	private static final Logger logger = Logger
			.getLogger(FeedbackOutcrossing.class);

	private double outcrossingCost = Double.NaN;
	private Filter<Outcrosser> filter = null;

	@Override
	// first get the initial tendency of each outcrosser, as a function of its
	// inherent genome and the density(environment) - and use it to calculate
	// the
	// feedback:
	public double outcross(Population deme, Population pool) {

		double density = deme.getDensity();
		double sum = 0;
		double filtered = 0;
		double sumOfInherentTendencies = 0;
		for (Outcrosser o : deme) {
			if (getFilter() == null || getFilter().filter(o)) {
				sum = sum + o.getOutcrossingTendency(density)[1];
				sumOfInherentTendencies = sumOfInherentTendencies
						+ (o.getOutcrossingTendency(density)[0]);
				filtered++;
			}
		}
		// if average is higher than
		// random (i.e., than the average inherent rate, without density effect)
		// then it will be a positive feedback, else it will be a negative
		// feedback:
		double feedback = 1 + ((sum / filtered) - (sumOfInherentTendencies / filtered));

		// now use the feedback to get the final actual tendency:
		double outcrossed = 0;
		double total = 0;

		// safe iteration, avoids concurrent modifications exception:

		for (int i = 0; i < deme.size(); i++) {
			Outcrosser o = deme.get(i);
			if (getFilter() == null || getFilter().filter(o)) {
				total++;
				if (RandomUtils.nextDouble() < o
						.getOutcrossingTendency(density)[1] * feedback) {
					deme.remove(i);
					pool.add(o);
					o.addFitnessCost(getOutcrossingCost());
					i--;
					logger.debug(String.format("Outcrosser %s  outcrossed ", o
							.getID().toString()));
				}
			}
		}

		// for (Outcrosser o : deme) {
		// if (getFilter() == null || getFilter().filter(o)) {
		// total++;
		// if (RandomUtils.nextDouble() < o.getOutcrossingTendency(density)
		// * feedback) {
		// deme.remove(o);
		// pool.add(o);
		// o.addFitnessCost(getOutcrossingCost());
		// outcrossed++;
		// logger.debug(o.getID() + " outcrossed");
		// }
		// }
		// }

		return outcrossed / total;
	}

	public void setOutcrossingCost(double outcrossingCost) {
		this.outcrossingCost = outcrossingCost;
	}

	public double getOutcrossingCost() {
		return outcrossingCost;
	}

	public void setFilter(Filter<Outcrosser> filter) {
		this.filter = filter;
	}

	public Filter<Outcrosser> getFilter() {
		return filter;
	}

}
