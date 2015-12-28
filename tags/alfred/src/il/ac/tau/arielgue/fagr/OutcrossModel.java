package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.SerializableModel;
import il.ac.tau.yoavram.pes.Simulation;
import il.ac.tau.yoavram.pes.utils.RandomUtils;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class OutcrossModel extends SerializableModel<Outcrosser> {
	private static final long serialVersionUID = -8235545584313588405L;
	private static final Logger logger = Logger.getLogger(OutcrossModel.class);

	/*
	 * females of deme number n - index 2n males of deme number n - index 2n+1
	 * pool is deme number 0
	 */
	private List<Population> populations = null;
	private Population pool = null;

	private AbstractOutcrosserInvasion invasion = null;

	private double migrationProbability = Double.NaN;
	private int demeCapacity = 0;
	private Outcrosser ancestor = null;
	private int numberOfDemes = 0;
	private double migrationCost = Double.NaN;
	/*
	 * private double faoHighOutcrossingProbability; private double
	 * faoLowOutcrossingProbability;
	 */
	private double meanPredation = Double.NaN;
	private double weatherCycle = Double.NaN;
	private OutcrossingStrategy outcrossingStrategy;

	@Override
	public void init() {
		if (ancestor != null && (populations == null || populations.isEmpty())) {
			logger.info("Creating populations using an ancestor");
			populations = Lists.newArrayList();
			pool = new Population();
			while (populations.size() < getNumberOfDemes()) {
				Population p = new Population();
				while (p.size() < getDemeCapacity()) {
					p.add(ancestor.reproduce(ancestor));
				}
				p.setCapacity(getDemeCapacity());
				populations.add(p);
			}
		}
		if (invasion != null) {
			logger.info("Invading the populations with "
					+ invasion.getInvasionRate() * 100 + "% "
					+ invasion.getInvaderName());
			populations = invasion.invadePopulations(populations);
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
		pool.clear();

		// DEBUG PRINT
		// System.out.println(Simulation.getInstance().getTick());
		// double totalSize=pool.size();
		// for (Population pop:populations){
		// totalSize+=pop.size();
		// };
		// System.out.println(totalSize);

		/*
		 * CHANGING THE CAPACITY OF A DEME THROUGH CYCLIC GENERAL EFFECT
		 * (WEATHER) AND PARTIALLY-RANDOM DEME-SPECIFIC EFFECT (PREDATION)
		 */
		// weather
		int capacity = getDemeCapacity();
		if (getWeatherCycle() > 0) {
			double tick = Simulation.getInstance().getTick();
			double mod = 3.0 / 2.0 - Math.cos(tick * Math.PI
					/ getWeatherCycle()) / 2;
			capacity = (int) Math.round((double) getDemeCapacity() * mod);
			logger.debug(String.format("Weather modifier is %f", mod));
		}

		// predation - hold? change algorithm to relate to previous generation
		// size or density?
		if (getMeanPredation() > 0) {
			for (Population deme : populations) {
				int predations = RandomUtils.nextPoisson(getMeanPredation());
				logger.debug(String.format("%d predations", predations));
				deme.setCapacity(capacity - predations);
				logger.debug(String.format("Capacity is %d", capacity
						- predations));
			}
		} else {
			for (Population deme : populations) {
				deme.setCapacity(capacity);
				logger.debug(String.format("Capacity is %d", capacity));
			}
		}

		// outcrossing - 2 ways: "realistic"(this requires when analyzing
		// data/simulation results to compare to other simulations with above
		// and below rates of uniform to see that the differences in rate are
		// not important) and "equal". also - feedback can be either sigmoid or
		// exponential
		for (Population deme : populations) {
			getOutcrossingStrategy().outcross(deme, pool);
		}

		// migration
		Map<Outcrosser, Integer> migrators = Maps.newHashMap();
		for (int deme = 0; deme < populations.size(); deme++) {
			Population pop = populations.get(deme);

			// safe iteration, avoids concurrent modifications exception:
			for (int i = 0; i < pop.size(); i++) {
				Outcrosser o = pop.get(i);
				if (RandomUtils.nextDouble() < getMigrationProbability()) {
					pop.remove(i);
					i--;
					int dstDeme = deme;
					while (dstDeme == deme) {
						dstDeme = RandomUtils.nextInt(0, getPopulations()
								.size() - 1);
					}
					o.addFitnessCost(getMigrationCost());
					migrators.put(o, dstDeme);
					logger.debug(String.format(
							"Outcrosser %s  migrates from %d to %d", o.getID()
									.toString(), deme, dstDeme));
				}
			}
		}
		for (Outcrosser o : migrators.keySet()) {
			int dstDeme = migrators.get(o);
			populations.get(dstDeme).add(o);
		}

		// create next generation per deme - includes selection, reproduction
		// and random drift (drift satisfied by the roulette process)
		// NOTE: maybe no males in deme can shape results
		// offspring are generated by choosing parent-pairs with selection and
		// with replacement
		// until Capacity is reached. this way is better computationally, and
		// expectation of number of offspring per parent is similar to if we
		// produced offspring and then do selection on them (variance may be
		// different). we also use as default a roulette in the
		// getFitRandom(boolean b): this is useful when
		// fitness is low, and a threshold function might cause many iterations
		// to populate the deme

		boolean bothSexesInPool = (pool.getFemales().size() > 0 && pool
				.getMales().size() > 0);
		double numberOfFemalesInPoolByNumberOfDemes = (double) pool
				.getFemales().size() / (double) numberOfDemes;

		for (int demeInd = 0; demeInd < populations.size(); demeInd++) {
			Population currentGen = populations.get(demeInd);
			Population nextGen = new Population();

			double femalesInDeme = currentGen.getFemales().size();
			boolean bothSexesInDeme = (femalesInDeme > 0 && currentGen
					.getMales().size() > 0);
			double probabilityParentsFromDeme = femalesInDeme
					/ (numberOfFemalesInPoolByNumberOfDemes + femalesInDeme);

			int nextGenCapacity = currentGen.getCapacity();
			if (!bothSexesInPool && !bothSexesInDeme) {
				nextGenCapacity = 0;
			}

			while (nextGen.size() < nextGenCapacity) {
				Population source = null;

				if (bothSexesInPool && !bothSexesInDeme) {
					// all from pool
					source = pool;
				} else if (!bothSexesInPool && bothSexesInDeme) {
					// all from deme
					source = currentGen;
				} else if (bothSexesInPool && bothSexesInDeme) {
					/**
					 * NOTE - check when changing configuration from FAGR to
					 * FAO: from deme or pool: note: ratio of parents couples
					 * from pool to from deme depends on the ratio of FEMALES
					 * only, and not on the ratio of population. if moving to
					 * hermaphrodites it needs to be changed
					 */
					source = RandomUtils.nextDouble() < probabilityParentsFromDeme ? currentGen
							: pool;
				}
				// selection
				Outcrosser female = source.getFitRandom(true);
				Outcrosser male = source.getFitRandom(false);
				// reproduction
				Outcrosser child = female.reproduce(male);
				if (child.viable()) {
					logger.debug(String.format("%d born to %d and %d",
							child.getID(), female.getID(), male.getID()));
					// insert to next generation lists
					nextGen.add(child);
				}
			}
			populations.set(demeInd, nextGen);
		}
	}

	/*
	 * // Yoav check if needed - NOT NEEDED public double
	 * densityDependentOutcrossingProbability(double probability, double
	 * density) {
	 * 
	 * return Double.NaN; }
	 */

	public void setMeanPredation(double meanPredation) {
		this.meanPredation = meanPredation;
	}

	public void setWeatherCycle(double weatherCycle) {
		this.weatherCycle = weatherCycle;
	}

	private double getMeanPredation() {
		return meanPredation;
	}

	private double getWeatherCycle() {
		return weatherCycle;
	}

	/*
	 * private double getFaoHighOutcrossingProbability() { return
	 * faoHighOutcrossingProbability; }
	 * 
	 * private double getFaoLowOutcrossingProbability() { return
	 * faoLowOutcrossingProbability; }
	 */

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

}
/*
 * double density = (double) getPopulations().get(deme).size() / (double)
 * getDemeCapacity(deme); double meanTendency = 0; int count = 0; for
 * (Outcrosser o : getPopulations().get(deme)) { meanTendency = ((count *
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

