package io.github.deepakraj5.knulldb.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record Row(List<Object> values) {

    public Row {
        // List.copyOf would reject nulls, but SQL rows can have null
        values = Collections.unmodifiableList(new ArrayList<>(values));
    }

    public static Row of(Object... values) {
        return new Row(Arrays.asList(values));
    }

    public Object get(int index) {
        return values.get(index);
    }

}
