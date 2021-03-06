package il.ac.tau.arielgue.fagr.statistics;

import java.io.IOException;

import il.ac.tau.arielgue.fagr.OutcrossModel;
import il.ac.tau.arielgue.fagr.Outcrosser;
import il.ac.tau.yoavram.pes.filters.Filter;
import il.ac.tau.yoavram.pes.statistics.SimpleDataGatherer;
import il.ac.tau.yoavram.pes.statistics.aggregators.Aggregator;
import il.ac.tau.yoavram.pes.statistics.listeners.DataListener;

public class PoolDataGatherer extends SimpleDataGatherer<Outcrosser> {

	@Override
	public void gather() {
		dataList.clear();
		for (Aggregator<Outcrosser> agg : getAggregators()) {
			agg.clear();
		}
		for (Outcrosser t : ((OutcrossModel) getModel()).getPool()) {
			boolean isFilter = true;
			for (Filter<Outcrosser> filter : getFilters()) {
				if (!filter.filter(t)) {
					isFilter = false;
					break;
				}
			}
			if (isFilter) {
				for (Aggregator<Outcrosser> agg : getAggregators()) {
					agg.aggregate(t);
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
