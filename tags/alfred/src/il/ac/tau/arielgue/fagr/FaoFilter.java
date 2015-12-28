package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.filters.Filter;

public class FaoFilter implements Filter<Outcrosser> {

	@Override
	public boolean filter(Outcrosser o) {
		return o.isFao();
	}
}
