package com.peternaggschga.gwent.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    @Nullable
    private Integer squad;

    @ColumnInfo(index = true)
    @NonNull
    private RowType row;

    public UnitEntity(boolean epic, int damage, @NonNull Ability ability, @Nullable Integer squad, @NonNull RowType row) {
        this.epic = epic;
        assert damage >= 0;
        this.damage = damage;
        this.ability = ability;
        assert squad == null || (ability == Ability.BINDING && squad >= 0);
        this.squad = squad;
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

    @Nullable
    public Integer getSquad() {
        return squad;
    }

    public void setSquad(@Nullable Integer squad) {
        assert squad == null || (ability == Ability.BINDING && squad >= 0);
        this.squad = squad;
    }

    @NonNull
    public RowType getRow() {
        return row;
    }

    public void setRow(@NonNull RowType row) {
        this.row = row;
    }
}
