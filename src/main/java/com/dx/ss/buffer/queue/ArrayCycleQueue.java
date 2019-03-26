package com.dx.ss.buffer.queue;

import java.util.ArrayList;
import java.util.List;


/**
 * 循环队列，非线程安全，
 * 可动态扩容，不可移除元素。
 *
 * @param <E> 队列元素类型
 */
public class ArrayCycleQueue<E> implements CycleQueue<E> {

    /**
     * 默认的初始容量
     */
    private static final int DEFAULT_CAPACITY = 2;

    /**
     * 队列指针
     */
    private int curr;

    /**
     * 队列元素，非空
     */
    private List<E> items;

    public ArrayCycleQueue() {
        this.curr = -1;
        this.items = new ArrayList<>(DEFAULT_CAPACITY);
    }

    public ArrayCycleQueue(int initialCapacity) {
        if (initialCapacity > 0) {
            this.items = new ArrayList<>(initialCapacity);
        } else if (initialCapacity == 0) {
            this.items = new ArrayList<>(DEFAULT_CAPACITY);
        } else {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
    }

    public ArrayCycleQueue(List<E> entities) {
        this();
        if (entities != null) {
            this.items.addAll(entities);
        }
    }

    /**
     * 队列是否为空
     */
    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public int size() {
        return this.items.size();
    }

    @Override
    public boolean offer(E e) {
        return this.items.add(e);
    }

    @Override
    public E next() {
        if (this.items.isEmpty()) return null;
        this.curr = (this.curr + 1) % size();
        return items.get(this.curr);
    }

}
