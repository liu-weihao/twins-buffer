package com.dx.ss.buffer.config;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class DataBufferOptions {
    /**
     * 是否启用数据缓冲区，默认启用
     */
    private boolean enable;

    /**
     * 是否允许重复数据
     */
    private boolean allowDuplicate;

    /**
     * 是否启用临时存储层
     */
    private boolean enableTemporaryStorage;

    /**
     * 缓冲区最大容量
     */
    private int capacity;

    /**
     * 缓冲区容量阈值，达到此阈值将会触发持久化
     */
    private int threshold;

    /**
     * 最大缓冲时限（单位：秒），超过此时间将会触发持久化
     */
    private int bufferTimeInSeconds;


    private DataBufferOptions(Builder builder) {
        this.enable = builder.enable;
        this.allowDuplicate = builder.allowDuplicate;
        this.enableTemporaryStorage = builder.enableTemporaryStorage;
        this.bufferTimeInSeconds = builder.bufferTimeInSeconds;
        this.capacity = builder.capacity;
        this.threshold = builder.threshold.multiply(new BigDecimal(capacity)).intValue();
    }

    public static class Builder {
        /**
         * 是否启用数据缓冲区，默认启用
         */
        private boolean enable = true;

        /**
         * 是否允许重复数据
         */
        private boolean allowDuplicate = true;

        /**
         * 是否启用临时存储层
         */
        private boolean enableTemporaryStorage = false;

        /**
         * 缓冲区最大容量
         */
        private int capacity = BufferProperties.DEFAULT_MAXIMUM_CAPACITY;

        /**
         * 缓冲区容量阈值，由百分比转换过来的小数形式，达到此阈值将会触发持久化
         */
        private BigDecimal threshold = BufferProperties.DEFAULT_THRESHOLD;

        /**
         * 最大缓冲时限（单位：秒），超过此时间将会触发持久化
         */
        private int bufferTimeInSeconds = BufferProperties.DEFAULT_BUFFER_TIME;

        public Builder enable(final boolean enable) {
            this.enable = enable;
            return this;
        }

        public Builder allowDuplicate(final boolean allowDuplicate) {
            this.allowDuplicate = allowDuplicate;
            return this;
        }

        public Builder enableTemporaryStorage(final boolean enableTemporaryStorage) {
            this.enableTemporaryStorage = enableTemporaryStorage;
            return this;
        }

        public Builder capacity(final int capacity) {
            if (capacity <= 0) {
                throw new IllegalArgumentException("capacity is not a legal value.");
            }
            this.capacity = capacity;
            return this;
        }

        public Builder threshold(final BigDecimal threshold) {
            if (threshold.doubleValue() <= 0) {
                throw new IllegalArgumentException("threshold is not a legal value.");
            }
            this.threshold = threshold;
            return this;
        }

        public Builder bufferTimeInSeconds(final int bufferTimeInSeconds) {
            if (bufferTimeInSeconds <= 0) {
                throw new IllegalArgumentException("bufferTimeInSeconds is not a legal value.");
            }
            this.bufferTimeInSeconds = bufferTimeInSeconds;
            return this;
        }

        public DataBufferOptions build() {
            return new DataBufferOptions(this);
        }
    }
}
