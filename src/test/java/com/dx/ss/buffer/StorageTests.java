package com.dx.ss.buffer;

import com.dx.ss.buffer.storage.PersistStorage;
import com.dx.ss.buffer.storage.TemporaryStorage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StorageTests {

    @Autowired
    private TemporaryStorage<Object> temporaryStorage;

    @Autowired
    private PersistStorage<Object> persistStorage;

    private List<BufferData> dataList = new ArrayList<BufferData>() {{
        add(new BufferData("H_5560", "0X105EFF"));
        add(new BufferData("H_7100", "0X105F00"));
        add(new BufferData("H_6560", "0X105F10"));
        add(new BufferData("H_1160", "0X105F11"));
        add(new BufferData("H_2160", "0X10FE0F"));
    }};

    @Test
    public void temporaryStorageTest() {
        Assert.assertNotNull(temporaryStorage.store(new BufferData("H_00000", "0xFFFFFF")));
        Assert.assertEquals(5, temporaryStorage.store(dataList));
        List<Object> list = temporaryStorage.getAll();
        Assert.assertEquals(6, list.size());
        temporaryStorage.clear();
    }

    @Test
    public void persistStorageTest() {
        Assert.assertNotNull(persistStorage.store(new BufferData("H_00000", "0xFFFFFF")));
        Assert.assertEquals(5, persistStorage.store(dataList));
    }
}
