package com.peternaggschga.gwent.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "units", foreignKeys = {
        @ForeignKey(entity = RowEntity.class,
                parentColumns = "id",
                childColumns = "row",
                onDelete = ForeignKey.CASCADE)})
public class UnitEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(defaultValue = "false")
    public boolean epic;

    public int damage;

    public Ability ability;

    public byte row;
}
