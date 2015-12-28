package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.filters.Filter;

public class FFfilter implements Filter<Outcrosser> {
	private static int[] FFbenchmark = new int[] { Outcrosser.HOMOZYGOT_BB,
			Outcrosser.HOMOZYGOT_BB };
	@Override
	public boolean filter(Outcrosser o) {
		return o.getOutcrossingModifier() == FFbenchmark;
	}
}