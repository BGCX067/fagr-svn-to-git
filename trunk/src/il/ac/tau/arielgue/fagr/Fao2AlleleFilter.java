package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.filters.Filter;

public class Fao2AlleleFilter implements Filter<Outcrosser> {

	@Override
	public boolean filter(Outcrosser o) {
		return o.getOutcrossingModifier()[0] == Outcrosser.HOMOZYGOT_CC
				|| o.getOutcrossingModifier()[1] == Outcrosser.HOMOZYGOT_CC;
	}
}
