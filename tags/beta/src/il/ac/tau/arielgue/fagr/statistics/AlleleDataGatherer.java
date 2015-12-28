package il.ac.tau.arielgue.fagr.statistics;

import il.ac.tau.arielgue.fagr.Outcrosser;
import il.ac.tau.yoavram.pes.filters.Filter;
import il.ac.tau.yoavram.pes.statistics.SimpleDataGatherer;
import il.ac.tau.yoavram.pes.statistics.aggregators.Aggregator;
import il.ac.tau.yoavram.pes.statistics.listeners.DataListener;

import java.util.List;

public class AlleleDataGatherer extends SimpleDataGatherer<Outcrosser> {

	public void init() {
		super.init();
		if (getFilters() == null || getFilters().isEmpty()) {
			throw new IllegalStateException(
					"Must have at least one allele filter!");
		}
	}

	@Override
	public void gather() {
		dataList.clear();
		for (Aggregator<Outcrosser> agg : getAggregators()) {
			agg.clear();
		}
		for (List<Outcrosser> pop : getModel().getPopulations()) {
			for (Outcrosser t : pop) {
				boolean isFilter = true;
				for (Filter<Outcrosser> alleleFilter : getFilters()) {
					if (!alleleFilter.filter(t)) {
						isFilter = false;
						break;
					}
				}
				if (isFilter) {
					for (Aggregator<Outcrosser> agg : getAggregators()) {
						agg.aggregate(t);
						if (!t.isHeterozygote()) {
							agg.aggregate(t); // do again for homozygote.

						}
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

}
