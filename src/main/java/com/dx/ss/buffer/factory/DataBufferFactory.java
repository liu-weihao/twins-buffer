package com.dx.ss.buffer.factory;


import com.dx.ss.buffer.config.DataBufferOptions;
import com.dx.ss.buffer.core.DataBuffer;

public interface DataBufferFactory {

    <E> DataBuffer<E> createDataBuffer(DataBufferOptions options);

}
