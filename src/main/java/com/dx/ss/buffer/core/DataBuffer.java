package com.dx.ss.buffer.core;

import java.util.Collection;

/**
 * 缓冲区对象，提供数据对象的缓存，验重。
 * 暂不支持对单个对象的获取、更新操作。
 * 缓冲区内不允许NULL的存在，一切数据对象都是非空的。
 *
 * @param <E> 缓冲的数据类型，最好重写equals和hashcode方法
 */
public interface DataBuffer<E> {

    /**
     * 获取缓冲区名称，可用于多缓冲区场景下的识别。
     *
     * @return 缓冲区默认的命名规则为<code>DataBuffer_</code>+缓冲区对象的hashcode
     */
    default String getBufferName() {
        return "DataBuffer_" + this.hashCode();
    }

    /**
     * 获取缓冲区的数据集合。
     */
    Collection<E> getDataList();

    /**
     * 当前缓冲区的长度
     */
    int length();

    /**
     * 缓冲区是否为空
     */
    boolean isEmpty();

    /**
     * 缓冲区是否已满。
     */
    boolean isFull();

    /**
     * 是否达到了缓冲阈值。
     */
    boolean reachThreshold();

    /**
     * 放入数据。
     *
     * @param entity 数据对象
     */
    E put(E entity);

    /**
     * 批量添加缓冲数据
     *
     * @param c 缓冲数据集合
     */
    void putAll(Collection<? extends E> c);

    /**
     * 是否需要临时存储
     */
    boolean needStore();

    /**
     * 判断数据对象是否存在缓冲区中。
     *
     * @param entity 是否对象
     */
    boolean exists(E entity);

    /**
     * 清空缓冲区。
     */
    void clear();

}
