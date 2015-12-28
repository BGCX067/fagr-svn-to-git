package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.Simulation;
import il.ac.tau.yoavram.pes.utils.RandomUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import cern.jet.random.Binomial;

import com.google.common.collect.Lists;

public class CoupledToUniformOutcrossing implements OutcrossingStrategy {
	/**
	 * the outcrossing determination and coupling here are at deme level so
	 * biases may appear; it is also to some extent with relative fitness
	 * approach
	 */
	private static final long serialVersionUID = 2214017411287337493L;
	private static final Logger logger = Logger
			.getLogger(CoupledToUniformOutcrossing.class);

	private double outcrossingCost;
	private double stepValue;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * il.ac.tau.arielgue.fagr.FeedbackOutcrossing#outcross(il.ac.tau.arielgue
	 * .fagr.Population, il.ac.tau.arielgue.fagr.Population) in coupled
	 * outcrossing, first the enforcing subpopulation will outcross according to
	 * the supplied filter (in most cases, UniFilter) and then the non-filtered
	 * will outcross according to the formers' ratio:
	 */
	@Override
	public void outcross(Population deme, Population pool) {
		int originalDemeSize = deme.size();
		if (originalDemeSize == 0)
			return;
		double density = deme.getDensity();

		LinkedList<Outcrosser> uniform = Lists.newLinkedList();
		LinkedList<Outcrosser> fao = Lists.newLinkedList();

		for (Outcrosser o : deme) {
			if (o.isFao()) {
				fao.add(o);
			} else {
				uniform.add(o);
			}
		}

		Collections.shuffle(uniform);
		Collections.shuffle(fao);

		// density of an arbitrary Outcrosser, all have the field
		// UniformOutcrossingProbability:
		double uniformOutcrossingTendencyWithoutDensity = deme.get(0)
				.getUniformOutcrossingProbability();
		double uniformOutcrossingTendencyWithDensity = deme.get(0)
				.applyDensityDependence(
						uniformOutcrossingTendencyWithoutDensity, density);

		int uniformOutcrossings = uniform.size() > 0 ? Binomial.staticNextInt(
				uniform.size(), uniformOutcrossingTendencyWithDensity) : 0;
		logger.debug("Number of uniform outcrossings: " + uniformOutcrossings);
		int faoOutcrossings = fao.size() > 0 ? Binomial.staticNextInt(
				fao.size(), uniformOutcrossingTendencyWithDensity) : 0;
		logger.debug("Number of fao outcrossings: " + uniformOutcrossings);

		int allWithoutDensity = Binomial.staticNextInt(deme.size(),
				uniformOutcrossingTendencyWithoutDensity);

		double feedback = 1
				+ ((double) (uniformOutcrossings + faoOutcrossings - allWithoutDensity) / deme
						.size())
				* (((OutcrossModel) Simulation.getInstance().getModel())
						.getFeedBackWeight());
		int effectiveOutcrossings = 0;
		/*
		 * TODO :change accessing the model, give it as a parameter instead
		 * through the xml
		 */
		// ** outcrossing for uniform
		int effectiveUniformOutcrossings = (int) Math.round(uniformOutcrossings
				* feedback);
		for (int i = 0; i < effectiveUniformOutcrossings && uniform.size() > 0; i++) {
			Outcrosser o = uniform.pop();

			deme.remove(o);
			pool.add(o);
			o.addFitnessCost(getOutcrossingCost());
			effectiveOutcrossings++;
		}

		// outcrossing for fao
		/*
		 * relative fitness approach partially satisfied by the WHILE iteration
		 * running until there are enough outcrossers, chosen by comparing
		 * tendency (in FAO dependent on fitness) with a rand:
		 */

		int effectiveFaoOutcrossings = (int) Math.round(faoOutcrossings
				* feedback);
		for (int i = 0; i < effectiveFaoOutcrossings && fao.size() > 0; i++) {
			Outcrosser o = fao.pop();

			if (RandomUtils.nextDouble() < o.applyDensityDependence(
					1 - o.getFitness(), density)) {
				deme.remove(o);
				pool.add(o);
				o.addFitnessCost(getOutcrossingCost());
				effectiveOutcrossings++;
			} else {
				fao.add(o);
				i--;
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
		for (Population deme : demes) {
			outcross(deme, pool);
		}
	}

	@Override
	public int getEffectiveOutcrossings() {
		// TODO Auto-generated method stub
		return Integer.MIN_VALUE;
	}
}