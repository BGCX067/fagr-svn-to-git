package il.ac.tau.arielgue.fagr.statistics.aggregators;

import il.ac.tau.yoavram.pes.filters.Filter;
import il.ac.tau.yoavram.pes.statistics.aggregators.AbstractAggregator;
import il.ac.tau.yoavram.pes.statistics.aggregators.Aggregator;
/**
 * no need to use this class. use AlleleFilterAggregator instead
 */
public class FilterCount<T> extends AbstractAggregator<T> implements
		Aggregator<T> {

	private double total = 0;
	private double filtered = 0;
	private Filter<T> filter = null;

	@Override
	public Aggregator<T> aggregate(T input) {
		total++;
		if (filter==null ||getFilter().filter(input))
			filtered++;
		return this;
	}

	@Override
	public Number result() {
		return filtered;
	}

	@Override
	public void clear() {
		total = 0;
		filtered = 0;
	}

	public Filter<T> getFilter() {
		return filter;
	}

	public void setFilter(Filter<T> filter) {
		this.filter = filter;
	}
}
