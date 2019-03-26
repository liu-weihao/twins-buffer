package com.dx.ss.buffer.core;


import com.dx.ss.buffer.queue.ArrayCycleQueue;
import com.dx.ss.buffer.queue.CycleQueue;
import com.dx.ss.buffer.storage.PersistStorage;
import com.dx.ss.buffer.storage.TemporaryStorage;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 双Buffer设计，实际上是对两个{@link DataBuffer}的切换和控制。
 */
@Slf4j
public class TwinsBufferPool<E> implements BufferPoolWithStorage<E> {

    /**
     * 缓冲池暂未就绪
     */
    private static final int ST_NOT_READY = 1;

    /**
     * 缓冲池初始化完毕，处于启动状态
     */
    private static final int ST_STARTED = 2;

    /**
     * 如果安全关闭缓冲池，会立即进入此状态
     */
    private static final int ST_SHUTTING_DOWN = 3;

    /**
     * 缓冲池已关闭
     */
    private static final int ST_SHUTDOWN = 4;

    /**
     * 正在进行数据恢复
     */
    private static final int ST_RECOVERING = 5;

    /**
     * 缓冲池当前状态
     */
    private final AtomicInteger state = new AtomicInteger(ST_NOT_READY);

    /**
     * 多线程场景下需要对缓冲池进行加锁使用
     */
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 由两个缓冲区组成的循环队列
     */
    private final CycleQueue<DataBuffer<E>> queue;

    /**
     * 当前所使用的缓冲区
     */
    private volatile DataBuffer<E> currentBuffer;

    /**
     * 预备的缓冲区，当前缓冲区达到阈值，会先切换到预备缓冲区
     */
    private volatile DataBuffer<E> standbyBuffer;

    /**
     * 最大缓冲时限（单位：秒），超过此时间将会触发持久化
     */
    private int bufferTimeInSeconds;

    /**
     * 是否启用临时存储层
     */
    private boolean enableTemporaryStorage;

    /**
     * 如果bufferTimeInSeconds>0，将会启用定时任务
     */
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * 定时flush缓冲区，避免缓冲时间长，数据不落盘
     */
    private final AtomicInteger count = new AtomicInteger(0);

    /**
     * 临时存储层
     */
    private TemporaryStorage<E> temporaryStorage;

    /**
     * 持久化层，flush触发持久化
     */
    private PersistStorage<E> persistStorage;


    /**
     * 双Buffer缓冲池，必须同时指定两个缓冲区。
     *
     * @param buffer1 第一个缓冲区，默认将其作为当前所使用的缓冲区
     * @param buffer2 第二个缓冲区，容量达到阈值可替换buffer1的角色
     */
    public TwinsBufferPool(DataBuffer<E> buffer1, DataBuffer<E> buffer2) {
        //添加两个缓冲区
        List<DataBuffer<E>> buffers = new ArrayList<>(2);
        buffers.add(buffer1);
        buffers.add(buffer2);
        this.queue = new ArrayCycleQueue<>(buffers);
        //将队列移动到第一位
        this.currentBuffer = this.queue.next();

    }

    /**
     * 设置临时存储层。
     *
     * @param storage 存储
     */
    @Override
    public void setTemporaryStorage(TemporaryStorage<E> storage) {
        if (storage == null) throw new NullPointerException("TemporaryStorage is not allowed to be NULL");
        this.temporaryStorage = storage;
    }

    /**
     * 设置持久化层
     *
     * @param storage 持久化
     */
    @Override
    public void setPersistStorage(PersistStorage<E> storage) {
        this.persistStorage = storage;
    }

    /**
     * 从存储层中恢复数据。
     *
     * @return 恢复的数量
     */
    @Override
    public int recovery() {

        //缓冲池状态=正在恢复数据
        this.state.set(ST_RECOVERING);
        List<E> dataList = temporaryStorage.getAll();
        int c = dataList.size();
        if (!dataList.isEmpty()) {
            persistStorage.store(dataList);
            temporaryStorage.clear();
        }
        log.info("恢复了{}条数据", c);
        return c;
    }

    /**
     * 开启缓冲池，进入工作状态
     */
    @Override
    public void start() {
        log.info("缓冲池启动，检测是否有数据需要恢复。");
        recovery();
        if (bufferTimeInSeconds > 0) {
            //一秒钟检测一次，达到上限就flush
            scheduledExecutorService = Executors.newScheduledThreadPool(1);
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                int c = count.incrementAndGet();
                if (c >= bufferTimeInSeconds) {
                    log.warn("缓冲时间达到上限，当前有{}条缓冲数据，自动flush。", currentBuffer.length());
                    flush(currentBuffer);
                }
            }, 0, 1, TimeUnit.SECONDS);
        }
        //缓冲池从数据恢复状态中转变为已启动状态
        this.state.compareAndSet(ST_RECOVERING, ST_STARTED);
    }

    /**
     * 提交一条数据
     *
     * @param data 缓冲数据，不限类型
     */
    @Override
    public void submit(Object data) {
        int s = this.state.get();
        if (isRunning(s)) {
            final ReentrantLock mainLock = this.lock;
            mainLock.lock();
            try {
                if (currentBuffer.reachThreshold()) {
                    log.info("当前Buffer{}已有{}条数据，达到阈值，准备切换Buffer。", currentBuffer.getBufferName(), currentBuffer.length());
                    swap();
                    flush(standbyBuffer);
                }
                E o = currentBuffer.put((E) data);
                if (enableTemporaryStorage && o != null) {
                    //启用存储层
                    if (temporaryStorage != null) {
                        temporaryStorage.store(o);
                    } else {
                        log.warn("Temporary storage is enabled, but the TemporaryStorage is NULL.");
                    }
                }
            } finally {
                mainLock.unlock();
            }
        }
    }

    /**
     * 关闭缓冲池，不再接收任何缓冲数据。
     * 此方法会触发flush动作。
     *
     * @param safe 是否安全关闭，true表示会将依次对
     *             Queue中的缓冲区触发flush动作，
     *             否则只会flush当前缓冲区。
     */
    @Override
    public void shutdown(boolean safe) {
        final ReentrantLock mainLock = this.lock;
        mainLock.lock();
        try {
            //首先要停止定时器
            if (scheduledExecutorService != null) {
                scheduledExecutorService.shutdownNow();
            }
            if (!safe) {
                shutdownNow();
            } else {
                this.state.compareAndSet(ST_STARTED, ST_SHUTTING_DOWN);
                flush(currentBuffer);
                this.state.compareAndSet(ST_SHUTTING_DOWN, ST_SHUTDOWN);
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 关闭缓冲池，不再接收任何缓冲数据。
     * 此方法将不会触发flush动作，有丢失数据的风险。
     *
     * @return 不为空的缓冲区对象实例
     */
    @Override
    public List<DataBuffer> shutdownNow() {
        final ReentrantLock mainLock = this.lock;
        mainLock.lock();
        try {
            //首先要停止定时器
            if (scheduledExecutorService != null) {
                scheduledExecutorService.shutdownNow();
            }
            this.state.compareAndSet(ST_STARTED, ST_SHUTDOWN);
            List<DataBuffer> notEmptyBuffers = new ArrayList<>(2);
            if (!currentBuffer.isEmpty()) {
                notEmptyBuffers.add(currentBuffer);
            }
            DataBuffer next = queue.next();
            if (!next.isEmpty()) {
                notEmptyBuffers.add(next);
            }
            return notEmptyBuffers;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 缓冲池是否已关闭
     */
    @Override
    public boolean isShutdown() {
        return !isRunning(state.get());
    }

    /**
     * 最大缓冲时限（单位：秒），超过此时间将会触发持久化
     */
    @Override
    public void setBufferTimeInSeconds(int bufferTimeInSeconds) {
        this.bufferTimeInSeconds = bufferTimeInSeconds;
    }

    /**
     * 是否启用临时存储层
     */
    @Override
    public void setEnableTemporaryStorage(boolean enableTemporaryStorage) {
        this.enableTemporaryStorage = enableTemporaryStorage;
    }

    /**
     * 交换下一个缓冲区。
     */
    private void swap() {
        this.standbyBuffer = this.currentBuffer;
        this.currentBuffer = queue.next();
        log.info("切换完毕，当前工作Buffer是{}，初始数据量是{}条", currentBuffer.getBufferName(), currentBuffer.length());
    }

    /**
     * 将缓冲区的数据持久化到磁盘。
     * 每次flush都会重置计数器。
     */
    private void flush(DataBuffer<E> buffer) {
        List<E> dataList = new ArrayList<>(buffer.getDataList());
        if (!dataList.isEmpty()) {
            persistStorage.store(dataList);
            buffer.clear();
            temporaryStorage.clear();
        }
        //重置计数器
        count.set(0);
        log.info("当前工作Buffer数据落盘，共计{}条数据。", dataList.size());
    }

    /**
     * 检测是否正在运行
     *
     * @param s 当前状态
     */
    private static boolean isRunning(int s) {
        return s < ST_SHUTDOWN;
    }

}
