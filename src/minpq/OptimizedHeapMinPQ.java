package minpq;

import java.util.*;

/**
 * Optimized binary heap implementation of the {@link MinPQ} interface.
 *
 * @param <E> the type of elements in this priority queue.
 * @see MinPQ
 */
public class OptimizedHeapMinPQ<E> implements MinPQ<E> {
    /**
     * {@link List} of {@link PriorityNode} objects representing the heap of element-priority pairs.
     */
    private final List<PriorityNode<E>> elements;
    /**
     * {@link Map} of each element to its associated index in the {@code elements} heap.
     */
    private final Map<E, Integer> elementsToIndex;

    /**
     * Constructs an empty instance.
     */
    public OptimizedHeapMinPQ() {
        elements = new ArrayList<>();
        elements.add(null);
        elementsToIndex = new HashMap<>();
    }

    @Override
    public void add(E element, double priority) {
        if (element == null) {
            throw new IllegalArgumentException();
        }
        if (contains(element)) {
            throw new IllegalArgumentException("Already contains " + element);
        }

        PriorityNode<E> newNode = new PriorityNode<>(element, priority);
        elements.add(newNode);
        int index = elements.size() - 1;
        elementsToIndex.put(element, index);
        swim(index);
    }

    @Override
    public boolean contains(E element) {
        return elementsToIndex.containsKey(element);
    }

    @Override
    public double getPriority(E element) {
        if (!contains(element)) {
            throw new NoSuchElementException();
        }
        return elements.get(elementsToIndex.get(element)).getPriority();
    }

    @Override
    public E peekMin() {
        if (elements.size() <= 1) {
            throw new NoSuchElementException("PQ is empty");
        }
        return elements.get(1).getElement();
    }

    @Override
    public E removeMin() {
        if (elements.size() <= 1) {
            throw new NoSuchElementException("PQ is empty");
        }
        return removeAt(1);
    }

    @Override
    public void changePriority(E element, double newPriority) {
        if (!contains(element)) {
            throw new NoSuchElementException();
        }

        int index = elementsToIndex.get(element);
        double oldPriority = elements.get(index).getPriority();
        elements.get(index).setPriority(newPriority);
        if (newPriority < oldPriority) {
            swim(index);
        } else {
            sink(index);
        }
    }

    @Override
    public int size() {
        return elements.size() - 1;
    }

    private void swim(int k) {
        while (k > 1 && greater(parent(k), k)) {
            swap(parent(k), k);
            k = parent(k);
        }
    }

    private void sink(int k) {
        while (2 * k <= size()) {
            int j = 2 * k;
            if (j < size() && greater(j, j + 1)) j++;
            if (!greater(k, j)) break;
            swap(k, j);
            k = j;
        }
    }

    private boolean greater(int i, int j) {
        return elements.get(i).getPriority() > elements.get(j).getPriority();
    }

    private void swap(int i, int j) {
        Collections.swap(elements, i, j);
        elementsToIndex.put(elements.get(i).getElement(), i);
        elementsToIndex.put(elements.get(j).getElement(), j);
    }

    private int parent(int index) {
        return index / 2;
    }

    private E removeAt(int i) {
        if (i == size()) {
            elementsToIndex.remove(elements.get(i).getElement());
            return elements.remove(i).getElement();
        }
        swap(i, size());
        E removedElement = elements.remove(size()).getElement();
        elementsToIndex.remove(removedElement);
        sink(i);
        if (i <= size()) swim(i);
        return removedElement;
    }
}
