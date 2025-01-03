package com.peternaggschga.gwent.data;

import static com.peternaggschga.gwent.domain.damage.DamageCalculator.Color;
import static com.peternaggschga.gwent.domain.damage.DamageCalculator.Color.DEFAULT;

import android.content.Context;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
     * Integer array containing every damage value an epic unit may have.
     */
    @NonNull
    public static final Integer[] EPIC_DAMAGE_VALUES = new Integer[]{0, 7, 8, 10, 11, 15};

    /**
     * Integer defining what the maximum damage of a non-epic UnitEntity can be.
     */
    public static final int NON_EPIC_DAMAGE_VALUES_UPPER_BOUND = 20;

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
    @IntRange(from = 1)
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
     * @throws IllegalArgumentException When damage is less than zero or if ability is Ability#BINDING and squad is null or less than one or if ability is not Ability#BINDING and squad is not null.
     */
    UnitEntity(boolean epic, @IntRange(from = 0) int damage, @NonNull Ability ability, @IntRange(from = 1) @Nullable Integer squad, @NonNull RowType row) {
        if (damage < 0) {
            throw new IllegalArgumentException("Damage must be greater or equal to 0 but is " + damage + ".");
        }
        if (ability != Ability.BINDING && squad != null) {
            throw new IllegalArgumentException("Squad must be null or ability must be BINDING but squad is " + squad + " and ability is " + ability + ".");
        }
        if (ability == Ability.BINDING && (squad == null || squad < 1)) {
            throw new IllegalArgumentException("Squad must not be null or less than one but squad is " + squad + ".");
        }
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
     * @return An Integer representing the units (de-)buffed damage.
     * @see #getDamage()
     */
    public int calculateDamage(@NonNull DamageCalculator calculator) {
        return epic ? damage : calculator.calculateDamage(id, damage);
    }

    /**
     * Returns whether this unit is (de-)buffed.
     * Returns Color#DEFAULT if #epic is true.
     * Otherwise, whether the unit is (de-)buffed is calculated through the given DamageCalculator,
     * which follows the visitor pattern.
     *
     * @param calculator DamageCalculator visitor used for damage calculation.
     * @return A Color representing the units (de-)buffed damage.
     * @see #getDamage()
     */
    @NonNull
    public Color isBuffed(@NonNull DamageCalculator calculator) {
        return epic ? DEFAULT : calculator.isBuffed(id);
    }

    /**
     * Creates a String containing the descriptions of all units in the given collection,
     * separated by commas.
     * If n units have the same description, instead of printing the same description n times,
     * "n×" is printed in front of the description.
     * Unit descriptions are created using #toString(Context).
     * Ordering of units in the given Collection is not guaranteed to be kept.
     *
     * @param context Context used to acquire String resources.
     * @param units   Collection of UnitEntity objects that should be in the created String.
     * @return A String containing the description of all units.
     * @throws IllegalArgumentException When units collection is empty.
     * @see #toString(Context)
     */
    @NonNull
    public static String collectionToString(@NonNull Context context, @NonNull Collection<UnitEntity> units) {
        if (units.isEmpty()) {
            throw new IllegalArgumentException("Units collection must not be empty.");
        }

        Map<String, Integer> descriptionStrings = new HashMap<>(units.size());
        for (UnitEntity unit : units) {
            String description = unit.toString(context);
            descriptionStrings.merge(description, 1, Integer::sum);
        }

        List<String> descriptions = new ArrayList<>(descriptionStrings.size());
        if (descriptionStrings.size() < units.size()) {
            descriptionStrings.forEach((key, value) ->
                    descriptions.add(context.getString(R.string.unit_toString_multiplicity, value, key)));
        } else {
            descriptions.addAll(descriptionStrings.keySet());
        }

        Iterator<String> descriptionIterator = descriptions.iterator();
        String[] result = {descriptionIterator.next()};
        if (descriptionIterator.hasNext()) {
            result[0] = context.getString(R.string.unit_collection_toString_accumulation_word,
                            descriptionIterator.next(),
                            result[0])
                    .trim();
        }
        descriptionIterator.forEachRemaining(description ->
                result[0] = context.getString(R.string.unit_collection_toString_accumulation_symbol,
                                description,
                                result[0])
                        .trim());
        return result[0];
    }

    /**
     * Returns a string representation of this unit.
     * The representation contains information on each field of this class,
     * i.e., #row, #epic, #damage, #ability, and #squad.
     *
     * @param context Context used to acquire String resources.
     * @return A string representing the unit.
     */
    @NonNull
    public String toString(@NonNull Context context) {
        String row;
        switch (getRow()) {
            case RANGE:
                row = context.getString(R.string.unit_toString_range);
                break;
            case SIEGE:
                row = context.getString(R.string.unit_toString_siege);
                break;
            case MELEE:
            default:
                row = context.getString(R.string.unit_toString_melee);
        }
        String epic = isEpic() ? context.getString(R.string.unit_toString_epic) : context.getString(R.string.unit_toString_unit);
        String ability;
        String squad = "";
        switch (getAbility()) {
            case HORN:
                ability = context.getString(R.string.add_picker_ability_horn);
                break;
            case BINDING:
                ability = context.getString(R.string.add_picker_ability_binding);
                squad = context.getString(R.string.unit_toString_squad, getSquad());
                break;
            case MORAL_BOOST:
                ability = context.getString(R.string.add_picker_ability_moralBoost);
                break;
            case REVENGE:
                ability = context.getString(R.string.add_picker_ability_revenge);
                break;
            case NONE:
            default:
                ability = context.getString(R.string.unit_toString_ability_none);
        }
        return context.getString(R.string.unit_toString, row, epic, getDamage(), ability, squad).trim();
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
     *
     * @return Boolean representing whether the card is epic.
     */
    public boolean isEpic() {
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
     *
     * @return Integer representing the card's base-damage.
     * @see #calculateDamage(DamageCalculator)
     */
    public int getDamage() {
        return damage;
    }

    /**
     * Setter for #damage.
     * Only used by Room extension.
     *
     * @param damage Integer representing the card's base-damage.
     * @throws IllegalArgumentException When damage is less than zero.
     */
    void setDamage(@IntRange(from = 0) int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("Damage must not be less than zero but is " + damage + ".");
        }
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
     * @throws IllegalArgumentException When #ability is Ability#BINDING and squad is null or less than 1 or if #ability is not Ability#BINDING and squad is not null.
     */
    void setSquad(@IntRange(from = 1) @Nullable Integer squad) {
        if (ability != Ability.BINDING && squad != null) {
            throw new IllegalArgumentException("Squad must be null or ability must be BINDING but squad is " + squad + " and ability is " + ability + ".");
        }
        if (ability == Ability.BINDING && (squad == null || squad < 1)) {
            throw new IllegalArgumentException("Squad must not be null or less than one but squad is " + squad + ".");
        }
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
