package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.filters.Filter;

public class UUfilter implements Filter<Outcrosser> {
	private static int[] UUbenchmark = new int[] { Outcrosser.HOMOZYGOT_AA,
			Outcrosser.HOMOZYGOT_AA };

	@Override
	public boolean filter(Outcrosser o) {
		return o.getOutcrossingModifier() == UUbenchmark;
	}
}
