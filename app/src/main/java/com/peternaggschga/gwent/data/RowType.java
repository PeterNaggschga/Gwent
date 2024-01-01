package com.peternaggschga.gwent.data;

public enum RowType {
    MELEE(0),
    RANGE(1),
    SIEGE(2);

    private final Integer index;

    RowType(final Integer index) {
        this.index = index;
    }

    public final Integer getIndex() {
        return index;
    }
}
