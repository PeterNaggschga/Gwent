package com.peternaggschga.gwent.data;

import androidx.annotation.NonNull;
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

    @NonNull
    public Ability ability;

    @ColumnInfo(index = true)
    @NonNull
    public Row row;

    public UnitEntity(boolean epic, int damage, @NonNull Ability ability, @NonNull Row row) {
        this.epic = epic;
        this.damage = damage;
        this.ability = ability;
        this.row = row;
    }
}
