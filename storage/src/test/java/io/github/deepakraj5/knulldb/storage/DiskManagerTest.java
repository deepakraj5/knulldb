package io.github.deepakraj5.knulldb.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class DiskManagerTest {

    @Test
    void writtenPageCanBeReadBack(@TempDir Path dir) {
        try (DiskManager dm = new DiskManager(dir.resolve("test.db"))) {
            int pageId = dm.allocatePage();

            ByteBuffer out = ByteBuffer.allocate(DiskManager.PAGE_SIZE);
            out.putInt(42).put("knulldb".getBytes());
            dm.writePage(pageId, out);

            ByteBuffer in = ByteBuffer.allocate(DiskManager.PAGE_SIZE);
            dm.readPage(pageId, in);

            Assertions.assertEquals(42, in.getInt());
        }
    }

    @Test
    void dataSurvivesReopen(@TempDir Path dir) {
        Path file = dir.resolve("test.db");

        try (DiskManager dm = new DiskManager(file)) {
            int pageId = dm.allocatePage();
            ByteBuffer buffer = ByteBuffer.allocate(DiskManager.PAGE_SIZE);
            buffer.putInt(7);
            dm.writePage(pageId, buffer);
        }

        try (DiskManager dm = new DiskManager(file)) {
            Assertions.assertEquals(1, dm.numPages());

            ByteBuffer buffer = ByteBuffer.allocate(DiskManager.PAGE_SIZE);
            dm.readPage(0, buffer);

            Assertions.assertEquals(7, buffer.getInt());
        }
    }

    @Test
    void readMissingPageFails(@TempDir Path file) {
        try (DiskManager dm = new DiskManager(file.resolve("test.db"))) {
            ByteBuffer buffer = ByteBuffer.allocate(DiskManager.PAGE_SIZE);
            Assertions.assertThrows(RuntimeException.class, () -> dm.readPage(100, buffer));
        }
    }

}
