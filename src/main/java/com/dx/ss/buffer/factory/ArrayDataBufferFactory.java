package com.dx.ss.buffer.factory;

import com.dx.ss.buffer.config.DataBufferOptions;
import com.dx.ss.buffer.core.ArrayDataBuffer;
import com.dx.ss.buffer.core.DataBuffer;

public class ArrayDataBufferFactory implements DataBufferFactory {

    @Override
    public <E> DataBuffer<E> createDataBuffer(DataBufferOptions options) {
        return new ArrayDataBuffer<>(options);
    }

}
