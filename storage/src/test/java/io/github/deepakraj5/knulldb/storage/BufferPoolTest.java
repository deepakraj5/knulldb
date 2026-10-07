package io.github.deepakraj5.knulldb.storage;

import io.github.deepakraj5.knulldb.common.DbException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class BufferPoolTest {

    @Test
    void evictedDirtyPagIsWrittenToDisk(@TempDir Path dir) {
        try (DiskManager dm = new DiskManager(dir.resolve("test.db"))) {
            BufferPool pool = new BufferPool(dm, 2);

            Page p0 = pool.newPage();
            p0.getData().putInt(0 ,42);
            pool.unpinPage(p0.getPageId(), true);

            Page p1 = pool.newPage();
            pool.unpinPage(p1.getPageId(), false);

            Page p2 = pool.newPage();
            pool.unpinPage(p2.getPageId(), false);

            Assertions.assertFalse(pool.contains(0));

            Page storedPage = pool.fetchPage(0);
            Assertions.assertEquals(42, storedPage.getData().getInt(0));
        }
    }

    @Test
    void leastRecentlyUsedPageIsEvicted(@TempDir Path dir) {
        try (DiskManager dm = new DiskManager(dir.resolve("test.db"))) {
            BufferPool pool = new BufferPool(dm, 2);

            pool.unpinPage(pool.newPage().getPageId(), false);
            pool.unpinPage(pool.newPage().getPageId(), false);

            pool.fetchPage(0);
            pool.unpinPage(0, false);

            pool.newPage();

            Assertions.assertTrue(pool.contains(0));
            Assertions.assertFalse(pool.contains(1));
        }
    }

    @Test
    void pinnedPagesAreNeverEvicted(@TempDir Path dir) {
        try (DiskManager dm = new DiskManager(dir.resolve("test.db"))) {
            BufferPool pool = new BufferPool(dm, 2);

            pool.newPage();
            pool.newPage();

            Assertions.assertThrows(DbException.class, () -> pool.newPage());
        }
    }

    @Test
    void flushAllWriteDirtyPages(@TempDir Path dir) {
        try (DiskManager dm = new DiskManager(dir.resolve("test.db"))) {
            BufferPool pool = new BufferPool(dm, 2);

            Page p0 = pool.newPage();
            p0.getData().putInt(0 ,92);
            pool.unpinPage(p0.getPageId(), true);
            pool.flushAll();

            ByteBuffer raw = ByteBuffer.allocate(DiskManager.PAGE_SIZE);
            dm.readPage(p0.getPageId(), raw);

            Assertions.assertEquals(92, raw.getInt(0));
        }
    }

}
