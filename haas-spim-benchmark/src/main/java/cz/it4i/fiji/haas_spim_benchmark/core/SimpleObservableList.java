
package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.collections.ModifiableObservableListBase;

public class SimpleObservableList<T> extends ModifiableObservableListBase<T> {

	private final List<T> innerList;
	private final Runnable numberOfListenersChangedCallback;
	private int numberOfSubscribedListeners = 0;
	
	public SimpleObservableList(final List<T> list) {
		this(list, null);
	}

	public SimpleObservableList(final List<T> list,
		Runnable numberOfListenersCallback)
	{
		this.innerList = list;
		this.numberOfListenersChangedCallback = numberOfListenersCallback;
	}

	@Override
	public T get(int index) {
		return innerList.get(index);
	}

	@Override
	public int size() {
		return innerList.size();
	}

	@Override
	protected void doAdd(int index, T element) {
		innerList.add(index, element);
	}

	@Override
	protected T doSet(int index, T element) {
		return innerList.set(index, element);
	}

	@Override
	protected T doRemove(int index) {
		return innerList.remove(index);
	}

	synchronized public boolean hasAnyListeners() {
		assert numberOfSubscribedListeners >= 0;
		return numberOfSubscribedListeners > 0;
	}

	
	synchronized public void subscribe(ListChangeListener<? super T> listener) {
		super.addListener(listener);
		numberOfSubscribedListeners++;
		if (numberOfListenersChangedCallback != null) {
			numberOfListenersChangedCallback.run();
		}
	}

	synchronized public void unsubscribe(ListChangeListener<? super T> listener) {
		super.removeListener(listener);
		numberOfSubscribedListeners--;
		if (numberOfListenersChangedCallback != null) {
			numberOfListenersChangedCallback.run();
		}
	}

}
