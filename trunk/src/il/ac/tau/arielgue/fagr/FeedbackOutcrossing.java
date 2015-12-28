package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.Simulation;
import il.ac.tau.yoavram.pes.utils.RandomUtils;

import java.util.List;

import org.apache.log4j.Logger;

public class FeedbackOutcrossing implements OutcrossingStrategy {
	private static final long serialVersionUID = -575206636670385648L;
	private static final Logger logger = Logger
			.getLogger(FeedbackOutcrossing.class);

	private double outcrossingCost = Double.NaN;
	private double stepValue = Double.NaN;

	@Override
	// first get the initial tendency of each outcrosser, as a function of its
	// inherent genome and the density(environment) - and use it to calculate
	// the
	// feedback:
	public void outcross(Population deme, Population pool) {
		double density = deme.getDensity();
		double sumWithDensity = 0;
		double demeSize = (double) deme.size();
		double sumWithoutDensity = 0;

		for (Outcrosser o : deme) {
			double tendency = o.getOutcrossingTendency();
			sumWithDensity += o.applyDensityDependence(tendency, density);
			sumWithoutDensity += tendency;
		}
		// if average is higher than
		// random (i.e., than the average inherent rate, without density effect)
		// then it will be a positive feedback, else it will be a negative
		// feedback. feedback is a FAGR attribute, due to locusts behavior,
		// another
		// effect after density dependence:
		double feedback = 1
				+ ((sumWithDensity - sumWithoutDensity) / demeSize)
				* (((OutcrossModel) Simulation.getInstance().getModel())
						.getFeedBackWeight());

		// now use the feedback to get the final actual tendency:

		// safe iteration, avoids concurrent modifications exception:
		for (int i = 0; i < deme.size();) {
			Outcrosser o = deme.get(i);
			if (RandomUtils.nextDouble() < o.applyDensityDependence(
					o.getOutcrossingTendency(), density)
					* feedback) {
				deme.remove(i);
				pool.add(o);
				o.addFitnessCost(getOutcrossingCost());
				logger.debug(String.format("Outcrosser %s  outcrossed ", o
						.getID().toString()));
			} else {
				i++;
			}
		}
	}

	public void setOutcrossingCost(double outcrossingCost) {
		this.outcrossingCost = outcrossingCost;
	}

	public double getOutcrossingCost() {
		return outcrossingCost;
	}

	public void setStepValue(double stepValue) {
		this.stepValue = stepValue;
	}

	public double getStepValue() {
		return stepValue;
	}

	@Override
	public void outcross(List<Population> demes, Population pool) {
		for (Population deme : demes)
			outcross(deme, pool);
	}

	@Override
	public int getEffectiveOutcrossings() {
		// TODO Auto-generated method stub
		return Integer.MIN_VALUE;
	}
}
