package il.ac.tau.arielgue.fagr.statistics;

import il.ac.tau.arielgue.fagr.Outcrosser;
import il.ac.tau.yoavram.pes.Simulation;
import il.ac.tau.yoavram.pes.filters.Filter;
import il.ac.tau.yoavram.pes.statistics.SimpleDataGatherer;
import il.ac.tau.yoavram.pes.statistics.aggregators.Aggregator;
import il.ac.tau.yoavram.pes.statistics.listeners.DataListener;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

//NUBY'S NOTE: not generic <T>, because SpringRunner currently doesn't support this directly
public class DemeDataGatherer extends SimpleDataGatherer<Outcrosser> {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DemeDataGatherer.class);

	private int numberOfDemes;
	private Number[] data = new Number[getNumberOfDemes() + 1];

	public DemeDataGatherer() {
		super();
	}

	public void init() {
		List<String> headers = Lists
				.newArrayListWithExpectedSize(getNumberOfDemes());
		headers.add(getAggregators().get(0).getName());
		for (int i = 0; i < getNumberOfDemes(); i++) {
			headers.add("deme " + i);
		}
		for (DataListener dl : getListeners()) {
			dl.setDataFieldNames(headers);
		}
		data = new Number[getNumberOfDemes() + 1];
	}

	@Override
	public void gather() {
		data[0] = getAggregators().get(0).result(); // tick aggregator
		List<List<Outcrosser>> populations = getModel().getPopulations();
		Aggregator<Outcrosser> agg = getAggregators().get(1); // chosen
																// aggregator

		for (int i = 0; i < populations.size(); i++) {
			List<Outcrosser> pop = populations.get(i);
			for (Outcrosser t : pop) {
				boolean isFilter = true;
				for (Filter<Outcrosser> filter : getFilters()) {
					if (!filter.filter(t)) {
						isFilter = false;
						break;
					}
				}
				if (isFilter) {
					agg.aggregate(t);
				}
			}
			data[i + 1] = agg.result();
			agg.clear();
		}
		for (DataListener listener : getListeners()) {
			listener.listen(data);
		}
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
		this.gather();
		super.close();
	}

	public void setNumberOfDemes(int numberOfDemes) {
		this.numberOfDemes = numberOfDemes;
	}

	public int getNumberOfDemes() {
		return numberOfDemes;
	}
}