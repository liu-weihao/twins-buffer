package com.dx.ss.buffer.storage;

import java.util.List;

public interface PersistStorage<E> {

    /**
     * 存储一条数据。
     */
     E store(E e);

    /**
     * 存储多条数据。
     */
    int store(List<E> list);
}
