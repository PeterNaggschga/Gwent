package com.peternaggschga.gwent.domain.damage;

import static com.peternaggschga.gwent.domain.damage.DamageCalculator.Color.BUFFED;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import java.util.Map;
import java.util.Objects;

/**
 * A DamageCalculator class responsible for calculating the tight bond buff if necessary,
 * i.e., if there are units with the com.peternaggschga.gwent.data.Ability#BINDING ability.
 * Acts as a ConcreteDecorator in the implemented decorator pattern
 * and should decorate a WeatherDamageCalculator for correct calculation.
 */
class BondDamageCalculatorDecorator extends DamageCalculatorDecorator {
    /**
     * A Map
     * mapping the ids of all units with the com.peternaggschga.gwent.data.Ability#BINDING ability to the respective squad size.
     */
    private final Map<Integer, Integer> idToSquadSize;

    /**
     * Constructor of a BondDamageCalculatorDecorator.
     * Should only be called by DamageCalculatorBuilder.
     * The given damage calculator should be of type WeatherDamageCalculator for correct damage calculation.
     * The values of the given Map must contain non-``null`` or positive Integers.
     *
     * @param component     DamageCalculator that is being decorated by this decorator.
     * @param idToSquadSize Map mapping the ids of all units with the com.peternaggschga.gwent.data.Ability#BINDING ability to the respective squad size.
     * @throws IllegalArgumentException When idToSquadSize contains non-positive or null values.
     * @see DamageCalculatorBuilder
     */
    BondDamageCalculatorDecorator(@NonNull DamageCalculator component, @NonNull Map<Integer, Integer> idToSquadSize) {
        super(component);
        if (idToSquadSize.values().stream().anyMatch(integer -> integer == null || integer < 1)) {
            throw new IllegalArgumentException("Map idToSquadSize must not contain non-positive or null values.");
        }
        this.idToSquadSize = idToSquadSize;
    }

    /**
     * Calculates the (de-)buffed damage of unit with the given id and the given base-damage.
     * Returns the given damage times the number of units with the same squad
     * if the unit has the com.peternaggschga.gwent.data.Ability#BINDING ability.
     *
     * @param id     Integer representing the UnitEntity#id of the unit whose (de-)buff damage is calculated.
     * @param damage Integer representing the base-damage of the unit whose (de-)buff damage is calculated.
     * @return Integer representing the (de-)buffed damage of the unit.
     * @throws IllegalArgumentException When damage is negative.
     */
    @Override
    public int calculateDamage(int id, @IntRange(from = 0) int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("Damage must be greater or equal to 0.");
        }
        return Objects.requireNonNull(idToSquadSize.getOrDefault(id, 1)) * component.calculateDamage(id, damage);
    }

    /**
     * Calculates whether the unit with the given id is shown as Color#BUFFED,
     * Color#DEBUFFED, or Color#DEFAULT.
     * Units are shown as Color#BUFFED when they are in a squad of two or more units,
     * otherwise their Color is defined by #component.
     *
     * @param id Integer representing the UnitEntity#id of the unit buff status is calculated.
     * @return Color representing whether the unit is buffed, de-buffed or not affected.
     * @see Color
     */
    @Override
    public Color isBuffed(int id) {
        return (Objects.requireNonNull(idToSquadSize.getOrDefault(id, 0)) > 1)
                ? BUFFED
                : component.isBuffed(id);
    }
}
