package com.peternaggschga.gwent.domain.damage;

import static org.valid4j.Assertive.require;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import java.util.Map;
import java.util.Objects;

/**
 * A DamageCalculator class responsible for calculating the tight bond buff if necessary,
 * i.e., if there are units with the com.peternaggschga.gwent.Ability#BINDING ability.
 * Acts as a ConcreteDecorator in the implemented decorator pattern
 * and should decorate a WeatherDamageCalculator for correct calculation.
 */
class BondDamageCalculatorDecorator extends DamageCalculatorDecorator {
    /**
     * A Map
     * mapping the ids of all units with the com.peternaggschga.gwent.Ability#BINDING ability to the respective squad size.
     */
    private final Map<Integer, Integer> idToSquadSize;

    /**
     * Constructor of a BondDamageCalculatorDecorator.
     * Should only be called by DamageCalculatorBuilder.
     * The given damage calculator should be of type WeatherDamageCalculator for correct damage calculation.
     * The values of the given Map must contain non-``null`` or positive Integers.
     *
     * @param component     DamageCalculator that is being decorated by this decorator.
     * @param idToSquadSize Map mapping the ids of all units with the com.peternaggschga.gwent.Ability#BINDING ability to the respective squad size.
     * @throws org.valid4j.errors.RequireViolation When idToSquadSize contains negative or null values.
     * @see DamageCalculatorBuilder
     */
    BondDamageCalculatorDecorator(@NonNull DamageCalculator component, @NonNull Map<Integer, Integer> idToSquadSize) {
        super(component);
        require(idToSquadSize.values().stream().noneMatch(integer -> integer == null || integer <= 0));
        this.idToSquadSize = idToSquadSize;
    }

    /**
     * Calculates the (de-)buffed damage of unit with the given id and the given base-damage.
     * Returns the given damage times the number of units with the same squad
     * if the unit has the com.peternaggschga.gwent.Ability#BINDING ability.
     *
     * @param id     Integer representing the UnitEntity#id of the unit whose (de-)buff damage is calculated.
     * @param damage Integer representing the base-damage of the unit whose (de-)buff damage is calculated.
     * @return Integer representing the (de-)buffed damage of the unit.
     * @throws org.valid4j.errors.RequireViolation When damage is negative.
     */
    @Override
    public int calculateDamage(int id, @IntRange(from = 0) int damage) {
        require(damage >= 0);
        int componentDamage = component.calculateDamage(id, damage);
        return idToSquadSize.containsKey(id) ? componentDamage * Objects.requireNonNull(idToSquadSize.get(id)) : componentDamage;
    }
}
