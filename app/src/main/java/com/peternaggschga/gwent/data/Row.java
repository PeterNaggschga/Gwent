package com.peternaggschga.gwent.data;

public enum Row {
    MELEE(0),
    RANGE(1),
    SIEGE(2);

    private final Integer index;

    Row(final Integer index) {
        this.index = index;
    }

    public final Integer getIndex() {
        return index;
    }
}
