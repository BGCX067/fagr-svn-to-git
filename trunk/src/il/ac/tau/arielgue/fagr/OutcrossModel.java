package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.SerializableModel;
import il.ac.tau.yoavram.pes.Simulation;
import il.ac.tau.yoavram.pes.statistics.DataGatherer;
import il.ac.tau.yoavram.pes.utils.RandomUtils;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

public class OutcrossModel extends SerializableModel<Outcrosser> {

	// private static final long serialVersionUID = -1930941483172617031L; //
	// version where demes could be with only 1 disperser that therefor won't
	// reproduce

	/**
	 * 
	 */
	private static final long serialVersionUID = -1930941483172617031L;

	private static final Logger logger = Logger.getLogger(OutcrossModel.class);

	/*
	 * females of deme number n - index 2n males of deme number n - index 2n+1
	 */
	protected List<Population> populations = null;
	protected Population pool = null;

	protected AbstractOutcrosserInvasion invasion = null;

	protected double migrationProbability = Double.NaN;
	protected int demeCapacity = 0;
	protected Outcrosser ancestor = null;
	protected int numberOfDemes = 0;
	protected double migrationCost = Double.NaN;
	/*
	 * private double faoHighOutcrossingProbability; private double
	 * faoLowOutcrossingProbability;
	 */
	protected double meanPredation = Double.NaN;
	protected double weatherCycle = Double.NaN;
	protected OutcrossingStrategy outcrossingStrategy;
	protected boolean directDispersal = false;
	protected double densityDependenceWeight;
	protected double feedBackWeight;

	protected transient DataGatherer<Outcrosser> intermediateDataGatherer1;
	protected transient DataGatherer<Outcrosser> intermediateDataGatherer2;
	protected transient DataGatherer<Outcrosser> intermediateDataGatherer3;
	protected transient DataGatherer<Outcrosser> intermediateDataGatherer4;
	protected transient DataGatherer<Outcrosser> intermediateDataGatherer5;
	protected transient int maxNumOfOutcrossers = Integer.MIN_VALUE;
	protected transient int numOfMigrators = Integer.MIN_VALUE;

	@Override
	public void init() {

		if (ancestor != null && (populations == null || populations.isEmpty())) {
			logger.info("Creating populations using an ancestor");
			populations = Lists.newArrayList();
			pool = new Population();
			while (populations.size() < getNumberOfDemes()) {
				Population deme = new Population();
				while (deme.size() < getDemeCapacity()) {
					deme.add(ancestor.reproduce(ancestor));
				}
				populations.add(deme);
			}
		}
		if (invasion != null) {
			logger.info("Invading the populations with "
					+ invasion.getInvasionRate() * 100 + "% "
					+ invasion.getInvaderName());
			populations = invasion.invadePopulations(populations);
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		// if this fails for some reason, close in Simulation with other data
		// gatherers by giving it to simulation and setting interval to maximum
		// integer.
		try {
			getIntermediateDataGatherer1().close();
			getIntermediateDataGatherer2().close();
			getIntermediateDataGatherer3().close();
			getIntermediateDataGatherer4().close();
			getIntermediateDataGatherer5().close();
		} catch (IOException e) {
			logger.warn("IntermediateDataGatherer failed to close: " + e);
		}
	}

	/*
	 * the order of phases in each time step: weather&predation affect carrying
	 * capacity-> outcrossing/gregarization and to the pool->random migration
	 * between demes->reproduction under selection and drift (of the parents)in
	 * each deme (from parents from the pool or the deme)
	 */
	@Override
	public void step() {
		// outcrossing (really migration)
		getOutcrossingStrategy().outcross(populations, pool);

		// now make sure no one is left alone in the deme:
		int effectiveOutcrossingUpdated = getOutcrossingStrategy()
				.getEffectiveOutcrossings();
		// System.out.println("before: " + effectiveOutcrossingUpdated);
		for (Population p : populations) {
			if (p.size() == 1) {
				Outcrosser o = p.get(0);
				p.remove(o);
				getPool().add(o);
				o.addFitnessCost(getOutcrossingStrategy().getOutcrossingCost());
				effectiveOutcrossingUpdated += 1;
			}
		}
		// System.out.println("now " + effectiveOutcrossingUpdated);

		// populationInDemeSizeDataGatherer
		// getIntermediateDataGatherer3().gather();

		// clear pool
		// first, if number is odd, make it even:
		if (getPool().size() % 2 != 0) {
			int demeSize = 0;
			Outcrosser o = getPool().get(0);
			while (demeSize <= 1) {
				int demeIndex = RandomUtils.nextInt(0, populations.size() - 1);
				demeSize = populations.get(demeIndex).size();
				if (demeSize >= 1) {
					populations.get(demeIndex).add(o);
					getPool().remove(o);
				}
			}
			demeSize = Integer.MIN_VALUE;
		}
		// now, disperse in couples so not to have a deme with 1 disperser that
		// can't reproduce without mate (and no self fertilization)
		int numOfDispersers = getPool().size();
		for (int i = 0; i < numOfDispersers;) {
			Outcrosser o1 = getPool().get(i);
			Outcrosser o2 = getPool().get(i + 1);
			int demeIndex = RandomUtils.nextInt(0, populations.size() - 1);
			populations.get(demeIndex).add(o1);
			populations.get(demeIndex).add(o2);
			i += 2;
		}
		// System.out.println("pool size: " + getPool().size());
		pool.clear();

		// /**
		// * DEBUG
		// */
		// int debugCount = 0;
		// for (Population p : populations) {
		// if (p.size() == 1) {
		// System.out.println(1 + " F " + p.get(0).isFao() + " ; het: "
		// + p.get(0).isHeterozygote() + "; fitness: "
		// + p.get(0).getFitness() + "; debug count= "
		// + debugCount);
		// debugCount++;
		// }
		// }

		// gathering data
		if (Simulation.getInstance().getTick() == 1
				|| Simulation.getInstance().getTick()
						% getIntermediateDataGatherer1().getInterval() == 0) {
			getIntermediateDataGatherer1().gather();
			getIntermediateDataGatherer2().gather();
			getIntermediateDataGatherer3().gather();
			getIntermediateDataGatherer4().gather();
			getIntermediateDataGatherer5().gather();
		}
		// reproduction
		// create demes roulette
		double[] roulette = new double[populations.size()];
		double sum = 0;
		for (int i = 0; i < roulette.length; i++) {
			double size = populations.get(i).size();
			if (size == 1) {
				/*
				 * SELFING NOT PERMITTED so demes with a single outcrosser are
				 * considered empty
				 */
				size = 0;
			}
			sum += size;
			roulette[i] = sum;
		}
		for (int i = 0; i < roulette.length; i++) {
			roulette[i] /= sum;
		}
		// create the new populations
		int totalPopulationSize = 0;
		int populationCapacity = getDemeCapacity() * getNumberOfDemes();
		List<Population> newPopulations = Lists
				.newArrayListWithExpectedSize(populations.size());
		for (int i = 0; i < populations.size(); i++) {
			newPopulations.add(new Population());
		}
		// create the new outcrossers until populations are full
		while (totalPopulationSize < populationCapacity) {
			// choose random deme
			double demeRandom = RandomUtils.nextDouble();
			int demeInd = 0;
			while (roulette[demeInd] < demeRandom) {
				demeInd++;
			}
			Population deme = populations.get(demeInd);
			// choose random parents
			Outcrosser o1 = deme.getRandom(); // all are HERMAPHRODITES
			Outcrosser o2 = deme.getRandom();
			while (o2 == o1) {
				// deme size > 1 - see roulette creation - so no infinite loop
				// here
				o2 = deme.getRandom();

			}
			// if the mating is possible, then create a child
			if (RandomUtils.nextDouble() < (1 - o1.getFitnessCost())
					* (1 - o2.getFitnessCost())) {
				Outcrosser child = o1.reproduce(o2);
				// selection
				if (RandomUtils.nextDouble() < child.getFitness()) {
					newPopulations.get(demeInd).add(child);
					totalPopulationSize++;
				}
			}
		}

		// finished with the old population
		populations.clear();

		// add new populations to the model
		for (Population deme : newPopulations) {
			if (deme.size() > 0) { // empty demes are cast away
				if (deme.size() >= 2 * getDemeCapacity()) {
					// demes that are too large are broken to 2 demes
					int targetSize = deme.size() / 2;
					Population deme2 = new Population();
					while (deme.size() > targetSize) {
						deme2.add(deme.removeRandom());
					}
					populations.add(deme2);
				}
				populations.add(deme);
			}
		}
	}

	public void setMeanPredation(double meanPredation) {
		this.meanPredation = meanPredation;
	}

	public void setWeatherCycle(double weatherCycle) {
		this.weatherCycle = weatherCycle;
	}

	public double getMeanPredation() {
		return meanPredation;
	}

	public double getWeatherCycle() {
		return weatherCycle;
	}

	public AbstractOutcrosserInvasion getInvasion() {
		return invasion;
	}

	public void setInvasion(AbstractOutcrosserInvasion invasion) {
		this.invasion = invasion;
	}

	/**
	 * copy the lists, but not the objects
	 */
	@Override
	public List<List<Outcrosser>> getPopulations() {
		List<List<Outcrosser>> list = Lists.newArrayList();
		for (Population p : populations) {
			list.add(Lists.newArrayList(p));
		}
		return list;
	}

	/**
	 * be aware that changing the returned collection will change the original
	 * collection
	 */
	public List<Outcrosser> getPool() {
		return pool;
	}

	/**
	 * copy the lists, but not the objects
	 */
	@Override
	public void setPopulations(List<List<Outcrosser>> populations) {
		this.populations = Lists.newArrayList();
		for (List<Outcrosser> pop : populations) {
			populations.add(new Population(pop));
		}

	}

	public void setMigrationProbability(double migrationProbability) {
		this.migrationProbability = migrationProbability;
	}

	public double getMigrationProbability() {
		return migrationProbability;
	}

	public void setDemeCapacity(int demeCapacity) {
		this.demeCapacity = demeCapacity;
	}

	public int getDemeCapacity() {
		return demeCapacity;
	}

	public void setAncestor(Outcrosser ancestor) {
		this.ancestor = ancestor;
	}

	public Outcrosser getAncestor() {
		return ancestor;
	}

	public void setNumberOfDemes(int numberOfDemes) {
		this.numberOfDemes = numberOfDemes;
	}

	public int getNumberOfDemes() {
		return numberOfDemes;
	}

	public void setMigrationCost(double migrationCost) {
		this.migrationCost = migrationCost;
	}

	public double getMigrationCost() {
		return migrationCost;
	}

	public void setOutcrossingStrategy(OutcrossingStrategy outcrossingStrategy) {
		this.outcrossingStrategy = outcrossingStrategy;
	}

	public OutcrossingStrategy getOutcrossingStrategy() {
		return outcrossingStrategy;
	}

	public boolean isDirectDispersal() {
		return directDispersal;
	}

	public void setDirectDispersal(boolean directDispersal) {
		this.directDispersal = directDispersal;
	}

	public void setFeedBackWeight(double feedBackWeight) {
		this.feedBackWeight = feedBackWeight;
	}

	public double getFeedBackWeight() {
		return feedBackWeight;
	}

	public void setDensityDependenceWeight(double densityDependenceWeight) {
		this.densityDependenceWeight = Math.min(1,
				Math.abs(densityDependenceWeight));
	}

	public double getDensityDependenceWeight() {
		return densityDependenceWeight;
	}

	public DataGatherer<Outcrosser> getIntermediateDataGatherer1() {
		return intermediateDataGatherer1;
	}

	public void setIntermediateDataGatherer1(
			DataGatherer<Outcrosser> intermediateDataGatherer_a) {
		this.intermediateDataGatherer1 = intermediateDataGatherer_a;
	}

	public void setIntermediateDataGatherer2(
			DataGatherer<Outcrosser> intermediateDataGatherer_b) {
		this.intermediateDataGatherer2 = intermediateDataGatherer_b;
	}

	public DataGatherer<Outcrosser> getIntermediateDataGatherer2() {
		return intermediateDataGatherer2;
	}

	public void setIntermediateDataGatherer3(
			DataGatherer<Outcrosser> intermediateDataGatherer_c) {
		this.intermediateDataGatherer3 = intermediateDataGatherer_c;
	}

	public DataGatherer<Outcrosser> getIntermediateDataGatherer3() {
		return intermediateDataGatherer3;
	}

	public void setIntermediateDataGatherer4(
			DataGatherer<Outcrosser> intermediateDataGatherer_d) {
		this.intermediateDataGatherer4 = intermediateDataGatherer_d;
	}

	public DataGatherer<Outcrosser> getIntermediateDataGatherer4() {
		return intermediateDataGatherer4;
	}

	public void setIntermediateDataGatherer5(
			DataGatherer<Outcrosser> intermediateDataGatherer_e) {
		this.intermediateDataGatherer5 = intermediateDataGatherer_e;
	}

	public DataGatherer<Outcrosser> getIntermediateDataGatherer5() {
		return intermediateDataGatherer5;
	}
}
/*
 * double density = (double) getPopulations().get(deme).size() / (double)
 * getDemeCapacity(deme); double meanTendency = 0; int count = 0; for
 * (Outcrosser o : getPopulations().get(deme)) { meanTendency = ((count
 * meanTendency) + o .getInitialOutcrossingTendency(density)) / ++count; } for
 * (Outcrosser o : getPopulations().get(deme)) { if (RandomUtils.nextDouble() <
 * o.getFinalOutcrossingTendency( density, meanTendency)) {
 * getPopulations().get(deme).remove(o); getPopulations().get(0).add(o);
 * logger.debug(String.format( "Outcrosser %d outcrossed to pool", o.getID()
 * .toString())); } }
 */

/*
 * private double getUniformOutcrossingProbability(double density) { adjust to 2
 * ways of outcrossing and check that this is ok with the noise return
 * getTopOfPopulationFraction() densityDependentOutcrossingProbability(
 * getFaoLowOutcrossingProbability(), density) + (1 -
 * getTopOfPopulationFraction()) densityDependentOutcrossingProbability(
 * getFaoHighOutcrossingProbability(), density); return Double.NaN; }
 */

