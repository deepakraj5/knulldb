package io.github.deepakraj5.knulldb.storage;

import io.github.deepakraj5.knulldb.common.DbException;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class BufferPool {

    private final DiskManager disk;
    private final int capacity;

    private final LinkedHashMap<Integer, Page> pages = new LinkedHashMap<>(16, 0.75f, true);

    public BufferPool(DiskManager disk, int capacity) {
        if (capacity < 1) throw new DbException("Capacity must be at least 1");
        this.disk= disk;
        this.capacity = capacity;
    }

    public synchronized Page fetchPage(int pageId) {
        Page page = pages.get(pageId);
        if (page == null) {
            makeRoom();
            page = new Page(pageId);
            this.disk.readPage(pageId, page.getData());
            pages.put(pageId, page);
        }
        page.pinCount ++;
        return page;
    }

    public synchronized Page newPage() {
        this.makeRoom();
        int pageId = this.disk.allocatePage();
        Page page = new Page(pageId);
        pages.put(pageId, page);
        page.pinCount ++;

        return  page;
    }

    public synchronized void unpinPage(int pageId, boolean isDirty) {
        Page page = pages.get(pageId);

        if (page == null) {
            throw new DbException("Page: " + pageId + " not found");
        }
        if (page.pinCount == 0) {
            throw new DbException("Page: " + pageId + " not pinned");
        }

        page.pinCount --;
        if (isDirty) page.dirty = true;
    }

    public synchronized void flushPage(int pageId) {
        Page page = pages.get(pageId);
        if (page != null && page.dirty) writeBack(page);
    }

    public synchronized void flushAll() {
        for (Page page: pages.values()) {
            if (page.dirty) writeBack(page);
        }
    }

    public synchronized boolean contains(int pageId) {
        return pages.containsKey(pageId);
    }

    private void makeRoom() {
        if (pages.size() < capacity) return;

        Iterator<Map.Entry<Integer, Page>> it = pages.entrySet().iterator();
        while (it.hasNext()) {
            Page victim = it.next().getValue();
            if (victim.pinCount == 0) {
                if (victim.dirty) this.writeBack(victim);
                it.remove();
                return;
            }
        }

        throw new DbException("Buffer pool is full: all pages are pinned");
    }

    private void writeBack(Page page) {
        disk.writePage(page.getPageId(), page.getData());
        page.getData().rewind();
        page.dirty = false;
    }

}
