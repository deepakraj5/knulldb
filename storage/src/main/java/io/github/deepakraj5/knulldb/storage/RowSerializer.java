package io.github.deepakraj5.knulldb.storage;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.github.deepakraj5.knulldb.common.Column;
import io.github.deepakraj5.knulldb.common.DataType;
import io.github.deepakraj5.knulldb.common.DbException;
import io.github.deepakraj5.knulldb.common.Row;
import io.github.deepakraj5.knulldb.common.Schema;

public final class RowSerializer {
    
    private RowSerializer() {
    }

    public static byte[] serialize(Schema schema, Row row) {
        List<Column> columns = schema.columns();
        int columnSize = columns.size();

        if (row.values().size() != columnSize) {
            throw new DbException("Expected " + columnSize + " but got" + row.values().size());
        }

        int bitmapBytes = (columnSize + 7) / 8;
        int size = bitmapBytes;
        byte[][] strings = new byte[columnSize][];

        for (int i = 0 ; i < columnSize; i ++) {
            Object value = row.get(i);
            if (value == null) continue;

            DataType type = columns.get(i).type();
            if (!type.javaType().isInstance(value)) {
                throw new DbException("Column " + columns.get(i).name() + " expects " + type 
                        + " but got " + value.getClass().getSimpleName());
            }

            if (type == DataType.VARCHAR) {
                strings[i] = ((String) value).getBytes(StandardCharsets.UTF_8);
                if (strings[i].length > 0XFFFF) throw new DbException("VARCHAR too long");
                size += 2 + strings[i].length;
            } else {
                size += type.fixedSize();
            }
        }

        ByteBuffer buffer = ByteBuffer.allocate(size);
        byte[] bitmap = new byte[bitmapBytes];
        buffer.position(bitmapBytes);

        for (int i = 0; i < columnSize; i ++) {
            Object value = row.get(i);
            if (value == null) {
                bitmap[i / 8] |= (byte) (1 << (i % 8));
                continue;
            }

            switch (columns.get(i).type()) {
                case INT -> buffer.putInt((Integer) value);
                case BIGINT -> buffer.putLong((Long) value);
                case DOUBLE -> buffer.putDouble((Double) value);
                case BOOLEAN -> buffer.put((byte) ((Boolean) value ? 1 : 0));
                case VARCHAR -> {
                    buffer.putShort((short) strings[i].length);
                    buffer.put(strings[i]);
                }
            }
        }

        buffer.put(0, bitmap);

        return buffer.array();
    }

    public static Row deserialize(Schema schema, byte[] data) {
        List<Column> columns = schema.columns();
        int columnSize = columns.size();

        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position((columnSize + 7) / 8);

        Object[] values = new Object[columnSize];
        for (int i = 0; i < columnSize; i ++) {

            boolean isNull = (data[i / 8] & (1 << (i % 8))) != 0;
            if (isNull) continue;

            values[i] = switch (columns.get(i).type()) {
                case INT -> buffer.getInt();
                case BIGINT -> buffer.getLong();
                case DOUBLE -> buffer.getDouble();
                case BOOLEAN -> buffer.get() != 0;
                case VARCHAR -> {
                    int len = buffer.getShort() & 0XFFFF;
                    byte[] bytes = new byte[len];
                    buffer.get(bytes);
                    yield new String(bytes, StandardCharsets.UTF_8);
                }
            };
        }

        return Row.of(values);
    }

}
