package com.openxc.sources;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class BytestreamDataSourceTest extends AndroidTestCase {
    BytestreamDataSourceMixin buffer;

    @Override
    public void setUp() {
        buffer = new BytestreamDataSourceMixin();
    }

    @SmallTest
    public void testParseOne() {
        byte[] bytes = new String("{\"key\": \"value\"}\r\n").getBytes();
        buffer.receive(bytes, bytes.length);
        String record = buffer.readLine();
        assertNotNull(record);
        assertTrue(record.indexOf("key") != -1);
        assertTrue(record.indexOf("value") != -1);
    }

    @SmallTest
    public void testParseTwo() {
        byte[] bytes = new String("{\"key\": \"value\"}\r\n").getBytes();
        buffer.receive(bytes, bytes.length);

        bytes = new String("{\"pork\": \"miracle\"}\r\n").getBytes();
        buffer.receive(bytes, bytes.length);

        String record = buffer.readLine();
        assertNotNull(record);
        assertTrue(record.indexOf("key") != -1);
        assertTrue(record.indexOf("value") != -1);

        record = buffer.readLine();
        assertNotNull(record);

        assertTrue(record.indexOf("pork") != -1);
        assertTrue(record.indexOf("miracle") != -1);
    }

    @SmallTest
    public void testLeavePartial() {
        byte[] bytes = new String("{\"key\": \"value\"}\r\n").getBytes();
        buffer.receive(bytes, bytes.length);

        bytes = new String("{\"pork\": \"mira").getBytes();
        buffer.receive(bytes, bytes.length);

        String record = buffer.readLine();
        assertNotNull(record);
        assertTrue(record.indexOf("key") != -1);
        assertTrue(record.indexOf("value") != -1);

    }

    @SmallTest
    public void testCompletePartial() {
        byte[] bytes = new String("{\"key\": \"value\"}\r\n").getBytes();
        buffer.receive(bytes, bytes.length);

        bytes = new String("{\"pork\": \"mira").getBytes();
        buffer.receive(bytes, bytes.length);

        String record = buffer.readLine();
        assertNotNull(record);
        assertTrue(record.indexOf("key") != -1);
        assertTrue(record.indexOf("value") != -1);

        bytes = new String("cle\"}\r\n").getBytes();
        buffer.receive(bytes, bytes.length);

        record = buffer.readLine();
        assertNotNull(record);
        assertTrue("Expected to find pork in " + record,
                record.indexOf("pork") != -1);
        assertTrue(record.indexOf("miracle") != -1);
    }

}
