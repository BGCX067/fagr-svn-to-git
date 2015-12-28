package il.ac.tau.arielgue.fagr;

public class IdealAncestor extends Outcrosser {

	private static final long serialVersionUID = -5230155575028262723L;

	public IdealAncestor() {
		super();
		setGenome(newGenome());

	}

	public IdealAncestor(Outcrosser other) {
		super(other);
	}

	public void init() {
//		// add some neutral mutations
//		int interval = getIntervalOfNeutralGenes();
//		for (int gene = 0; gene < getGenomeSize(); gene += interval) {
//			getGenome().put(gene, HOMOZYGOT_BB);
//		}

	}
}
