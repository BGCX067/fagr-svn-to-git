package il.ac.tau.arielgue.fagr.statistics;

import il.ac.tau.arielgue.fagr.Outcrosser;
import il.ac.tau.yoavram.pes.Simulation;
import il.ac.tau.yoavram.pes.filters.Filter;
import il.ac.tau.yoavram.pes.statistics.SimpleDataGatherer;
import il.ac.tau.yoavram.pes.statistics.aggregators.Aggregator;
import il.ac.tau.yoavram.pes.statistics.listeners.DataListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PerDemeDataGatherer extends SimpleDataGatherer<Outcrosser> {
	public PerDemeDataGatherer() {
		super();
	}

	public void init() {
		List<String> aggList = new ArrayList<String>(getAggregators().size());
		for (Aggregator<Outcrosser> agg : getAggregators()) {
			aggList.add(agg.getName() + " mean");
			aggList.add(agg.getName() + " stdev");
		}
		for (DataListener dl : getListeners()) {
			dl.setDataFieldNames(aggList);
		}
	}

	@Override
	public void gather() {
		Number[][] data = new Number[getModel().getPopulations().size()][];

		for (int popInd = 0; popInd < getModel().getPopulations().size(); popInd++) {
			List<Outcrosser> pop = getModel().getPopulations().get(popInd);
			for (Outcrosser t : pop) {
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
			data[popInd] = new Number[getAggregators().size()];
			for (int i = 0; i < data[popInd].length; i++) {
				data[popInd][i] = getAggregators().get(i).result();
				getAggregators().get(i).clear();
			}
		}

		Number[] output = new Number[getAggregators().size() * 2];
		for (int aggInd = 0; aggInd < getAggregators().size(); aggInd++) {
			Number m = mean(data, aggInd);
			output[aggInd * 2] = m;
			output[aggInd * 2 + 1] = stdev(data, aggInd, m.doubleValue());
		}

		for (DataListener listener : getListeners()) {
			listener.listen(output);
		}
	}

	private Number mean(Number[][] data, int col) {
		double sum = 0;
		for (int row = 0; row < data.length; row++) {
			sum += (data[row][col]).doubleValue();
		}
		return sum / (double) data.length;
	}

	private Number stdev(Number[][] data, int col, double mean) {
		double sum = 0;
		for (int row = 0; row < data.length; row++) {
			sum += Math.pow((data[row][col]).doubleValue() - mean, 2);
		}
		return Math.sqrt(sum / (double) data.length);
	}

	@Override
	public int getInterval() {
		if (Simulation.getInstance().getTick() == 1)
			return 1;
		else {
			return super.getInterval();
		}
	}

	@Override
	public void close() throws IOException {
		this.gather(); // REMOVE IN PES2011
		super.close();
	}
}
