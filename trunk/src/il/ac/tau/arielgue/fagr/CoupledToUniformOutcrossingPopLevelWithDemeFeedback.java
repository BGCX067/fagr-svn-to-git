package il.ac.tau.arielgue.fagr;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import il.ac.tau.yoavram.pes.utils.RandomUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.springframework.util.MultiValueMap;

import cern.jet.random.Binomial;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class CoupledToUniformOutcrossingPopLevelWithDemeFeedback extends
		CoupledToUniformOutcrossingPopLevel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1015480464290801912L;
	/**
	 * TODO: insert "alpha and beta" (from the deterministic model)
	 */

	private transient HashMap<Population, Double> demesEffectOnOutcrossingScore;
	private transient HashMap<Population, Integer> demesEffectOnOutcrossingScoreNominator;
	// private transient Integer outcrossersNominator;
	private transient HashMap<Outcrosser, Double> outcrosser2outcrossingTendency;

	public CoupledToUniformOutcrossingPopLevelWithDemeFeedback() {

		super();
		demesEffectOnOutcrossingScore = Maps.newHashMap();
		// outcrossersNominator = Integer.MIN_VALUE;
		outcrosser2outcrossingTendency = Maps.newHashMap();
		demesEffectOnOutcrossingScoreNominator = Maps.newHashMap();
	}

	@Override
	public void outcross(List<Population> demes, Population pool) {

		double uniformOutcrossingTendency = Double.NaN;
		for (Population deme : demes) {
			// starting with a positive effect of density above 0:
			/**
			 * TODO: consider a Hill Function instead *
			 */
			demesEffectOnOutcrossingScore.put(deme, (0.0 + deme.getDensity()));
			for (Outcrosser o : deme) {
				outcrosser2outcrossingTendency.put(o, 0.0);
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
		int uniformOutcrossings = 0;
		int faoOutcrossings = 0;
		// -> Coupling to the defacto rate. coupling to absolute number (i.e. to
		// filteredOutcrossing, or in the Binomial have filteredList.size()
		// as parameter instead of nonFilteredList.size()) may create an
		// artifact of increase or decrease in total outcrossing rate:
		logger.debug("Number of uniform outcrossings: " + uniformOutcrossings);
		logger.debug("Number of FAO outcrossings: " + faoOutcrossings);

		// ** outcrossing for uniform
		int effectiveOutcrossings = 0;

		// iterating first time - including only density dependence and no
		// feedback yet
		for (int i = 0; i < uniform.size(); i++) {
			Outcrosser o = uniform.get(i);
			double rand = RandomUtils.nextDouble();
			if (uniformOutcrossingTendency
					* demesEffectOnOutcrossingScore.get(outcrosser2deme.get(o)) < rand) {
				outcrosser2outcrossingTendency.put(o, 1.0);
				int demesEffectOnOutcrossingScoreNominatorValue = (demesEffectOnOutcrossingScoreNominator
						.get(outcrosser2deme.get(o)) == null) ? 0
						: demesEffectOnOutcrossingScoreNominator
								.get(outcrosser2deme.get(o));
				demesEffectOnOutcrossingScoreNominator.put(
						outcrosser2deme.get(o),
						demesEffectOnOutcrossingScoreNominatorValue + 1);
				uniformOutcrossings++;

			} else
				outcrosser2outcrossingTendency.put(
						o,
						uniformOutcrossingTendency
								* demesEffectOnOutcrossingScore
										.get(outcrosser2deme.get(o)));
		}
		// update faoOutcrossings value to other than 0, if so:
		if (uniformOutcrossings > 0 && fao.size() > 0) {
			faoOutcrossings = Math.round(fao.size() * uniformOutcrossings
					/ uniform.size());
		} else if (fao.size() > 0) {
			faoOutcrossings = Binomial.staticNextInt(fao.size(),
					uniformOutcrossingTendency);
		}
		// ** outcrossing for FAO:
		// relative fitness approach by the roulette(on the whole population and
		// not by demes: due to coupling being "artificial" any way) and
		// iteration running until there are enough outcrossers, chosen by
		// comparing tendency(in FAO dependent on fitness) with a 0<random<1:

		LinkedList<Outcrosser> faoWorkingCopy = fao;
		for (int i = 0; i < faoOutcrossings && fao.size() > 0; i++) {
			double[] roulette = getWithDemeEffectRouletteFemalesAndMales(
					faoWorkingCopy, false);
			int indexInRoulette = 0;
			if (roulette.length > 0) {
				double rand = RandomUtils.nextDouble();
				while (roulette[indexInRoulette] <= rand)
					indexInRoulette++;
				Outcrosser o = faoWorkingCopy.get(indexInRoulette);
				faoWorkingCopy.remove(indexInRoulette);
				// sanity output
				logger.debug("fitness of FAO outcrosser=" + o.getFitness());
				outcrosser2outcrossingTendency.put(o, 1.0);
				/**
				 * TODO: check if can be done at least in part during the
				 * roulette and save time?
				 */
				int demesFeedbackScoreNominatorValue = (demesEffectOnOutcrossingScoreNominator
						.get(outcrosser2deme.get(o)) == null) ? 0
						: demesEffectOnOutcrossingScoreNominator
								.get(outcrosser2deme.get(o));
				demesEffectOnOutcrossingScoreNominator.put(
						outcrosser2deme.get(o),
						demesFeedbackScoreNominatorValue + 1);

			}
		}
		faoWorkingCopy.clear();

		// //////////////////////////////////////////////////////////////////////////////////////////////////////////

		// now we need to go again through the demes, outcross the sure==1
		// outcrossers and effect the feedback:

		uniform.clear();
		fao.clear();
		for (Population deme : demes) {
			// starting with a positive effect of effective proportion above
			// the uniform outcrossing rate; changing the values in
			// demesFeedbackScore to new ones:
			/**
			 * TODO: consider a Hill Function instead *
			 */
			int demeFeedbackScoreValue = (demesEffectOnOutcrossingScoreNominator
					.get(deme) == null) ? 0
					: demesEffectOnOutcrossingScoreNominator.get(deme);
			demesEffectOnOutcrossingScore
					.put(deme,
							((1 - uniformOutcrossingTendency) + ((double) demeFeedbackScoreValue)
									/ deme.size()));

			for (int i = 0; i < deme.size();) {
				Outcrosser o = deme.get(i);
				if (outcrosser2outcrossingTendency.get(o) == 1) {
					deme.remove(i);
					pool.add(o);
					o.addFitnessCost(getOutcrossingCost());
					outcrosser2deme.remove(o);
					effectiveOutcrossings++;
				} else {
					i++;
					if (o.isFao()) {
						fao.add(o);
					} else {
						uniform.add(o);
					}
				}
			}
		}

		// ///////////////////////////////////////////////////////////////////////////////////////////////////////////

		// last, go through the lists, and couple FAO
		// by the uniform random results:
		// Uniforms:
		int uniformsOutcrossingByFeedback = 0;
		for (int i = 0; i < uniform.size();) {
			Outcrosser o = uniform.get(i);
			Population deme = outcrosser2deme.get(o);
			if (o.getOutcrossingTendency()
					* demesEffectOnOutcrossingScore.get(deme) > RandomUtils
					.nextDouble()) {
				deme.remove(o);
				uniform.remove(i);
				pool.add(o);
				o.addFitnessCost(getOutcrossingCost());
				outcrosser2deme.remove(o);
				uniformsOutcrossingByFeedback++;
				effectiveOutcrossings++;
			} else {
				i++;
			}
		}
		int faoformsOutcrossingByFeedback = 0;
		if (uniformsOutcrossingByFeedback > 0 && fao.size() > 0) {
			faoformsOutcrossingByFeedback = Math.round(fao.size()
					* uniformsOutcrossingByFeedback / uniform.size());
		} else if (fao.size() > 0) {
			faoformsOutcrossingByFeedback = faoformsOutcrossingByFeedback;
		}
		// Fao's:
		faoWorkingCopy = fao;
		for (int i = 0; i < faoformsOutcrossingByFeedback && fao.size() > 0; i++) {
			double[] roulette = getWithDemeEffectRouletteFemalesAndMales(
					faoWorkingCopy, false);
			int indexInRoulette = 0;
			if (roulette.length > 0) {
				double rand = RandomUtils.nextDouble();
				while (roulette[indexInRoulette] <= rand)
					indexInRoulette++;
				Outcrosser o = faoWorkingCopy.get(indexInRoulette);
				Population deme = outcrosser2deme.get(o);
				faoWorkingCopy.remove(indexInRoulette);
				deme.remove(o);
				pool.add(o);
				o.addFitnessCost(getOutcrossingCost());
				outcrosser2deme.remove(o);
				effectiveOutcrossings++;
				// sanity output
				logger.debug("fitness of FAO outcrosser=" + o.getFitness());
			}
		}
		faoWorkingCopy.clear();
		// after finishing the outcrossing step, clearing the collections:
		uniform.clear();
		fao.clear();
		outcrosser2deme.clear();
		outcrosser2outcrossingTendency.clear();
		demesEffectOnOutcrossingScoreNominator.clear();
		demesEffectOnOutcrossingScore.clear();

	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private double[] getWithDemeEffectRouletteFemalesAndMales(
			List<Outcrosser> listOfOutcrossers, boolean fitRoulette) {
		double[] roulette = null;
		if (roulette == null) { // lazy init
			List<Outcrosser> outcrossers = listOfOutcrossers;
			roulette = new double[outcrossers.size()];
			double sum = 0;
			for (int i = 0; i < roulette.length; i++) {
				Outcrosser o = outcrossers.get(i);
				if (!fitRoulette) {
					sum += (1 - o.getOutcrossingTendency()
							* demesEffectOnOutcrossingScore.get(outcrosser2deme
									.get(o)));
				} else {
					sum += o.getOutcrossingTendency()
							* demesEffectOnOutcrossingScore.get(outcrosser2deme
									.get(o));
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
}