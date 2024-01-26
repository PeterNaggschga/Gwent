package com.peternaggschga.gwent.domain.damage;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.UnitEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A build director responsible for the creation of DamageCalculator instances from sets of units using DamageCalculatorBuilder.
 * Encapsulates the logic for correct order of decorators.
 *
 * @see DamageCalculatorBuilder
 */
public class DamageCalculatorBuildDirector {
    /**
     * Creates a DamageCalculator for a row with the given weather status, horn status and units.
     *
     * @param weather Boolean defining whether the calculation encompasses the weather debuff.
     * @param horn    Boolean defining whether a commander's horn is in the row.
     * @param units   Collection of UnitEntity objects that are in the row.
     * @return A DamageCalculator object capable of calculating damage for the given units.
     */
    @NonNull
    public static DamageCalculator getCalculator(boolean weather, boolean horn, @NonNull Collection<UnitEntity> units) {
        DamageCalculatorBuilder builder = new DamageCalculatorBuilder();
        builder.setWeather(weather);

        if (units.stream().anyMatch(unit -> unit.getAbility() == Ability.BINDING)) {
            setSquads(units, builder);
        }

        if (units.stream().anyMatch(unit -> unit.getAbility() == Ability.MORAL_BOOST)) {
            setMoralBoosts(units, builder);
        }

        if (horn || units.stream().anyMatch(unit -> unit.getAbility() == Ability.HORN)) {
            setHorns(horn, units, builder);
        }

        return builder.getResult();
    }

    /**
     * Uses the given units
     * to add a BondDamageCalculatorDecorator to the builder
     * using DamageCalculatorBuilder#setBond().
     * To accomplish this,
     * the given unit list is converted to a Map from ids of units with the Ability.BINDING Ability to the respective squad size.
     *
     * @param units   Collection of UnitEntity objects that are in the row.
     * @param builder DamageCalculatorBuilder where the horn buff is added.
     * @see DamageCalculatorBuilder#setBond(Map)
     */
    private static void setSquads(@NonNull Collection<UnitEntity> units, @NonNull DamageCalculatorBuilder builder) {
        Optional<Map<Integer, List<Integer>>> mapOptional = units.stream()
                .filter(unit -> unit.getAbility() == Ability.BINDING) // filter for binding units only
                .map(unit -> {
                    // create a map from squad to list of id for each unit
                    Map<Integer, List<Integer>> result = new HashMap<>();
                    result.put(unit.getSquad(), Collections.singletonList(unit.getId()));
                    return result;
                }).reduce((integerListMap, integerListMap2) -> {
                    // merge maps
                    for (Map.Entry<Integer, List<Integer>> entry : integerListMap2.entrySet()) {
                        integerListMap.merge(entry.getKey(), entry.getValue(), (integers, integers2) -> {
                            integers.addAll(integers2);
                            return integers;
                        });
                    }
                    return integerListMap;
                });
        assert mapOptional.isPresent();
        // create a new map from mapOptional which maps Unit ids to the respective squad size
        Map<Integer, Integer> idToSquadSize = new HashMap<>();
        for (List<Integer> list : mapOptional.get().values()) {
            list.forEach(integer -> idToSquadSize.put(integer, list.size()));
        }
        builder.setBond(idToSquadSize);
    }

    /**
     * Uses the given units
     * to add a MoralDamageCalculatorDecorator to the builder using DamageCalculatorBuilder#setMoral().
     * To accomplish this,
     * the given unit list is converted to a List of the ids of the units with the Ability.MORAL_BOOST Ability.
     *
     * @param units   Collection of UnitEntity objects that are in the row.
     * @param builder DamageCalculatorBuilder where the horn buff is added.
     * @see DamageCalculatorBuilder#setMoral(List)
     */
    private static void setMoralBoosts(@NonNull Collection<UnitEntity> units, @NonNull DamageCalculatorBuilder builder) {
        List<Integer> unitIds = units.stream()
                .filter(unit -> unit.getAbility() == Ability.MORAL_BOOST)
                .map(UnitEntity::getId)
                .collect(Collectors.toList());
        builder.setMoral(unitIds);
    }

    /**
     * Uses the given commander's horn status and the given units
     * to add a HornDamageCalculatorDecorator to the builder
     * using DamageCalculatorBuilder#setHorn().
     * To accomplish this,
     * the given unit list is converted to a List of the ids of the units with the Ability.HORN Ability
     * (including ``null`` if horn is ``true``).
     *
     * @param horn    Boolean defining whether a commander's horn is in the row.
     * @param units   Collection of UnitEntity objects that are in the row.
     * @param builder DamageCalculatorBuilder where the horn buff is added.
     * @see DamageCalculatorBuilder#setHorn(List)
     */
    private static void setHorns(boolean horn, @NonNull Collection<UnitEntity> units, @NonNull DamageCalculatorBuilder builder) {
        List<Integer> unitIds = units.stream()
                .filter(unit -> unit.getAbility() == Ability.HORN)
                .map(UnitEntity::getId)
                .collect(Collectors.toList());
        if (horn) {
            unitIds.add(null);
        }
        builder.setHorn(unitIds);
    }
}
