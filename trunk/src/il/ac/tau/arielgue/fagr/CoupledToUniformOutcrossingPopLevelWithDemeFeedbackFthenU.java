package il.ac.tau.arielgue.fagr;

import java.util.List;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import il.ac.tau.yoavram.pes.utils.RandomUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.management.RuntimeErrorException;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.springframework.util.MultiValueMap;
import cern.jet.random.Binomial;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class CoupledToUniformOutcrossingPopLevelWithDemeFeedbackFthenU
		implements OutcrossingStrategy {
	protected static final Logger logger = Logger
			.getLogger(CoupledToUniformOutcrossingPopLevel.class);
	protected transient LinkedList<Outcrosser> uniform;
	protected transient LinkedList<Outcrosser> fao;
	protected transient HashMap<Outcrosser, Population> outcrosser2deme;
	protected double outcrossingCost = Double.NaN;
	protected double stepValue = Double.NaN;
	protected double fractionOfRegulatedFAOutcrossing = Double.NaN;
	/**
	 * TODO: insert this "alpha and beta" (from the deterministic model)
	 */
	private transient HashMap<Population, Double> demesEffectOnOutcrossingScore;
	private transient HashMap<Population, Double> demesFeedbackValues;
	private transient HashMap<Population, Integer> demesEffectOnOutcrossingScoreNominator;
	// private transient Integer outcrossersNominator;
	private transient HashMap<Outcrosser, Double> outcrosser2outcrossingTendency;
	private static final long serialVersionUID = -6946548167556845880L;

	public CoupledToUniformOutcrossingPopLevelWithDemeFeedbackFthenU() {
		uniform = Lists.newLinkedList();
		fao = Lists.newLinkedList();
		outcrosser2deme = Maps.newHashMap();
		demesEffectOnOutcrossingScore = Maps.newHashMap();
		// outcrossersNominator = Integer.MIN_VALUE;
		outcrosser2outcrossingTendency = Maps.newHashMap();
		demesEffectOnOutcrossingScoreNominator = Maps.newHashMap();
	}

	@Override
	public void outcross(List<Population> demes, Population pool) {
		double uniformOutcrossingTendency = Double.NaN;
		/*
		 * starting with dividing the population to 2 lists: FAO and UNI; at the
		 * same time calculate a density effect per deme:
		 */
		for (Population deme : demes) {
			// starting with a positive effect of density above the added before
			// the density:
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
		int beforeOutcrossingUniformSize = uniform.size();
		int beforeOutcrossingFaoSize = fao.size();
		uniformOutcrossingTendency = fao.size() == 0 ? uniform.get(0)
				.getUniformOutcrossingProbability() : fao.get(0)
				.getUniformOutcrossingProbability();
		Collections.shuffle(uniform);
		Collections.shuffle(fao);
		int originalFaoSize = fao.size();
		// int originalUniSize = uniform.size();
		int uniformOutcrossings = 0;
		int faoOutcrossings = 0;

		// -> Coupling to the defacto rate. coupling to absolute number (i.e. to
		// filteredOutcrossing, or in the Binomial have filteredList.size()
		// as parameter instead of nonFilteredList.size()) may create an
		// artifact of increase or decrease in total outcrossing rate:
		// logger.debug("Number of uniform outcrossings: " +
		// uniformOutcrossings);
		// logger.debug("Number of FAO outcrossings: " + faoOutcrossings);

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

		for (int i = 0; i < faoOutcrossings && fao.size() > 0; i++) {
			double[] roulette = getWithDemeEffectRouletteFemalesAndMales(fao,
					false);
			int indexInRoulette = 0;
			if (roulette.length > 0) {
				double rand = RandomUtils.nextDouble();
				while (roulette[indexInRoulette] <= rand)
					indexInRoulette++;
				Outcrosser o = fao.get(indexInRoulette);
				fao.remove(indexInRoulette);
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

		uniform.clear();
		fao.clear();
		// //////////////////////////////////////////////////////////////////////////////////////////////////////////

		// now calculate feedback score for each deme, according to the total
		// tentative outcrossing. so now we need to go again through the demes,
		// outcross the sure==1 outcrossers and calculate the feedback:

		double totalFAOFeedbackValue = 0;
		for (Population deme : demes) {

			// starting with a positive effect of effective proportion above
			// the uniform outcrossing rate; changing the values in
			// demesFeedbackScore to new ones:
			/**
			 * TODO: consider a Hill Function instead *
			 */
			int numberOfTentativeOutcrossersInDeme = (demesEffectOnOutcrossingScoreNominator
					.get(deme) == null) ? 0
					: demesEffectOnOutcrossingScoreNominator.get(deme);
			// only positive demesEffectOnOutcrossingScore values:
			/**
			 * TODO: if sticking to ratioThreshold parameter, then make it a
			 * changeable parameter
			 */
			double ratioThreshold = 1.05; // i.e. in a deme of 20, at least 2
			// more than expected should
			// outcross to have a positive
			// feedback
			double actualRatio = deme.size() > 0 ? (((double) numberOfTentativeOutcrossersInDeme / deme
					.size())) / uniformOutcrossingTendency
					: 0;
			double effectValue = (actualRatio > ratioThreshold && actualRatio > 0) ? actualRatio
					: (actualRatio > 0) ? 1 : 0;
			demesEffectOnOutcrossingScore.put(deme, effectValue);

			// a mixed positive-negative demesEffectOnOutcrossingScore version:
			// demesEffectOnOutcrossingScore
			// .put(deme,
			// ((1 - uniformOutcrossingTendency) + ((double)
			// demeFeedbackScoreValue)
			// / deme.size()));

			// weight the effectValue by the potential
			// extra-outcrossing-by-feedback remaining in that deme:
			double currVal = effectValue
					* (double) (deme.size() - numberOfTentativeOutcrossersInDeme)
					/ (beforeOutcrossingFaoSize - faoOutcrossings
							+ beforeOutcrossingUniformSize - uniformOutcrossings);
			if (currVal < 0 || effectValue < 0)
				throw new RuntimeException(
						"negative feedback error: effectValue=" + effectValue
								+ "; currVal=" + currVal);

			totalFAOFeedbackValue += currVal;

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
					// building the lists of those who remained and are
					// potential for outcrossing-by-feedback:
					if (o.isFao()) {
						fao.add(o);
					} else {
						uniform.add(o);
					}
				}
			}
		}

		// last - effect the feedback scores: first, on FAO total population,
		// using totalFAOFeedbackValue; then couple the UNI to it. a good way is
		// to actually create a roulette on demes according to the feedback
		// score, giving priority to higher score demes as should be:

		// Fao's by feedback:

		double faoOutcrossingRateByFeedback = Math.max(
				uniformOutcrossingTendency * (totalFAOFeedbackValue - 1), 0.0);
		int faoOutcrossingByFeedback = fao.size() > 0 ? Binomial.staticNextInt(
				fao.size(), faoOutcrossingRateByFeedback) : 0;
		int uniOutcrossingByFeedback = (uniform.size() > 0 && faoOutcrossingByFeedback > 0) ? Math
				.round(uniform.size() * faoOutcrossingByFeedback / fao.size())
				: (uniform.size() > 0 && faoOutcrossingRateByFeedback > 0) ? Binomial
						.staticNextInt(uniform.size(),
								faoOutcrossingRateByFeedback) : 0;
		for (int i = 0; i < faoOutcrossingByFeedback && fao.size() > 0; i++) {
			double[] roulette = getWithDemeEffectRouletteFemalesAndMales(fao,
					false);
			int indexInRoulette = 0;
			if (roulette.length > 0) {
				double rand = RandomUtils.nextDouble();
				while (roulette[indexInRoulette] <= rand)
					indexInRoulette++;
				Outcrosser o = fao.get(indexInRoulette);
				Population deme = outcrosser2deme.get(o);
				fao.remove(indexInRoulette);
				deme.remove(o);
				pool.add(o);
				o.addFitnessCost(getOutcrossingCost());
				outcrosser2deme.remove(o);
				effectiveOutcrossings++;
				// sanity output
				logger.debug("fitness of FAO outcrosser=" + o.getFitness());
			}
		}

		// UNI's by feedback, and according to the demes roulette:
		double[] roulette = getDemesRoulette(demes);
		for (int i = 0; i < uniOutcrossingByFeedback && uniform.size() > 0; i++) {
			int indexInRoulette = 0;
			if (roulette.length > 0) {
				double rand = RandomUtils.nextDouble();
				while (roulette[indexInRoulette] <= rand)
					indexInRoulette++;
				Population deme = demes.get(indexInRoulette);
				Outcrosser o = null;
				for (int j = 0; j < deme.size(); j++) {
					o = deme.get(j);
					if (!o.isFao())
						break;
				}
				/**
				 * TODO: check it is correct and works also when the 1st pick
				 * doesn't work:
				 */
				if (o == null) {
					i--;
					continue;
				}
				uniform.remove(indexInRoulette);
				deme.remove(o);
				if (deme.size() == 0) {
					demesEffectOnOutcrossingScore.put(deme, 0.0);
					roulette = getDemesRoulette(demes);
				}
				pool.add(o);
				o.addFitnessCost(getOutcrossingCost());
				outcrosser2deme.remove(o);
				effectiveOutcrossings++;
				// sanity output
				logger.debug("fitness of UNI outcrosser=" + o.getFitness());
			}
		}

		// after finishing the outcrossing step, clearing the collections:
		uniform.clear();
		fao.clear();
		outcrosser2deme.clear();
		outcrosser2outcrossingTendency.clear();
		demesEffectOnOutcrossingScoreNominator.clear();
		demesEffectOnOutcrossingScore.clear();
	}// end of: public void outcross(List<Population> demes, Population pool)

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////
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

	private double[] getDemesRoulette(List<Population> listOfDemes) {
		double[] roulette = null;
		if (roulette == null) { // lazy init
			List<Population> demesList = listOfDemes;
			roulette = new double[demesList.size()];
			double sum = 0;
			for (int i = 0; i < roulette.length; i++) {
				Population p = demesList.get(i);
				sum += demesEffectOnOutcrossingScore.get(p);
				roulette[i] = sum;
			}
			// to avoid a NaN result:
			if (sum == 0)
				sum = 1;
			for (int i = 0; i < roulette.length; i++) {
				roulette[i] = roulette[i] / sum;
			}
			// sanity check
			if (roulette.length > 0 && roulette[0] <= 0) {
				throw new RuntimeException(
						"First item in roulette must be positive, but it is "
								+ roulette[0]);
			}
			if (roulette.length > 0 && roulette[roulette.length - 1] != 1) {
				throw new RuntimeException(
						"Last item in roulette must be 1, but it is "
								+ roulette[roulette.length - 1]);
			}// end sanity check
		}
		return roulette;
	}

	@Override
	public double getOutcrossingCost() {
		return outcrossingCost;
	}

	public void setOutcrossingCost(double outcrossingCost) {
		this.outcrossingCost = outcrossingCost;
	}

	@Override
	public double getStepValue() {
		throw new NotImplementedException();
	}

	public void setStepValue(double stepValue) {
		this.stepValue = stepValue;
	}

	@Override
	public void outcross(Population deme, Population pool) {
		throw new NotImplementedException();
	}

	public double getFractionOfRegulatedFAOutcrossing() {
		return fractionOfRegulatedFAOutcrossing;
	}

	public void setFractionOfRegulatedFAOutcrossing(
			double fractionOfRegulatedFAOutcrossing) {
		this.fractionOfRegulatedFAOutcrossing = fractionOfRegulatedFAOutcrossing;
	}

	@Override
	public int getEffectiveOutcrossings() {
		// TODO Auto-generated method stub
		return Integer.MIN_VALUE;
	}
}