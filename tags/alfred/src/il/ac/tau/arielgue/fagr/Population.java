package il.ac.tau.arielgue.fagr;

import il.ac.tau.yoavram.pes.utils.RandomUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import com.google.common.collect.Lists;

// TODO test if it is serialized
public class Population implements List<Outcrosser>, Serializable {
	private static final long serialVersionUID = 7825535603060745386L;
	// where needed, the list indexes the females after the males
	private List<Outcrosser> males;
	private List<Outcrosser> females;
	private int capacity;
	private double[] femaleRoulette;
	private double[] maleRoulette;

	public Population() {
		males = Lists.newArrayList();
		females = Lists.newArrayList();
	}

	public Population(List<Outcrosser> source) {
		this();
		for (Outcrosser o : source) {
			add(o);
		}
	}

	@Override
	public void clear() {
		maleRoulette = null;
		femaleRoulette = null;
		males.clear();
		females.clear();
	}

	public List<Outcrosser> sort() {
		List<Outcrosser> sorted = Lists.newArrayList(males);
		sorted.addAll(females);
		Collections.sort(sorted);
		return sorted;
	}

	public Outcrosser getRandom() {
		return get(RandomUtils.nextInt(0, size() - 1));
	}

	public Outcrosser getRandom(boolean female) {
		if (female)
			return females.get(RandomUtils.nextInt(0, females.size() - 1));
		else
			return males.get(RandomUtils.nextInt(0, males.size() - 1));
	}

	public Outcrosser getFitRandom() {
		return getFitRandom(RandomUtils.coinToss());
	}

	/**
	 * 
	 * @param female if true returns female if false returns male
	 * @return
	 */
	public Outcrosser getFitRandom(boolean female) {
		double[] roulette = getRoulette(female);
		if (roulette.length == 0)
			return null;
		double rand = RandomUtils.nextDouble();
		int i = 0;
		while (roulette[i] <= rand)
			i++;
		return female ? getFemales().get(i) : getMales().get(i);
	}

	private double[] getRoulette(boolean female) {
		double[] roulette = female ? femaleRoulette : maleRoulette;
		if (roulette == null) { // lazy init
			List<Outcrosser> outcrossers = female ? getFemales() : getMales();
			roulette = new double[outcrossers.size()];
			double sum = 0;
			for (int i = 0; i < roulette.length; i++) {
				sum += outcrossers.get(i).getFitness();
				roulette[i] = sum;
			}
			for (int i = 0; i < roulette.length; i++) {
				roulette[i] = roulette[i] / sum;
			}
			// sanity check
			if (roulette.length > 0 && roulette[0] <= 0) {
				throw new RuntimeException(
						"First item in roulette must be positive, but it is "
								+ roulette[0]);
			}
			if (roulette.length > 0 && roulette[roulette.length - 1] != 1) {
				throw new RuntimeException(
						"Last item in roulette must be 1, but it is "
								+ roulette[roulette.length - 1]);
			}

			if (female)
				femaleRoulette = roulette;
			else
				maleRoulette = roulette;
		}

		return roulette;
	}

	public List<Outcrosser> getMales() {
		return males;
	}

	public void setMales(List<Outcrosser> males) {
		this.males = males;
	}

	public List<Outcrosser> getFemales() {
		return females;
	}

	public void setFemales(List<Outcrosser> females) {
		this.females = females;
	}

	@Override
	public int size() {
		return males.size() + females.size();
	}

	@Override
	public boolean isEmpty() {
		return males.isEmpty() && females.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return males.contains(o) || females.contains(o);
	}

	@Override
	public Iterator<Outcrosser> iterator() {
		return new PopulationIterator();
	}

	@Override
	public Object[] toArray() {
		Outcrosser[] array = new Outcrosser[size()];
		for (int i = 0; i < size(); i++) {
			if (i < males.size()) {
				array[i] = males.get(i);
			} else {
				array[i] = females.get(i - males.size());
			}
		}
		return array;
	}

	public List<Outcrosser> toList() {
		return subList(0, size());
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(Outcrosser e) {
		if (e.isFemale())
			return females.add(e);
		else
			return males.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return males.remove(o) || females.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object a : c) {
			if (!contains(a))
				return false;
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Outcrosser> c) {
		for (Outcrosser a : c) {
			add(a);
		}
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Outcrosser> c) {
		for (Outcrosser a : c) {
			add(index++, a);
		}
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		for (Object a : c) {
			changed = remove(a);
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = males.retainAll(c);
		changed = changed || females.retainAll(c);
		return changed;
	}

	@Override
	public Outcrosser get(int index) {
		if (index < males.size())
			return males.get(index);
		else
			return females.get(index - males.size());
	}

	@Override
	public Outcrosser set(int index, Outcrosser element) {
		if (index < males.size()) {
			if (element.isFemale())
				throw new IllegalArgumentException("Element in index " + index
						+ " is a male, cannot be replaced by a female");
			return males.set(index, element);
		} else {
			if (!element.isFemale())
				throw new IllegalArgumentException("Element in index " + index
						+ " is a female, cannot be replaced by a male");
			return females.set(index - males.size(), element);
		}
	}

	@Override
	public void add(int index, Outcrosser element) {
		if (index < males.size()) {
			if (element.isFemale())
				throw new IllegalArgumentException("Element in index " + index
						+ " is a male, cannot be replaced by a female");
			males.add(index, element);
		} else {
			if (!element.isFemale())
				throw new IllegalArgumentException("Element in index " + index
						+ " is a female, cannot be replaced by a male");
			females.add(index - males.size(), element);
		}
	}

	@Override
	public Outcrosser remove(int index) {
		if (index < males.size())
			return males.remove(index);
		else
			// in the combined list - males are first and the females, hence the
			// need to subtract males.size() from the index (of the combined
			// list) when accessing the females list:
			return females.remove(index - males.size());
	}

	@Override
	public int indexOf(Object o) {
		int index = males.indexOf(o);
		if (index == -1) {
			int find = females.indexOf(o);
			if (find != -1)
				index = males.size() + find;
		}
		return index;
	}

	@Override
	public int lastIndexOf(Object o) {
		int index = females.lastIndexOf(o);
		if (index == -1) {
			index = males.indexOf(o);
		} else {
			index += males.size();
		}
		return index;
	}

	@Override
	public ListIterator<Outcrosser> listIterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<Outcrosser> listIterator(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Outcrosser> subList(int fromIndex, int toIndex) {
		List<Outcrosser> list = Lists.newArrayList();
		for (int i = fromIndex; i < toIndex; i++) {
			list.add(get(i));
		}
		return list;
	}

	public void setCapacity(int capacity) {
		this.capacity = Math.max(0, capacity);
	}

	public int getCapacity() {
		return capacity;
	}

	public double getDensity() {
		return (double) size() / (double) getCapacity();
	}

	public class PopulationIterator implements Iterator<Outcrosser> {
		Iterator<Outcrosser> itFemales;
		Iterator<Outcrosser> itMales;
		boolean male = true;

		public PopulationIterator() {
			itFemales = females.iterator();
			itMales = males.iterator();
		}

		@Override
		public boolean hasNext() {
			return itMales.hasNext() || itFemales.hasNext();
		}

		@Override
		public Outcrosser next() {
			if (itMales.hasNext()) {
				return itMales.next();
			} else if (itFemales.hasNext()) {
				return itFemales.next();
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();

		}

	}
}
