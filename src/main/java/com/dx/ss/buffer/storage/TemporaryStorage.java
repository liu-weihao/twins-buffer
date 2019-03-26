package com.dx.ss.buffer.storage;

import java.util.List;

/**
 * 为缓冲池为设置的临时存储层，通过外部缓存，或者
 * 准实时的持久化，进一步避免数据丢失的情况。
 * 临时存储层会在缓冲池工作的时候，同步更新。
 * 如果缓冲池遇到意外情况而被迫中断，下次缓冲池恢复的
 * 时候，将会先将临时存储的数据进行持久化落盘。
 */
public interface TemporaryStorage<E> {

    /**
     * 存储一条数据。
     */
    E store(E e);

    /**
     * 存储多条数据。
     */
    int store(List<E> list);

    /**
     * 获取全部的临时存储数据。
     */
    List<E> getAll();

    /**
     * 清空临时存储的数据。
     */
    void clear();
}
