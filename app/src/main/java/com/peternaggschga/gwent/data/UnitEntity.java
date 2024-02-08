package com.peternaggschga.gwent.data;

import static org.valid4j.Assertive.require;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.peternaggschga.gwent.Ability;
import com.peternaggschga.gwent.RowType;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;

/**
 * A class representing a card on the game board.
 * Is a persistent Entity and is therefore saved in a database table named `units`.
 */
@Entity(tableName = "units", foreignKeys = {
        @ForeignKey(entity = RowEntity.class,
                parentColumns = "id",
                childColumns = "row",
                onDelete = ForeignKey.CASCADE)})
@SuppressWarnings("unused")
public class UnitEntity {
    /**
     * Defines the primary key of the represented unit.
     * Is generated automatically on insert.
     */
    @PrimaryKey(autoGenerate = true)
    private int id;

    /**
     * Defines whether the represented card is epic.
     * Is set to `false` by default.
     */
    @ColumnInfo(defaultValue = "false")
    private boolean epic;

    /**
     * Defines the base-damage of the represented card.
     * Must be non-negative.
     */
    private int damage;

    /**
     * Defines the Ability of the represented card.
     * Is set to Ability#NONE by default.
     * Must not be `null`.
     */
    @ColumnInfo(defaultValue = "NONE")
    @NonNull
    private Ability ability;

    /**
     * Defines the squad the unit belongs to if #ability is Ability#BINDING.
     * If #ability is anything else, this value is `null`.
     */
    @ColumnInfo(defaultValue = "NULL")
    @IntRange(from = 0)
    @Nullable
    private Integer squad;

    /**
     * Defines the attack row the card lies in.
     * Must not be `null`.
     */
    @ColumnInfo(index = true)
    @NonNull
    private RowType row;

    /**
     * Constructor of a UnitEntity.
     * #id may not be set here since the value is generated automatically.
     *
     * @param epic    Boolean representing whether card is #epic.
     * @param damage  Non-negative value representing the #damage of the card.
     * @param ability Ability representing the #ability of the card.
     * @param squad   Integer representing the #squad of a card that has the Ability#BINDING #ability.
     * @param row     RowType representing the combat type of the card.
     * @throws org.valid4j.errors.RequireViolation When damage is less than zero or if ability is Ability#BINDING and squad is null or less than zero or if ability is not Ability#BINDING and squad is not null.
     */
    UnitEntity(boolean epic, @IntRange(from = 0) int damage, @NonNull Ability ability, @IntRange(from = 0) @Nullable Integer squad, @NonNull RowType row) {
        require(damage >= 0);
        require(ability == Ability.BINDING || squad == null);
        require(ability != Ability.BINDING || (squad != null && squad >= 0));
        this.epic = epic;
        this.damage = damage;
        this.ability = ability;
        this.squad = squad;
        this.row = row;
    }

    /**
     * Calculates the damage of this unit when (de-)buffed.
     * Returns #damage if #epic is true.
     * Otherwise, the damage is calculated through the given DamageCalculator,
     * which follows the visitor pattern.
     *
     * @param calculator DamageCalculator visitor used for damage calculation.
     * @return Integer representing the units (de-)buffed damage.
     * @see #getDamage()
     */
    public int calculateDamage(@NonNull DamageCalculator calculator) {
        return epic ? damage : calculator.calculateDamage(id, damage);
    }

    /**
     * Getter for #id.
     *
     * @return Integer representing the units' id.
     */
    public int getId() {
        return id;
    }

    /**
     * Setter for #id.
     * Only used by Room extension.
     *
     * @param id Integer representing the units' id.
     */
    void setId(int id) {
        this.id = id;
    }

    /**
     * Getter for #epic.
     * Only used by Room extension.
     *
     * @return Boolean representing whether the card is epic.
     */
    boolean isEpic() {
        return epic;
    }

    /**
     * Setter for #epic.
     * Only used by Room extension.
     *
     * @param epic Boolean representing whether the card is epic.
     */
    void setEpic(boolean epic) {
        this.epic = epic;
    }

    /**
     * Getter for #damage.
     * Only used by Room extension.
     *
     * @return Integer representing the card's base-damage.
     * @see #calculateDamage(DamageCalculator)
     */
    int getDamage() {
        return damage;
    }

    /**
     * Setter for #damage.
     * Only used by Room extension.
     *
     * @param damage Integer representing the card's base-damage.
     * @throws org.valid4j.errors.RequireViolation When damage is less than zero.
     */
    void setDamage(@IntRange(from = 0) int damage) {
        require(damage >= 0);
        this.damage = damage;
    }

    /**
     * Getter for #ability.
     *
     * @return Ability representing the units' ability.
     */
    @NonNull
    public Ability getAbility() {
        return ability;
    }

    /**
     * Setter for #ability.
     * Only used by Room extension.
     *
     * @param ability Ability representing the units' ability.
     */
    void setAbility(@NonNull Ability ability) {
        this.ability = ability;
    }

    /**
     * Getter for #squad.
     *
     * @return Integer representing the units' squad if #ability is Ability#BINDING or `null`.
     */
    @Nullable
    public Integer getSquad() {
        return squad;
    }

    /**
     * Setter for #squad.
     * Only used by Room extension.
     *
     * @param squad Integer representing the units' squad if #ability is Ability#BINDING or `null`.
     * @throws org.valid4j.errors.RequireViolation When #ability is Ability#BINDING and squad is null or less than zero or if #ability is not Ability#BINDING and squad is not null.
     */
    void setSquad(@IntRange(from = 0) @Nullable Integer squad) {
        require(ability == Ability.BINDING || squad == null);
        require(ability != Ability.BINDING || (squad != null && squad >= 0));
        this.squad = squad;
    }

    /**
     * Getter for #row.
     *
     * @return RowType representing the units combat row.
     */
    @NonNull
    public RowType getRow() {
        return row;
    }

    /**
     * Setter for #row.
     * Only used by Room extension.
     *
     * @param row RowType representing the units combat row.
     */
    void setRow(@NonNull RowType row) {
        this.row = row;
    }
}
