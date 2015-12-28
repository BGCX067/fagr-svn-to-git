package il.ac.tau.arielgue.fagr.statistics.aggregators;

import il.ac.tau.arielgue.fagr.Outcrosser;
import il.ac.tau.yoavram.pes.statistics.aggregators.Mean;

public class MeanNeutralHeterozygosity extends Mean<Outcrosser> {

	@Override
	protected double extractData(Outcrosser o) {
		return o.getNeutralMutationHeterozygosity();
	}
}