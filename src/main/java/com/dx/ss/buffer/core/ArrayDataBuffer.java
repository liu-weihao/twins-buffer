package com.dx.ss.buffer.core;

import com.dx.ss.buffer.config.DataBufferOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class ArrayDataBuffer<E> implements DataBuffer<E> {

    private List<E> dataList;

    private final boolean enableTemporaryStorage;

    private final boolean allowDuplicate;

    private final int capacity;

    private final int threshold;

    private final DataBufferOptions options;

    public ArrayDataBuffer(DataBufferOptions options) {
        this.dataList = new ArrayList<>();
        this.options = options;
        this.enableTemporaryStorage = options.isEnableTemporaryStorage();
        this.capacity = options.getCapacity();
        this.threshold = options.getThreshold();
        this.allowDuplicate = options.isAllowDuplicate();
    }

    @Override
    public Collection<E> getDataList() {
        return dataList;
    }

    @Override
    public int length() {
        return dataList.size();
    }

    @Override
    public boolean isEmpty() {
        return dataList.isEmpty();
    }

    @Override
    public E put(E entity) {
        if (isFull()) {
            log.warn(this.getBufferName() + "缓冲区已满，数据已丢弃");
            return null;
        }
        if (!allowDuplicate && exists(entity)) {
            log.warn("检测到重复数据，自动丢弃");
            return null;
        }
        if (reachThreshold()) {
            log.warn(this.getBufferName() + "已经达到阈值");
        }
        dataList.add(entity);
        return entity;
    }

    @Override
    public boolean needStore() {
        return this.enableTemporaryStorage;
    }

    @Override
    public void putAll(Collection<? extends E> c) {
        if (isFull()) {
            log.warn(this.getBufferName() + "缓冲区已满，数据已丢弃");
            return;
        }
        if (!allowDuplicate && dataList.containsAll(c)) {
            log.warn("检测到重复数据，自动丢弃");
            return;
        }
        if (reachThreshold()) {
            log.warn(this.getBufferName() + "已经达到阈值");
        }
        dataList.addAll(c);
    }

    @Override
    public boolean exists(E entity) {
        return dataList.contains(entity);
    }

    @Override
    public void clear() {
        dataList.clear();
    }

    /**
     * 缓冲区是否已满。
     */
    @Override
    public boolean isFull() {
        return length() >= this.capacity;
    }

    /**
     * 是否达到了缓冲阈值。
     */
    @Override
    public boolean reachThreshold() {
        return this.threshold <= length();
    }

    public DataBufferOptions getOptions() {
        return options;
    }

    public boolean isEnableTemporaryStorage() {
        return enableTemporaryStorage;
    }

    public boolean isAllowDuplicate() {
        return allowDuplicate;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getThreshold() {
        return threshold;
    }
}
