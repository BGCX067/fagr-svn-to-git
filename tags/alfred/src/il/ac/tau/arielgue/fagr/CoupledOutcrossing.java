package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.utils.RandomUtils;

import org.apache.log4j.Logger;

public class CoupledOutcrossing extends FeedbackOutcrossing {
	private static final long serialVersionUID = -4713449945928212131L;
	private static final Logger logger = Logger
			.getLogger(CoupledOutcrossing.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * il.ac.tau.arielgue.fagr.FeedbackOutcrossing#outcross(il.ac.tau.arielgue
	 * .fagr.Population, il.ac.tau.arielgue.fagr.Population) in coupled
	 * outcrossing, first the enforcing subpopulation will outcross according to
	 * the supplied filter (in most cases, FaoFilter) and then the non-filtered
	 * will outcross according to the formers' ratio:
	 */
	@Override
	public double outcross(Population deme, Population pool) {
		double ratio = super.outcross(deme, pool);
		for (int i = 0; i < deme.size(); i++) {
			Outcrosser o = deme.get(i);
			if (getFilter() != null && !getFilter().filter(o)) {
				if (RandomUtils.nextDouble() < ratio) {
					deme.remove(o);
					pool.add(o);
					o.addFitnessCost(getOutcrossingCost());
					i--;
					logger.debug(o.getID() + " outcrossed");
				}
			}
		}
		return ratio;
	}
}