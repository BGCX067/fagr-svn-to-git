package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.filters.Filter;

public class UniFilter implements Filter<Outcrosser> {

	@Override
	public boolean filter(Outcrosser o) {
		return !o.isFao();
	}
}