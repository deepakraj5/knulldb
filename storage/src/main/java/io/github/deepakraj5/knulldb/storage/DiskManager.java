package io.github.deepakraj5.knulldb.storage;

import io.github.deepakraj5.knulldb.common.DbException;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;

public class DiskManager implements AutoCloseable {

    public static final int PAGE_SIZE = 4096;

    private final FileChannel channel;

    public DiskManager(Path path) {
        try {
            this.channel = FileChannel.open(path, CREATE, READ, WRITE);
        } catch (Exception exception) {
            throw new DbException("Cannot open database file, path: " + path, exception);
        }
    }

    public void readPage(int pageId, ByteBuffer buffer) {
        this.checkBuffer(buffer);
        buffer.clear();
        long start = (long) pageId * PAGE_SIZE;

        try {
            while (buffer.hasRemaining()) {
                int n = this.channel.read(buffer, start + buffer.position());
                if (n < 0) throw new DbException("Page " + pageId + " does not exist");
            }
        } catch (Exception exception) {
            throw new DbException("Failed to read page", exception);
        }
        buffer.rewind();
    }

    public void writePage(int pageId, ByteBuffer buffer) {
        this.checkBuffer(buffer);
        buffer.rewind();
        long start = (long) pageId * PAGE_SIZE;

        try {
            while (buffer.hasRemaining()) {
                this.channel.write(buffer, start + buffer.position());
            }
        } catch (Exception exception) {
            throw new DbException("Failed to write page", exception);
        }
    }

    public synchronized int allocatePage() {
        int pageId = this.numPages();
        this.writePage(pageId, ByteBuffer.allocate(PAGE_SIZE));
        return pageId;
    }

    @Override
    public void close() {
        try {
            this.channel.close();
        } catch (Exception exception) {
            throw new DbException("Failed to close database file", exception);
        }
    }

    public int numPages() {
        try {
            return (int) (this.channel.size() / PAGE_SIZE);
        } catch (Exception exception) {
            throw new DbException("Failed to get file size", exception);
        }
    }

    private void checkBuffer(ByteBuffer buffer) {
        if(buffer.capacity() != PAGE_SIZE) {
            throw new DbException("Buffer must be exactly " + PAGE_SIZE + " bytes");
        }
    }

}
