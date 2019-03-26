package com.dx.ss.buffer.core;

import com.dx.ss.buffer.storage.PersistStorage;
import com.dx.ss.buffer.storage.TemporaryStorage;

/**
 * 附带外部存储机制的缓冲池，进一步避免数据丢失。
 */
public interface BufferPoolWithStorage<E> extends BufferPool {

    /**
     * 设置临时存储层。
     *
     * @param storage 存储
     */
    void setTemporaryStorage(TemporaryStorage<E> storage);

    /**
     * 设置持久化层
     *
     * @param storage 持久化
     */
    void setPersistStorage(PersistStorage<E> storage);

    /**
     * 从存储层中恢复数据。
     *
     * @return 恢复的数量
     */
    int recovery();
}
