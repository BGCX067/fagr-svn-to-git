package il.ac.tau.arielgue.fagr.utils;

import java.util.List;

import il.ac.tau.yoavram.pes.Model;
import il.ac.tau.yoavram.pes.filters.Filter;
import il.ac.tau.yoavram.pes.statistics.aggregators.Aggregator;
import il.ac.tau.yoavram.pes.terminators.AbstractTerminator;
import il.ac.tau.yoavram.pes.terminators.Terminator;

public class ExtinctionAndEquilibriumTerminator<T> extends AbstractTerminator
		implements Terminator {

	Model<T> model;
	Filter<T> filter;
	private int count = 0;
	private int numberOfTicksAfterExtincion = 0; // how many ticks after
													// extinction occurs the
													// simulation should
													// continue

	@Override
	public boolean terminate() {
		if (isExtinced())
			count++;
		else
			count = 0;
		return count > getNumberOfTicksAfterExtincion();
	}

	private boolean isExtinced() {
		for (List<T> population : getModel().getPopulations()) {
			for (T t : population) {
				if (filter.filter(t)) {
					return false;
				}
			}
		}
		return true;
	}

	public void setNumberOfTicksAfterExtincion(int numberOfTicksAfterExtincion) {
		this.numberOfTicksAfterExtincion = numberOfTicksAfterExtincion;
	}

	public int getNumberOfTicksAfterExtincion() {
		return numberOfTicksAfterExtincion;
	}

	public Model<T> getModel() {
		return model;
	}

	public void setModel(Model<T> model) {
		this.model = model;
	}

	public Filter<T> getFilter() {
		return filter;
	}

	public void setFilter(Filter<T> filter) {
		this.filter = filter;
	}
}