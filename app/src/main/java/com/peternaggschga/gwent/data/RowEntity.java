package com.peternaggschga.gwent.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rows")
public class RowEntity {
    @PrimaryKey
    public byte id;

    @ColumnInfo(defaultValue = "false")
    public boolean weather;

    @ColumnInfo(defaultValue = "false")
    public boolean horn;
}
