package com.dx.ss.buffer;

import com.dx.ss.buffer.core.BufferPool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BufferPoolTests {

    @Autowired
    private BufferPool bufferPool;

    @Test
    public void submitTest() throws Exception {
        for (int i = 10; i < 1000; i++) {
            BufferData data = new BufferData();
            data.setHeader("H" + (Math.random() * i));
            data.setBody("0x" + (Math.random() * i));
            bufferPool.submit(data);
            Thread.sleep((long) (Math.random() * i / 10));
        }
    }
}
