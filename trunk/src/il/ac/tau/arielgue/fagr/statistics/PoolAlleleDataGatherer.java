package il.ac.tau.arielgue.fagr.statistics;

import il.ac.tau.arielgue.fagr.OutcrossModel;
import il.ac.tau.arielgue.fagr.Outcrosser;
import il.ac.tau.yoavram.pes.filters.Filter;
import il.ac.tau.yoavram.pes.statistics.SimpleDataGatherer;
import il.ac.tau.yoavram.pes.statistics.aggregators.Aggregator;
import il.ac.tau.yoavram.pes.statistics.listeners.DataListener;

import java.io.IOException;

public class PoolAlleleDataGatherer extends SimpleDataGatherer<Outcrosser> {
	
	@Override
	public void gather() {
		dataList.clear();
		for (Aggregator<Outcrosser> agg : getAggregators()) {
			agg.clear();
		}

		for (Outcrosser o : ((OutcrossModel) getModel()).getPool()) {
			boolean isFilter = true;
			for (Filter<Outcrosser> filter : getFilters()) {
				if (!filter.filter(o)) {
					isFilter = false;
					break;
				}
			}
			if (isFilter) {
				for (Aggregator<Outcrosser> agg : getAggregators()) {
					agg.aggregate(o);
					if (!o.isModifierHeterozygote()) {
						agg.aggregate(o); // do again for homozygote
											// PROBLEMATIC FOR CALCULATING
											// FRACTIONS - doesn't count 2
											// alleles(diploidy)for the het case
					}
				}
			}
		}

		for (Aggregator<Outcrosser> agg : getAggregators()) {
			dataList.add(agg.result());
		}
		Number[] data = dataList.toArray(EMPTY_NUMBER_ARRAY);
		for (DataListener listener : getListeners()) {
			listener.listen(data);
		}
	}

	@Override
	public void close() throws IOException {
		this.gather();
		super.close();
	}
}
