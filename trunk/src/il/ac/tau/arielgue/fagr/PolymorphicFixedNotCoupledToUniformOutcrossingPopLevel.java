package il.ac.tau.arielgue.fagr;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import cern.jet.random.Binomial;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * the outcrossing determination and coupling here are at total population
 * level; thus when set by fitness - are more governed by (non-relative)fitness
 * (when pop is large)
 */
public class PolymorphicFixedNotCoupledToUniformOutcrossingPopLevel implements
		OutcrossingStrategy {

	private static final long serialVersionUID = -778251181344780070L;

	protected static final Logger logger = Logger
			.getLogger(PolymorphicFixedNotCoupledToUniformOutcrossingPopLevel.class);

	protected double outcrossingCost = Double.NaN;
	protected double stepValue = Double.NaN;

	protected LinkedList<Outcrosser> uniform;
	protected LinkedList<Outcrosser> fao;
	protected LinkedList<Outcrosser> fao2;
	protected HashMap<Outcrosser, Population> outcrosser2deme;
	protected double fractionOfRegulatedFao = Double.NaN;
	protected double fractionOfRegulatedFao2 = Double.NaN;
	protected int effectiveOutcrossings = Integer.MIN_VALUE;

	public PolymorphicFixedNotCoupledToUniformOutcrossingPopLevel() {
		uniform = Lists.newLinkedList();
		fao = Lists.newLinkedList();
		fao2 = Lists.newLinkedList();
		outcrosser2deme = Maps.newHashMap();
	}

	@Override
	public void outcross(List<Population> demes, Population pool) {
		double uniformOutcrossingTendency = Double.NaN;
		for (Population deme : demes) {
			for (Outcrosser o : deme) {
				outcrosser2deme.put(o, deme);
				if (o.isFao()) {
					if (o.isFao2()) {
						fao2.add(o);
					} else
						fao.add(o);
				} else {
					uniform.add(o);
				}
			}
		}
		uniformOutcrossingTendency = uniform.size() > 0 ? uniform.get(0)
				.getUniformOutcrossingProbability() : 0;
		double faoOutcrossingTendency = fao.size() > 0 ? fao.get(0)
				.getFaoOutcrossingProbability()[0] : 0;
		double polyFaoOutcrossingTendency = fao2.size() > 0 ? fao2.get(0)
				.getFaoOutcrossingProbability()[1] : 0;

		Collections.shuffle(uniform);
		// Remember to shuffle fao and polyFao later if randomness is needed
		Collections.sort(fao);
		Collections.sort(fao2);

		// feedback doesn't make much sense in coupling and outcrossing
		// on
		// population level. and there is no need to apply density
		// dependence

		// tendency of an arbitrary Outcrosser, all have the field
		// UniformOutcrossingProbability:

		int uniformOutcrossings = (uniform.size() > 0 && uniformOutcrossingTendency > 0) ? Binomial
				.staticNextInt(uniform.size(), uniformOutcrossingTendency) : 0;
		// logger.debug("Number of uniform outcrossings: " +
		// uniformOutcrossings);

		// coupling to the proportion as a default. coupling to absolute number
		// (i.e. to
		// filteredOutcrossing, or in the Binomial have filteredList.size()
		// as parameter instead of nonFilteredList.size()) may create an
		// artifact of increase or decrease in total outcrossing rate:
		int faoOutcrossings = (fao.size() > 0 && faoOutcrossingTendency > 0) ? Binomial
				.staticNextInt(fao.size(), faoOutcrossingTendency) : 0;
		int polyFaoOutcrossings = (fao2.size() > 0 && polyFaoOutcrossingTendency > 0) ? Binomial
				.staticNextInt(fao2.size(), polyFaoOutcrossingTendency) : 0;

		// /////////////////////////////////////////////////////////////// HERE
		// CONTINUE

		int regulatedFaoOutcrossings = (int) Math.round(faoOutcrossings
				* fractionOfRegulatedFao);
		int remainingFaoOutcrossers = faoOutcrossings
				- regulatedFaoOutcrossings;
		int regulatedPolyFaoOutcrossings = (int) Math.round(polyFaoOutcrossings
				* fractionOfRegulatedFao);
		int remainingPolyFaoOutcrossers = polyFaoOutcrossings
				- regulatedPolyFaoOutcrossings;

		/*
		 * logger.debug("Number of FAO outcrossings: " + faoOutcrossings);
		 * double debugRatio2 = (double) faoOutcrossings / (double)
		 * uniformOutcrossings
		 * ;System.out.println(faoOutcrossings+" "+regulatedFaoOutcrossings
		 * +" "+uniformOutcrossings);
		 */

		/*
		 * TODO :change accessing the model, give it as a parameter instead
		 * through the.xml file
		 */

		// ** outcrossing for uniform
		effectiveOutcrossings = 0;
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

		for (int i = 0; i < regulatedFaoOutcrossings && fao.size() > 0; i++) {

			// 2 options to choose the fao's: by a roulette and this may add
			// stochasticity, or by the sorted list (faoComparator: ascending
			// order according to fitness):

			// by sorted list:
			Outcrosser o = fao.pop();
			outcrosser2deme.get(o).remove(o);
			pool.add(o);
			o.addFitnessCost(getOutcrossingCost());
			effectiveOutcrossings++;
			logger.debug("fitness of SORTED outcrosser=" + o.getFitness());
		}

		// now outcrossing the rest of the FAO which are not regulated:
		Collections.shuffle(fao); // needed because we previously sorted the
									// list for the ordered outcrossing
		for (int i = 0; i < remainingFaoOutcrossers && fao.size() > 0; i++) {
			Outcrosser o = fao.pop(); // fao shuffled
			outcrosser2deme.get(o).remove(o);
			pool.add(o);
			o.addFitnessCost(getOutcrossingCost());
			effectiveOutcrossings++;
		}

		// dispersal/outcrossing of polyFao: similar to Fao:
		for (int i = 0; i < regulatedPolyFaoOutcrossings && fao2.size() > 0; i++) {

			// 2 options to choose the fao's: by a roulette and this may add
			// stochasticity, or by the sorted list (faoComparator: ascending
			// order according to fitness):

			// by sorted list:
			Outcrosser o = fao2.pop();
			outcrosser2deme.get(o).remove(o);
			pool.add(o);
			o.addFitnessCost(getOutcrossingCost());
			effectiveOutcrossings++;
			logger.debug("fitness of Poly outcrosser=" + o.getFitness());
		}

		// now outcrossing the rest of the FAO which are not regulated:
		Collections.shuffle(fao2); // needed because we previously sorted the
										// list for the ordered outcrossing
		for (int i = 0; i < remainingPolyFaoOutcrossers && fao2.size() > 0; i++) {
			Outcrosser o = fao2.pop(); // polyFao shuffled
			outcrosser2deme.get(o).remove(o);
			pool.add(o);
			o.addFitnessCost(getOutcrossingCost());
			effectiveOutcrossings++;
		}

		uniform.clear();
		fao.clear();
		fao2.clear();
		outcrosser2deme.clear();
		effectiveOutcrossings=0;
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
	public void outcross(Population deme, Population pool) {
		throw new NotImplementedException();
	}

	public double getFractionOfRegulatedFao() {
		return fractionOfRegulatedFao;
	}

	public void setFractionOfRegulatedFao(double fractionOfRegulatedFao) {
		this.fractionOfRegulatedFao = fractionOfRegulatedFao;
	}

	public double getFractionOfRegulatedFao2() {
		return fractionOfRegulatedFao2;
	}

	public void setFractionOfRegulatedFao2(double fractionOfRegulatedFao2) {
		this.fractionOfRegulatedFao2 = fractionOfRegulatedFao2;
	}

	public int getEffectiveOutcrossings() {
		return effectiveOutcrossings;
	}
}
