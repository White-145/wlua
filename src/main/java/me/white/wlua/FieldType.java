package me.white.wlua;

public enum FieldType {
    REGULAR(true, true),
    READ_ONLY(true, false),
    WRITE_ONLY(false, true);

    final boolean canRead;
    final boolean canWrite;

    FieldType(boolean canRead, boolean canWrite) {
        this.canRead = canRead;
        this.canWrite = canWrite;
    }
}
