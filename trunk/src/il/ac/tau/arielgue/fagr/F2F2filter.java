package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.filters.Filter;

public class F2F2filter implements Filter<Outcrosser> {
	private static int[] PPbenchmark = new int[] { Outcrosser.HOMOZYGOT_CC,
			Outcrosser.HOMOZYGOT_CC };
	@Override
	public boolean filter(Outcrosser o) {
		return o.getOutcrossingModifier() == PPbenchmark;
	}
}
