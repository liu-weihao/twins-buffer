package com.dx.ss.buffer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Setter
@Getter
@ConfigurationProperties(prefix = "buffer")
public class BufferProperties {

    public static final int DEFAULT_MAXIMUM_CAPACITY = 1000;

    public static final int DEFAULT_BUFFER_TIME = 600;

    public static final BigDecimal DEFAULT_THRESHOLD = new BigDecimal(0.8);

    /**
     * 是否允许重复数据
     */
    private boolean allowDuplicate = true;

    /**
     * 缓冲区最大容量
     */
    private Integer capacity = null;

    /**
     * 缓冲区容量阈值（不超过1），达到此阈值将会触发持久化
     */
    private BigDecimal threshold = null;

    /**
     * 缓冲池配置项
     */
    private Pool pool = new Pool();

    @Getter
    @Setter
    public static class Pool {

        /**
         * 最大缓冲时限（单位：秒），超过此时间将会触发持久化
         */
        private Integer bufferTimeInSeconds = null;

        /**
         * 是否启用临时存储层
         */
        private boolean enableTemporaryStorage = false;
    }
}
