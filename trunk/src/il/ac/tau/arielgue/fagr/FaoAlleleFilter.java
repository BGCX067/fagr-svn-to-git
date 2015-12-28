package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.filters.Filter;

public class FaoAlleleFilter implements Filter<Outcrosser> {

	@Override
	public boolean filter(Outcrosser o) {
		return o.getOutcrossingModifier()[0] == Outcrosser.HOMOZYGOT_BB
				|| o.getOutcrossingModifier()[1] == Outcrosser.HOMOZYGOT_BB;
	}
}
