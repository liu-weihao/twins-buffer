package com.dx.ss.buffer.core;

import java.util.List;

/**
 * 有状态标识的缓冲池，可以同时对
 * 多个{@link DataBuffer}缓冲区进行切换和控制操作。
 */
public interface BufferPool {

    /**
     * 获取缓冲池的名称
     */
    default String getBufferPoolName() {
        return "BuffPool_" + this.hashCode();
    }

    /**
     * 开启缓冲池，进入工作状态
     */
    void start();

    /**
     * 提交一条数据
     *
     * @param data 缓冲数据，不限类型
     */
    void submit(Object data);

    /**
     * 关闭缓冲池，不再接收任何缓冲数据。
     * 此方法会触发flush动作。
     *
     * @param safe 是否安全关闭，true表示会将依次对
     *             Queue中的缓冲区触发flush动作，
     *             否则只会flush当前缓冲区。
     */
    void shutdown(boolean safe);

    /**
     * 关闭缓冲池，不再接收任何缓冲数据。
     * 此方法将不会触发flush动作，有丢失数据的风险。
     *
     * @return 不为空的缓冲区对象实例
     */
    List<DataBuffer> shutdownNow();

    /**
     * 缓冲池是否已关闭
     */
    boolean isShutdown();

    /**
     * 最大缓冲时限（单位：秒），超过此时间将会触发持久化
     */
    void setBufferTimeInSeconds(int bufferTimeInSeconds);

    /**
     * 是否启用临时存储层
     */
    void setEnableTemporaryStorage(boolean enableTemporaryStorage);
}
