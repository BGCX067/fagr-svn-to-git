package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.utils.RandomUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Outcrosser implements Serializable, Comparable<Outcrosser> {
	private static final long serialVersionUID = -528607464671051539L;
	// private static final Logger logger = Logger.getLogger(Outcrosser.class);

	private static int NEXT_ID = 0;
	protected static final int DEFAULT_VALUE = -1;

	/**
	 * A=non deleterious allele AND Uniform outcrossing allele
	 */
	public static final int HOMOZYGOT_AA = 0;
	public static final int HETEROZYGOT_BA = 1;
	public static final int HETEROZYGOT_AB = 2;
	/**
	 * B=deleterious allele AND FAO outcrossing allele
	 */
	public static final int HOMOZYGOT_BB = 3;

	private int id = NEXT_ID++;

	private int genomeSize = 10000;
	private double selectionCoefficient = Double.NaN;
	private double heterozygoteCoefficient = Double.NaN;
	private double mutationRate = Double.NaN;
	private double recombinationRate = Double.NaN;
	private double minimumFitness = Double.NaN;
	private double uniformOutcrossingProbability = Double.NaN;

	/**
	 * the default is a homozygote for Uniform outcrossing, and heterozygote not
	 * decided=-1;
	 */
	private int outcrossingModifier = HOMOZYGOT_AA;
	private Boolean fao = null;

	// the locations of deleterious mutations, maximum size=limitOfViability:
	private SortedMap<Integer, Integer> genome = null;
	private boolean female = true;
	private double fitness = -1;
	private double fitnessCost = 0;

	protected Outcrosser() {
	}

	public Outcrosser(Outcrosser other) {
		this.genomeSize = other.genomeSize;
		this.selectionCoefficient = other.selectionCoefficient;
		this.heterozygoteCoefficient = other.heterozygoteCoefficient;
		this.mutationRate = other.mutationRate;
		this.recombinationRate = other.recombinationRate;
		this.minimumFitness = other.minimumFitness;
		this.uniformOutcrossingProbability = other.uniformOutcrossingProbability;
		// coinTOss the female boolean because it is called at the end by
		// shild.spawn() in reproduction and we want 50-50:
		female = RandomUtils.coinToss();
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
		Outcrosser child = spawn(); // includes coinToss for male/female
		child.setGenome(zygote(thisGamete, mateGamete));
		child.setOutcrossingModifier(zygoteModifier(getOutcrossingModifier(),
				mate.getOutcrossingModifier())); // the modifier has free
													// recombination with the
													// genome, so one coin toss
													// is enough to include
													// recombination and
													// segregation

		// mutation
		int mutations = RandomUtils.nextPoisson(getMutationRate());
		for (int m = 0; m < mutations; m++) {
			child.mutate();
		}

		return child;
	}

	private int zygoteModifier(int myModifier, int mateModifier) {
		boolean side = RandomUtils.coinToss();
		int modifierRight = side ? segragate(myModifier)
				: segragate(mateModifier);
		int modifierLeft = side ? segragate(mateModifier)
				: segragate(myModifier);

		if (modifierLeft == modifierRight)
			return modifierRight;
		else if (modifierRight == HOMOZYGOT_AA)
			return HETEROZYGOT_BA;
		else
			// if (modifierLeft == HOMOZYGOT_AA)
			return HETEROZYGOT_AB;
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

	public double getFitness() {
		if (fitness == -1) {
			int het = 0;
			int hom = 0;
			for (int allele : genome.values()) {
				if (allele == HOMOZYGOT_BB) {
					hom++;
				} else {
					het++;
				}
			}
			fitness = Math.pow(1 - heterozygoteCoefficient
					* selectionCoefficient, het)
					* Math.pow(1 - selectionCoefficient, hom);
		}
		return fitness * (1 - fitnessCost);
	}

	public boolean viable() {
		return getFitness() > getMinimumFitness();
	}

	/*
	 * NOTE: NO BACK MUTATIONS . may be added if reduction in fitness is too
	 * fast, and mutational load should be slowed down. note then that back
	 * mutation may be to beneficial as well as another deleterious.
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
			genome.put(gene, HOMOZYGOT_BB);
		}
	}

	public SortedMap<Integer, Integer> getGenome() {
		return genome;
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

	public boolean isHeterozygote() {
		return outcrossingModifier == HETEROZYGOT_AB
				|| outcrossingModifier == HETEROZYGOT_BA;
	}

	public void setOutcrossingModifier(int outcrossingModifier) {
		this.outcrossingModifier = outcrossingModifier;
	}

	public int getOutcrossingModifier() {
		return outcrossingModifier;
	}

	public void setGenomeSize(int genomeSize) {
		this.genomeSize = genomeSize;
	}

	public int getGenomeSize() {
		return genomeSize;
	}

	public void addFitnessCost(double cost) {
		fitnessCost = Math.min(1, fitnessCost + cost);
	}

	public Object getID() {
		return id;
	}

	/**
	 * descending order
	 */
	@Override
	public int compareTo(Outcrosser o) {
		if (this.getFitness() < o.getFitness())
			return 1;
		else if (this.getFitness() == o.getFitness())
			return 0;
		else
			return -1;
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

	public int getHeterozygoteMutationNumber() {
		int het = 0;
		for (int allele : genome.values()) {
			if (allele != HOMOZYGOT_BB) {
				het++;
			}
		}
		return het;
	}

	public void setViabilityLimitFitness(int numberOfDeleteriousHetMutations) {
		minimumFitness = Math.pow(1 - getHeterozygoteCoefficient()
				* getSelectionCoefficient(), numberOfDeleteriousHetMutations);
	}

	public int getViabilityLimit() {
		return (int) (Math.log(getMinimumFitness()) / Math.log(1
				- getHeterozygoteCoefficient() * getSelectionCoefficient()));
	}

	public double[] getOutcrossingTendency(double density) {
		double resultsArray[] = { -1, -1 };
		double inherentTendency = 0;
		if (!isFao()) {
			inherentTendency = getUniformOutcrossingProbability();
		} else {
			// a simple decreasing linear fitness dependence function,
			// generating inherent tendencies between
			// UniformOutcrossingProbability*1.3333 and
			// UniformOutcrossingProbability*0.6666, as a first choice:
			inherentTendency = getUniformOutcrossingProbability()
					* (getFitness() * -2.0 / 3.0 + 4.0 / 3.0);
		}
		// a simple sigmoid density dependence function, with its center at
		// density=0.5:
		double densityDependence = 1.0 / (1.0 + Math.exp(-10.0 * density + 5.0));
		// only if density is higher than 0.5, then there is a positive density
		// effect:

		resultsArray[0] = inherentTendency;
		resultsArray[1] = inherentTendency * (0.5 + densityDependence);
		return resultsArray;
	}

	public boolean isFao() {
		if (fao == null) {
			if (outcrossingModifier == HETEROZYGOT_BA
					|| outcrossingModifier == HETEROZYGOT_AB) {
				// when tossing coin make sure save result, don't toss coin
				// twice
				// for same individual (HETRO):
				fao = RandomUtils.coinToss();
			} else {
				fao = outcrossingModifier == HOMOZYGOT_BB;
			}
		}
		return fao;
	}

	public void setFao(boolean fao) {
		this.fao = fao;
	}

	public void setUniformOutcrossingProbability(
			double uniformOutcrossingProbability) {
		this.uniformOutcrossingProbability = uniformOutcrossingProbability;
	}

	public double getUniformOutcrossingProbability() {
		return uniformOutcrossingProbability;
	}
}
