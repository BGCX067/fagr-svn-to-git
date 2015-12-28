package il.ac.tau.arielgue.fagr;

import java.util.Comparator;

public class OutcrosserDescendingFitnessComparator implements
		Comparator<Outcrosser> {
	/**
	 * TODO: make sure to use default comparator of sort (which is the compareTo
	 * of the object), kill this compartor, and fix compareTo in outcrosser
	 */
	@Override
	public int compare(Outcrosser o1, Outcrosser o2) {
		// return Integer.MAX_VALUE;
		// return Double.compare(o2.getFitness(), o1.getFitness());
		int res = Integer.MAX_VALUE;
		if (o1.getFitness() < o2.getFitness()) {
			return 1;
		} else if (o1.getFitness() == o2.getFitness()) {
			return 0;
		} else {
			return -1;
		}

	}
}
