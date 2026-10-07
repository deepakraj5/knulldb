package io.github.deepakraj5.knulldb.storage;

import java.nio.ByteBuffer;

public class Page {

    private final int pageId;
    private final ByteBuffer data = ByteBuffer.allocate(DiskManager.PAGE_SIZE);

    int pinCount;
    boolean dirty;

    Page(int pageId) {
        this.pageId = pageId;
    }

    public int getPageId() {
        return this.pageId;
    }

    public ByteBuffer getData() {
        return this.data;
    }

}
