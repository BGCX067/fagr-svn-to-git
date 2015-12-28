package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.filters.Filter;
import il.ac.tau.yoavram.pes.statistics.aggregators.Aggregator;

/**
 * 
 * this class is used as a filter and also as an aggregator-for-calculating
 * fractions(which itself needs setting a reference to a filter (in the *.xml
 * file))
 * 
 */
public class AlleleFilterAggregator implements Filter<Outcrosser>,
		Aggregator<Outcrosser> {

	private boolean count = false;
	private Filter<Outcrosser> filter;
	private String name;
	private double filtered = 0;
	private double aggregated = 0;

	@Override
	public boolean filter(Outcrosser o) {
		filtered += 2;
		return filter.filter(o);
	}

	@Override
	public Aggregator<Outcrosser> aggregate(Outcrosser arg0) {
		aggregated++;
		return this;
	}

	@Override
	public void clear() {
		filtered = 0;
		aggregated = 0;
	}

	@Override
	public Number result() {
		if (count)
			return aggregated;
		else //fraction
			return aggregated / filtered; // Returns the fraction of alleles that passed the filter out of the total alleles the aggregator goes through
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setFilter(Filter<Outcrosser> filter) {
		this.filter = filter;
	}

	public Filter<Outcrosser> getFilter() {
		return filter;
	}

	public boolean isCount() {
		return count;
	}

	public void setCount(boolean count) {
		this.count = count;
	}
}
