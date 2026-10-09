package io.github.deepakraj5.knulldb.common;

public enum DataType {

    INT(Integer.class, 4),
    BIGINT(Integer.class, 8),
    DOUBLE(Double.class, 8),
    BOOLEAN(Boolean.class, 1),
    VARCHAR(String.class, -1);

    private final Class<?> javaType;
    private final int fixedSize;

    DataType(Class<?> javaType, int fixedSize) {
        this.javaType = javaType;
        this.fixedSize = fixedSize;
    }

    public Class<?> javaType() {
        return this.javaType;
    }

    public int fixedSize() {
        return this.fixedSize;
    }

}
