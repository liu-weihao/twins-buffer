package com.dx.ss.buffer.config;

import com.dx.ss.buffer.core.BufferPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * 监听ApplicationContext的关闭事件，触发flush动作。
 */
@Slf4j
@Component
public class ApplicationShutdownEventListener implements ApplicationListener<ContextClosedEvent> {

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        ApplicationContext context = contextClosedEvent.getApplicationContext();
        log.warn("服务关闭，及时保存数据。");
        BufferPool bufferPool = context.getBean(BufferPool.class);
        bufferPool.shutdown(true);
    }
}
