package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.utils.RandomUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import cern.jet.random.Binomial;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class UniCoupledtoUniformOutcrossing implements OutcrossingStrategy {

	/**
	 * the outcrossing determination and coupling here are at total population
	 * level; thus when set by fitness - are more governed by
	 * (non-relative)fitness (when pop is large)
	 */
	private static final long serialVersionUID = 530546326530519835L;
	protected static final Logger logger = Logger
			.getLogger(CoupledToUniformOutcrossingPopLevel.class);

	protected double outcrossingCost = Double.NaN;
	protected double stepValue = Double.NaN;

	protected transient LinkedList<Outcrosser> uniform;
	protected transient LinkedList<Outcrosser> fao;
	protected transient HashMap<Outcrosser, Population> outcrosser2deme;

	public UniCoupledtoUniformOutcrossing() {
		uniform = Lists.newLinkedList();
		fao = Lists.newLinkedList();
		outcrosser2deme = Maps.newHashMap();
	}

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
	public void outcross(List<Population> demes, Population pool) {
		double uniformOutcrossingTendency = Double.NaN;
		double invaderOutcrossingTendency = Double.NaN;
		for (Population deme : demes) {
			for (Outcrosser o : deme) {
				outcrosser2deme.put(o, deme);
				if (o.isFao()) {
					fao.add(o);
				} else {
					uniform.add(o);
				}
			}
		}

		uniformOutcrossingTendency = fao.size() == 0 ? uniform.get(0)
				.getUniformOutcrossingProbability() : fao.get(0)
				.getUniformOutcrossingProbability();

		Collections.shuffle(uniform);
		Collections.shuffle(fao);

		// feedback doesn't make much sense in coupling and outcrossing
		// on
		// population level. and there is no need to apply density
		// dependence

		// tendency of an arbitrary Outcrosser, all have the field
		// UniformOutcrossingProbability:

		int uniformOutcrossings = uniform.size() > 0 ? Binomial.staticNextInt(
				uniform.size(), uniformOutcrossingTendency) : 0;
		logger.debug("Number of uniform outcrossings: " + uniformOutcrossings);
		// coupling to the rate. coupling to absolute number (i.e. to
		// filteredOutcrossing, or in the Binomial have filteredList.size()
		// as parameter instead of nonFilteredList.size()) may create an
		// artifact of increase or decrease in total outcrossing rate:
		if (invaderOutcrossingTendency == Double.NaN
				|| invaderOutcrossingTendency < 0
				|| invaderOutcrossingTendency > 1)
			throw new IllegalArgumentException(
					"invaderOutcrossingTendency should be a non-negative fraction");
		int faoOutcrossings = fao.size() > 0 ? Binomial.staticNextInt(
				fao.size(), invaderOutcrossingTendency) : 0;
		logger.debug("Number of FAO outcrossings: " + faoOutcrossings);

		/*
		 * TODO :change accessing the model, give it as a parameter instead
		 * through the.xml file
		 */
		// ** outcrossing for uniform
		int effectiveOutcrossings = 0;
		for (int i = 0; i < uniformOutcrossings && uniform.size() > 0; i++) {
			Outcrosser o = uniform.pop();

			outcrosser2deme.get(o).remove(o);
			pool.add(o);
			o.addFitnessCost(getOutcrossingCost());
			effectiveOutcrossings++;
		}

		// ** outcrossing for not-filtered:
		// relative fitness approach by the roulette and
		// iteration running until there are enough outcrossers, chosen by
		// comparing tendency(in FAO dependent on fitness) with a 0<random<1:

		for (int i = 0; i < faoOutcrossings && fao.size() > 0; i++) {
			Outcrosser o = fao.pop();

			outcrosser2deme.get(o).remove(o);
			pool.add(o);
			o.addFitnessCost(getOutcrossingCost());
			effectiveOutcrossings++;

		}
		uniform.clear();
		fao.clear();
		outcrosser2deme.clear();

	}

	protected void faoCoupledOutcrossingRoutine(Outcrosser o) {
		fao.remove(o);
		outcrosser2deme.get(o).remove(o);
		o.addFitnessCost(getOutcrossingCost());
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

	protected double[] getRouletteFemalesAndMales(
			List<Outcrosser> listOfOutcrossers, boolean fitRoulette) {
		double[] roulette = null;
		if (roulette == null) { // lazy init
			List<Outcrosser> outcrossers = listOfOutcrossers;
			roulette = new double[outcrossers.size()];
			double sum = 0;
			for (int i = 0; i < roulette.length; i++) {
				if (!fitRoulette) {
					sum += (1 - outcrossers.get(i).getFitness());
				} else {
					sum += outcrossers.get(i).getFitness();
				}

				roulette[i] = sum;
			}
			for (int i = 0; i < roulette.length; i++) {
				roulette[i] = roulette[i] / sum;
			}
			// sanity check
			if (roulette.length > 0 && roulette[0] < 0 && !fitRoulette
					|| roulette.length > 0 && roulette[0] <= 0 && fitRoulette) {
				throw new RuntimeException(
						"First item in roulette must be positive, but it is "
								+ roulette[0]);
			}
			if (roulette.length > 0 && roulette[roulette.length - 1] != 1
					&& fitRoulette || roulette.length > 0
					&& roulette[roulette.length - 1] < 0 && !fitRoulette) {
				throw new RuntimeException(
						"Last item in roulette must be 1, but it is "
								+ roulette[roulette.length - 1]);
			}// end sanity check

		}
		return roulette;
	}

	@Override
	public void outcross(Population deme, Population pool) {
		throw new NotImplementedException();
	}

	@Override
	public int getEffectiveOutcrossings() {
		// TODO Auto-generated method stub
		return Integer.MIN_VALUE;
	}

	// //old version - non roulette but with week relative fitness due to the
	// //running until completing quota - but with problems when all are
	// //fitness==1
	// for (int i = 0; i < faoOutcrossings && fao.size() > 0; i++) {
	// Outcrosser o = fao.pop();
	//
	// if (RandomUtils.nextDouble() < 1 - o.getFitness()) {
	// outcrosser2deme.get(o).remove(o);
	// pool.add(o);
	// o.addFitnessCost(getOutcrossingCost());
	// effectiveOutcrossings++;
	// } else {
	// logger.debug("searching for FAO outcrossers: " + i);
	// fao.add(o);
	// i--;
	// }
	// }
}