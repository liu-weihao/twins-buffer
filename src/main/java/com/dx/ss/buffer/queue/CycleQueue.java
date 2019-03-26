package com.dx.ss.buffer.queue;

/**
 * 循环队列接口
 * @param <E> 元素数据类型
 */
public interface CycleQueue<E> {

    /**
     * 队列是否为空
     */
    boolean isEmpty();

    /**
     * 队列长度
     */
    int size();

    /**
     * 在队尾加入一个元素
     */
    boolean offer(E e);

    /**
     * 获取下一个元素
     */
    E next();
}
