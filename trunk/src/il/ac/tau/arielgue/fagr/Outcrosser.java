package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.Simulation;
import il.ac.tau.yoavram.pes.utils.RandomUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;

import org.hamcrest.core.IsNull;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Outcrosser implements Serializable, Comparable<Outcrosser> {

	// private static final Logger logger = Logger.getLogger(Outcrosser.class);

	private static final long serialVersionUID = -8990323622458365604L;
	private static int NEXT_ID = 0;
	protected static final int DEFAULT_VALUE = -1;

	/**
	 * A=non deleterious allele AND Uniform outcrossing allele.
	 */
	public static final int HOMOZYGOT_AA = 0;
	public static final int HETEROZYGOT_BA = 1;
	public static final int HETEROZYGOT_AB = 2;
	/**
	 * B=deleterious allele AND FAO outcrossing allele.For NEUTRAL genes B is
	 * also non-deleterious
	 */
	public static final int HOMOZYGOT_BB = 3;
	public static final int HOMOZYGOT_CC = 4;

	private int id = NEXT_ID++;

	private int genomeSize = 10000;
	private int intervalOfNeutralGenes;

	private double selectionCoefficient = Double.NaN;
	private double heterozygoteCoefficient = Double.NaN;
	private double mutationRate = Double.NaN;
	private double recombinationRate = Double.NaN;
	private double minimumFitness = Double.NaN;
	private double uniformOutcrossingProbability = Double.NaN;

	// private double stepValue = 0;
	/**
	 * the default is a homozygote for Uniform outcrossing, and heterozygote not
	 * decided=-1;
	 */
	private int[] outcrossingModifier = new int[] { HOMOZYGOT_AA, HOMOZYGOT_AA };
	private Boolean fao = null;
	private Boolean fao2 = null;
	// the locations of deleterious mutations, maximum size=limitOfViability:
	private SortedMap<Integer, Integer> genome = null;
	private boolean female = true;// hermaphrodites
	private double fitness = -1;
	private double fitnessCost = 0;
	private boolean bornInPool = false;
	private double[] faoOutcrossingProbability = new double[] { Double.NaN,
			Double.NaN }; // 0 - B, 1 - C

	protected Outcrosser() {
	}

	public Outcrosser(Outcrosser other) {
		this();
		this.genomeSize = other.genomeSize;
		this.selectionCoefficient = other.selectionCoefficient;
		this.heterozygoteCoefficient = other.heterozygoteCoefficient;
		this.mutationRate = other.mutationRate;
		this.recombinationRate = other.recombinationRate;
		this.minimumFitness = other.minimumFitness;
		this.uniformOutcrossingProbability = other.uniformOutcrossingProbability;
		this.faoOutcrossingProbability = other.faoOutcrossingProbability;
		this.intervalOfNeutralGenes = other.intervalOfNeutralGenes;
	}

	protected Outcrosser spawn() {
		return new Outcrosser(this);
	}

	public static SortedMap<Integer, Integer> newGenome() {
		return newGenome(null);
	}

	public static SortedMap<Integer, Integer> newGenome(
			SortedMap<Integer, Integer> other) {
		if (other == null)
			return Maps.newTreeMap();
		else
			return Maps.newTreeMap(other);
	}

	/**
	 * this changes parents
	 * 
	 * @param mate
	 * @return
	 */
	public Outcrosser reproduce(Outcrosser mate) {

		// recombine, segregate and unite modifier gametic type - no reason to
		// assume proximity to
		// any specific locus therefore assume free recombination and
		// segregation. thus we can calculate only once with probability 0.5 -
		// for the modifier locus only

		// recombination
		SortedMap<Integer, Integer> thisGenome = this.recombinate();
		SortedMap<Integer, Integer> mateGenome = mate.recombinate();

		// Segregation
		SortedSet<Integer> thisGamete = segragate(thisGenome);
		SortedSet<Integer> mateGamete = segragate(mateGenome);

		// create the child
		Outcrosser child = spawn(); // includes coinToss for male/female - see
									// spawn() for details

		// the modifier has free recombination with the
		// genome, so one coin toss (in segregate in zygoteModifier) is enough
		// to include recombination and segregation. however, as Yoav suggested
		// for r107, why not add another one(here: side)?...
		boolean side = RandomUtils.coinToss();
		if (side) {
			child.setOutcrossingModifier(zygoteModifier(
					getOutcrossingModifier(), mate.getOutcrossingModifier()));
			child.setGenome(zygote(thisGamete, mateGamete));
		} else {
			child.setOutcrossingModifier(zygoteModifier(
					mate.getOutcrossingModifier(), getOutcrossingModifier()));
			child.setGenome(zygote(mateGamete, thisGamete));
		}
		/**
		 * TODO: check if the side's coinToss is really necessary
		 */

		// mutation
		int mutations = RandomUtils.nextPoisson(getMutationRate());
		for (int m = 0; m < mutations; m++) {
			child.mutate();
		}

		return child;
	}

	private int[] zygoteModifier(int[] myModifier, int[] mateModifier) {
		boolean side = RandomUtils.coinToss();
		int modifierRight = side ? myModifier[0] : myModifier[1];
		int modifierLeft = side ? mateModifier[0] : mateModifier[1];
		return new int[] { modifierLeft, modifierRight };
	}

	public static int segragate(int allele) {
		if (allele == HOMOZYGOT_AA || allele == HOMOZYGOT_BB) {
			return allele;
		} else {
			return RandomUtils.coinToss() ? HOMOZYGOT_AA : HOMOZYGOT_BB;
		}
	}

	public static SortedMap<Integer, Integer> zygote(
			SortedSet<Integer> gameteLeft, SortedSet<Integer> gameteRight) {
		SortedMap<Integer, Integer> genome = newGenome();

		for (int gene : gameteLeft) {
			if (gameteRight.remove(gene)) {
				genome.put(gene, HOMOZYGOT_BB);
			} else {
				genome.put(gene, HETEROZYGOT_BA);
			}
		}
		for (int gene : gameteRight) {
			genome.put(gene, HETEROZYGOT_AB);
		}
		return genome;
	}

	public static SortedSet<Integer> segragate(
			SortedMap<Integer, Integer> genome) {
		int side = RandomUtils.coinToss() ? HETEROZYGOT_BA : HETEROZYGOT_AB;
		SortedSet<Integer> gamete = Sets.newTreeSet();
		for (Entry<Integer, Integer> allele : genome.entrySet()) {
			int val = allele.getValue();
			if (val == HOMOZYGOT_BB || val == side) {
				gamete.add(allele.getKey());
			}
		}
		return gamete;
	}

	public SortedMap<Integer, Integer> recombinate() {
		SortedMap<Integer, Integer> newGenome = newGenome(genome);

		int numOfCrossovers = RandomUtils.nextPoisson(getRecombinationRate());
		int[] crossovers = new int[numOfCrossovers + 2];
		crossovers[0] = 0;
		for (int i = 1; i < crossovers.length; i++) {
			crossovers[i] = RandomUtils.nextInt(1, getGenomeSize() - 1);
		}
		crossovers[crossovers.length - 1] = getGenomeSize();
		Arrays.sort(crossovers);
		for (int i = 0; i < crossovers.length - 1; i += 2) {
			crossover(newGenome.subMap(crossovers[i], crossovers[i + 1]));
		}
		return newGenome;
	}

	public static void crossover(SortedMap<Integer, Integer> map) {
		for (int pos : map.keySet()) {
			int allele = map.get(pos);
			if (allele == HETEROZYGOT_AB) {
				map.put(pos, HETEROZYGOT_BA);
			} else if (allele == HETEROZYGOT_BA) {
				map.put(pos, HETEROZYGOT_AB);
			}
		}
	}

	public double[] getNumberOfMutations() {
		double[] mutNum = new double[4];
		for (Entry<Integer, Integer> e : genome.entrySet()) {
			int gene = e.getKey();
			int allele = e.getValue();
			if (gene % getIntervalOfNeutralGenes() != 0) {
				if (allele == HOMOZYGOT_BB) {
					mutNum[0]++; // homozygote deleterious mutation
				} else {
					mutNum[1]++; // heterozygote deleterious mutation
				}
			} else if (allele == HOMOZYGOT_BB) {
				mutNum[2]++; // homozygote neutral mutation BB only
			} else {
				mutNum[3]++; // heterozygote neutral mutation
			}
		}
		return mutNum;
	}

	public double getFitness() {
		/**
		 * directional selection:
		 */
		if (fitness == -1) {
			fitness = Math.pow(1 - heterozygoteCoefficient
					* selectionCoefficient, getNumberOfMutations()[1])
					* Math.pow(1 - selectionCoefficient,
							getNumberOfMutations()[0]);
			/**
			 * DEBUG: multiplicative selection
			 */
			/*
			 * fitness = Math.pow(1 - heterozygoteCoefficient
			 * selectionCoefficient, getNumberOfMutations()[1] + 2
			 * getNumberOfMutations()[0]); // NOTE: multiplicative
			 */}
		if (Double.isNaN(fitness) || fitness < 0 || fitness > 1) {
			throw new RuntimeException("Fitness may not be " + fitness);
		}
		return fitness * (1 - fitnessCost);
	}

	public boolean viable() {
		return getFitness() > getMinimumFitness();
	}

	/*
	 * NOTE: NO BACK MUTATIONS IN FITNESS RELATED GENES. may be added if
	 * reduction in fitness is too fast, and mutational load should be slowed
	 * down. note then that back mutation may be to beneficial as well as
	 * another deleterious. THERE ARE BACK MUTATIONS IN NEUTRAL GENES.
	 */
	public void mutate() {
		int gene = RandomUtils.nextInt(0, getGenomeSize());
		Integer allele = genome.get(gene);
		if (allele == null) {
			if (RandomUtils.coinToss()) {
				genome.put(gene, HETEROZYGOT_AB);
			} else {
				genome.put(gene, HETEROZYGOT_BA);
			}
		} else if (allele == HETEROZYGOT_AB || allele == HETEROZYGOT_BA) {
			if (gene % intervalOfNeutralGenes == 0 && RandomUtils.coinToss()) {
				genome.remove(gene); // go back to AA
			} else {
				genome.put(gene, HOMOZYGOT_BB); // if fitness gene, only
												// deleterious mutation
			}
		}
		// else if (allele == HOMOZYGOT_BB && gene % intervalOfNeutralGenes ==
		// 0) {
		// if (RandomUtils.coinToss()) { // if neutral gene, back mutation is
		// // possible
		// genome.put(gene, HETEROZYGOT_AB);
		// } else {
		// genome.put(gene, HETEROZYGOT_BA);
		// }
		// }
	}

	public boolean isModifierHeterozygote() {
		return outcrossingModifier[0] != outcrossingModifier[1];
	}

	public void addFitnessCost(double cost) {
		fitnessCost = Math.min(1, fitnessCost + cost);
	}

	/**
	 * ascending order
	 */
	@Override
	public int compareTo(Outcrosser o) {
		return Double.compare(this.getFitness(), o.getFitness());
	}

	public void setViabilityLimitFitness(int numberOfDeleteriousHetMutations) {
		minimumFitness = Math.pow(1 - getHeterozygoteCoefficient()
				* getSelectionCoefficient(), numberOfDeleteriousHetMutations);
	}

	public int getViabilityLimit() {
		return (int) (Math.log(getMinimumFitness()) / Math.log(1
				- getHeterozygoteCoefficient() * getSelectionCoefficient()));
	}

	public double getOutcrossingTendency() {
		double tendency = 0;
		if (!isFao()) { // phenotypically
			tendency = getUniformOutcrossingProbability();
		} else {
			// a simple decreasing linear fitness dependence function,
			// generating inherent tendencies between
			// 1 and 0, as a first choice could be: (getFitness() * -1 + 1).
			// however
			// WE can CHOOSE A SIMPLE ***STEP FUNCTION*** , where the step
			// is the mean fitness which above of or equal to there is selfing
			// and below there is outcrossing:
			// tendency = (getFitness() < (((OutcrossModel) Simulation
			// .getInstance().getModel()).getOutcrossingStrategy()
			// .getStepValue())) ? 1 : 0;
			// for now we choose FITNESS (as of 18/07/11):
			tendency = getFitness();
		}
		return tendency;
	}

	public double applyDensityDependence(double input, double density) {
		// a simple sigmoid density dependence function, with its center at
		// density=0.5, when densityDependenceWeight=1:
		double densityDependence = 1.0 / (1.0 + Math
				.exp((-10.0 * density + 5.0)
						* (((OutcrossModel) Simulation.getInstance().getModel())
								.getDensityDependenceWeight())));
		// only if density is higher than 0.5, then there is a positive density
		// effect:

		return input * (0.5 + densityDependence);
	}

	/**
	 * TODO: Yoav please check if there is a way to make the isFao() simpler,
	 * like the commented-out version below - i commented it out because i
	 * thought it didn't catch the case of Fao1-Fao2 heterozygote and maybe
	 * fao-uni hetro (it did catch uni-fao hetro). for the Fao1-Fao2 case i need
	 * the isPolyFao() method, below too - so i enterd all the logic to isFao(),
	 * to save some ifs - is it good?
	 * 
	 */
	/*
	 * public boolean isFao() { if (fao == null) { if (outcrossingModifier[0] ==
	 * HOMOZYGOT_AA) { if (outcrossingModifier[1] == HOMOZYGOT_AA) { // uni fao
	 * = false; } else { // heterozygoe uni-(fao or polyF) fao =
	 * RandomUtils.coinToss(); } } else { // fao fao = true; } } return fao; }
	 */
	public boolean isFao() {
		if (fao == null) {
			if (!isModifierHeterozygote()) {
				if (getOutcrossingModifier()[0] != HOMOZYGOT_AA) {
					// homozygote faofao or fao2fao2
					fao = true;
					if (getOutcrossingModifier()[0] == HOMOZYGOT_BB) {
						fao2 = false;
					} else {
						fao2 = true;
					}
				} else {
					// homozygote uni-uni
					fao = false;
					fao2 = false;
				}
			} else if (getOutcrossingModifier()[0] == HOMOZYGOT_AA
					|| getOutcrossingModifier()[1] == HOMOZYGOT_AA) {
				// heterozygote uni-(fao OR fao2)
				fao = RandomUtils.coinToss();
				if (fao) {
					if (getOutcrossingModifier()[0] == HOMOZYGOT_BB
							|| getOutcrossingModifier()[1] == HOMOZYGOT_BB) {
						fao2 = false;
					} else {
						fao2 = true;
					}
				}
			} else {
				// heterozygote fao-fao2
				fao = true;
				fao2 = RandomUtils.coinToss();
			}
		}
		return fao;
	}

	public boolean isFao2() {
		if (fao2 == null) {
			isFao();
		}
		return fao2;
	}

	public void setFao(boolean fao) {
		this.fao = fao;
	}

	public void setFao2(boolean anotherFao) {
		this.fao2 = anotherFao;
	}

	public void setUniformOutcrossingProbability(
			double uniformOutcrossingProbability) {
		this.uniformOutcrossingProbability = uniformOutcrossingProbability;
	}

	public double getUniformOutcrossingProbability() {
		return uniformOutcrossingProbability;
	}

	public void setFaoOutcrossingProbability(double faoOutcrossingProbability1,
			double faoOutcrossingProbability2) {
		this.faoOutcrossingProbability = new double[] {
				faoOutcrossingProbability1, faoOutcrossingProbability2 };
	}

	public void setFaoOutcrossingProbability(double[] faoOutcrossingProbability) {
		this.faoOutcrossingProbability = faoOutcrossingProbability;
	}

	public double[] getFaoOutcrossingProbability() {
		return faoOutcrossingProbability;
	}

	public SortedMap<Integer, Integer> getGenome() {
		return genome;
	}

	public void setOutcrossingModifier(int outcrossingModifier1,
			int outcrossingModifier2) {
		this.outcrossingModifier = new int[] { outcrossingModifier1,
				outcrossingModifier2 };
	}

	public void setOutcrossingModifier(int[] outcrossingModifier) {
		this.outcrossingModifier = outcrossingModifier;
	}

	public int[] getOutcrossingModifier() {
		return outcrossingModifier;
	}

	public void setGenomeSize(int genomeSize) {
		this.genomeSize = genomeSize;
	}

	public int getGenomeSize() {
		return genomeSize;
	}

	public boolean isBornInPool() {
		return bornInPool;
	}

	public void setBornInPool(boolean born) {
		this.bornInPool = born;
	}

	public void setGenome(SortedMap<Integer, Integer> genome) {
		this.genome = genome;
	}

	public boolean isFemale() {
		return female;
	}

	public void setFemale(boolean female) {
		this.female = female;
	}

	public void setMutationRate(double mutationRate) {
		this.mutationRate = mutationRate;
	}

	public double getMutationRate() {
		return mutationRate;
	}

	public double getRecombinationRate() {
		return recombinationRate;
	}

	public double getSelectionCoefficient() {
		return selectionCoefficient;
	}

	public void setSelectionCoefficient(double selectionCoefficient) {
		this.selectionCoefficient = selectionCoefficient;
	}

	public double getHeterozygoteCoefficient() {
		return heterozygoteCoefficient;
	}

	public void setHeterozygoteCoefficient(double heterozygoteCoefficient) {
		this.heterozygoteCoefficient = heterozygoteCoefficient;
	}

	public void setRecombinationRate(double recombinationRate) {
		this.recombinationRate = recombinationRate;
	}

	public double getMinimumFitness() {
		return minimumFitness;
	}

	public void setMinimumFitness(double minimumFitness) {
		this.minimumFitness = minimumFitness;
	}

	public Object getID() {
		return id;
	}

	public double getFitnessCost() {
		return fitnessCost;
	}

	public double getDeleteriousMutationHeterozygosity() {
		return getNumberOfMutations()[1] / getGenomeSize();
	}

	public double getDeleteriousMutationHomozygosity() {
		return getNumberOfMutations()[0] / getGenomeSize();
	}

	public double getNeutralMutationHeterozygosity() {
		return getNumberOfMutations()[3] / getGenomeSize();
	}

	public double getNeutralMutationHomozygosity() {
		return (genomeSize / getIntervalOfNeutralGenes() - getNumberOfMutations()[3])
				/ getGenomeSize();

	}

	public int getIntervalOfNeutralGenes() {
		return intervalOfNeutralGenes;
	}

	public void setIntervalOfNeutralGenes(int intervalOfNeutralGenes) {
		this.intervalOfNeutralGenes = intervalOfNeutralGenes;
	}
}
