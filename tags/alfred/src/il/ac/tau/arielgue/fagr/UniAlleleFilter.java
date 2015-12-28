package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.filters.Filter;

public class UniAlleleFilter implements Filter<Outcrosser> {
	
	@Override
	public boolean filter(Outcrosser o) {
		return o.getOutcrossingModifier() != Outcrosser.HOMOZYGOT_BB; // AA =
																		// homozygote
																		// Uniform
																		// outcrosser;
																		// BB=homozygote
																		// FAO
	}
}
