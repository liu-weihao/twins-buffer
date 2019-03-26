package com.dx.ss.buffer.config;

import com.dx.ss.buffer.core.BufferPool;
import com.dx.ss.buffer.core.DataBuffer;
import com.dx.ss.buffer.core.TwinsBufferPool;
import com.dx.ss.buffer.factory.ArrayDataBufferFactory;
import com.dx.ss.buffer.factory.DataBufferFactory;
import com.dx.ss.buffer.storage.MongoPersistStorage;
import com.dx.ss.buffer.storage.PersistStorage;
import com.dx.ss.buffer.storage.RedisTemporaryStorage;
import com.dx.ss.buffer.storage.TemporaryStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableConfigurationProperties(BufferProperties.class)
public class DataBufferAutoConfiguration {

    private final BufferProperties properties;

    public DataBufferAutoConfiguration(BufferProperties properties) {
        this.properties = properties;
    }

    /**
     * 注入一个缓冲池，默认是双Buffer缓冲池{@link TwinsBufferPool}
     *
     * @return 缓冲池
     */
    @Bean
    @ConditionalOnMissingBean(BufferPool.class)
    public BufferPool bufferPool(TemporaryStorage<Object> temporaryStorage, PersistStorage<Object> persistStorage) {
        DataBufferFactory factory = new ArrayDataBufferFactory();
        DataBufferOptions.Builder builder = new DataBufferOptions.Builder().allowDuplicate(properties.isAllowDuplicate());
        if (properties.getCapacity() != null) {
            builder.capacity(properties.getCapacity());
        }
        if (properties.getThreshold() != null) {
            builder.threshold(properties.getThreshold());
        }
        BufferProperties.Pool pool = properties.getPool();
        DataBufferOptions options = builder.build();
        DataBuffer<Object> buffer1 = factory.createDataBuffer(options);
        DataBuffer<Object> buffer2 = factory.createDataBuffer(options);
        TwinsBufferPool<Object> bufferPool = new TwinsBufferPool<>(buffer1, buffer2);
        bufferPool.setTemporaryStorage(temporaryStorage);
        bufferPool.setPersistStorage(persistStorage);
        bufferPool.setEnableTemporaryStorage(pool.isEnableTemporaryStorage());
        bufferPool.setBufferTimeInSeconds(pool.getBufferTimeInSeconds() != null ? pool.getBufferTimeInSeconds() : BufferProperties.DEFAULT_BUFFER_TIME);
        bufferPool.start();
        return bufferPool;
    }

    /**
     * 注入一个临时存储层，默认是基于Redis实现
     */
    @Bean
    @ConditionalOnMissingBean(TemporaryStorage.class)
    public TemporaryStorage<Object> temporaryStorage(RedisTemplate<String, Object> redisTemplate) {
        return new RedisTemporaryStorage<>(redisTemplate);
    }

    /**
     * 注入一个持久化存储层
     */
    @Bean
    @ConditionalOnMissingBean(PersistStorage.class)
    public PersistStorage<Object> persistStorage(MongoTemplate mongoTemplate) {
        return new MongoPersistStorage<>(mongoTemplate);
    }

    /**
     * 注入MongoTemplate
     *
     * @param mongoDbFactory MongoDB会话工厂
     * @return MongoTemplate
     */
    @Bean
    @ConditionalOnMissingBean(MongoTemplate.class)
    public MongoTemplate getMongoTemplate(MongoDbFactory mongoDbFactory) {
        return new MongoTemplate(mongoDbFactory);
    }

    /**
     * 注入RedisTemplate
     *
     * @param redisConnectionFactory redis会话工厂
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

}
