package com.dx.ss.buffer.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Slf4j
public class RedisTemporaryStorage<E> implements TemporaryStorage<E> {

    /**
     * 默认的缓存KEY
     */
    private static final String DEFAULT_CACHE_KEY = "@TEMP_BUFFER_DATA";

    /**
     * 缓存的KEY
     */
    private String cacheKey;

    /**
     * 使用SET数据结构存储
     */
    private final ListOperations<String, Object> operations;

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisTemporaryStorage(RedisTemplate<String, Object> redisTemplate) {
        this.cacheKey = DEFAULT_CACHE_KEY;
        this.redisTemplate = redisTemplate;
        this.operations = redisTemplate.opsForList();

    }

    /**
     * 存储一条数据。
     *
     * @param e 数据对象
     */
    @Override
    public E store(E e) {
        Long c = operations.rightPush(cacheKey, e);
        if (c != null && c > 0) {
            return e;
        }
        return null;
    }

    /**
     * 存储多条数据。
     *
     * @param list 数据列表
     */
    @Override
    public int store(List<E> list) {
        Long c = operations.rightPushAll(cacheKey, list);
        if (c != null && c > 0) {
            return list.size();
        }
        return 0;
    }

    /**
     * 获取全部的临时存储数据。
     */
    @Override
    public List<E> getAll() {
        return (List<E>) operations.range(cacheKey, 0, -1);
    }

    /**
     * 清空临时存储的数据。
     */
    @Override
    public void clear() {
        redisTemplate.delete(cacheKey);
    }


    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = (cacheKey == null || cacheKey.isEmpty()) ? DEFAULT_CACHE_KEY : cacheKey;
    }
}
