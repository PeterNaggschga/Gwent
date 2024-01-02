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
    private int id;

    @ColumnInfo(defaultValue = "false")
    private boolean epic;

    private int damage;

    @ColumnInfo(defaultValue = "NONE")
    @NonNull
    private Ability ability;

    @ColumnInfo(index = true)
    @NonNull
    private RowType row;

    public UnitEntity(boolean epic, int damage, @NonNull Ability ability, @NonNull RowType row) {
        this.epic = epic;
        assert damage >= 0;
        this.damage = damage;
        this.ability = ability;
        this.row = row;
    }

    public int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    public boolean isEpic() {
        return epic;
    }

    public void setEpic(boolean epic) {
        this.epic = epic;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        assert damage >= 0;
        this.damage = damage;
    }

    @NonNull
    public Ability getAbility() {
        return ability;
    }

    public void setAbility(@NonNull Ability ability) {
        this.ability = ability;
    }

    @NonNull
    public RowType getRow() {
        return row;
    }

    public void setRow(@NonNull RowType row) {
        this.row = row;
    }
}
