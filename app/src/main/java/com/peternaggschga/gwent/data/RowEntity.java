package com.peternaggschga.gwent.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rows")
public class RowEntity {
    @PrimaryKey
    @NonNull
    private final RowType id;

    @ColumnInfo(defaultValue = "false")
    private boolean weather;

    @ColumnInfo(defaultValue = "false")
    private boolean horn;

    public RowEntity(@NonNull RowType id) {
        this.id = id;
    }

    @NonNull
    public RowType getId() {
        return id;
    }

    public boolean isWeather() {
        return weather;
    }

    public void setWeather(boolean weather) {
        this.weather = weather;
    }

    public boolean isHorn() {
        return horn;
    }

    public void setHorn(boolean horn) {
        this.horn = horn;
    }
}
