package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.filters.Filter;

public class FUfilter implements Filter<Outcrosser> {
	private static int[] FUbenchmark = new int[] { Outcrosser.HOMOZYGOT_BB,
			Outcrosser.HOMOZYGOT_AA };
	private static int[] UFbenchmark = new int[] { Outcrosser.HOMOZYGOT_AA,
			Outcrosser.HOMOZYGOT_BB };

	@Override
	public boolean filter(Outcrosser o) {
		return o.getOutcrossingModifier() == FUbenchmark
				|| o.getOutcrossingModifier() == UFbenchmark;
	}
}
