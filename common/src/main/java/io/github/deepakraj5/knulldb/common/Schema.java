package io.github.deepakraj5.knulldb.common;

import java.util.List;

public record Schema(List<Column> columns) {

    public Schema {
        if (columns.isEmpty()) throw new DbException("Schema needs at least 1 column");
        columns = List.copyOf(columns);
    }

    public static Schema of(Column... columns) {
        return new Schema(List.of(columns));
    }

}
