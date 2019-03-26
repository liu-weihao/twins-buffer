package com.dx.ss.buffer.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

@Slf4j
public class MongoPersistStorage<E> implements PersistStorage<E> {

    private final MongoTemplate mongoTemplate;

    public MongoPersistStorage(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 存储一条数据。
     */
    @Override
    public E store(E e) {
        try {
            mongoTemplate.insert(e);
            return e;
        } catch (Exception ex) {
            log.error("插入Document出现异常，{}", ex);
        }
        return null;
    }

    /**
     * 存储多条数据。
     */
    @Override
    public int store(List<E> list) {
        try {
            mongoTemplate.insertAll(list);
            return list.size();
        } catch (Exception ex) {
            log.error("批量插入Document出现异常，{}", ex);
        }
        return 0;
    }
}
