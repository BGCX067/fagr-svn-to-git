package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.utils.RandomUtils;

public class IdealAncestor extends Outcrosser {
	private static final long serialVersionUID = 90672235573819641L;

	public IdealAncestor() {
		setFemale(RandomUtils.coinToss());
		setGenome(newGenome());
	}

	public IdealAncestor(Outcrosser other) {
		super(other);
	}

	public void init() {
	}
}
