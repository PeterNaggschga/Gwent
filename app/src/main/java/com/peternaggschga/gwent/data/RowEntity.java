package com.peternaggschga.gwent.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rows")
public class RowEntity {
    @PrimaryKey
    @NonNull
    public Row id;

    @ColumnInfo(defaultValue = "false")
    public boolean weather;

    @ColumnInfo(defaultValue = "false")
    public boolean horn;

    public RowEntity(@NonNull Row id) {
        this.id = id;
    }
}
