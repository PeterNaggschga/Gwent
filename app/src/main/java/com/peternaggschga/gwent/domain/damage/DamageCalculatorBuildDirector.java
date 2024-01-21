package com.peternaggschga.gwent.domain.damage;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.UnitEntity;

import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DamageCalculatorBuildDirector {
    public static DamageCalculator getCalculator(boolean weather, boolean horn, @NonNull Collection<UnitEntity> units) {
        DamageCalculatorBuilder builder = new DamageCalculatorBuilder();
        builder.setWeather(weather);

        if (units.stream().anyMatch(getAbilityPredicate(Ability.BINDING))) {
            setSquads(units, builder);
        }

        if (units.stream().anyMatch(getAbilityPredicate(Ability.MORAL_BOOST))) {
            setMoralBoosts(units, builder);
        }

        if (horn || units.stream().anyMatch(getAbilityPredicate(Ability.HORN))) {
            setHorns(horn, units, builder);
        }

        return builder.getResult();
    }

    private static void setSquads(@NonNull Collection<UnitEntity> units, @NonNull DamageCalculatorBuilder builder) {
        Optional<Map<Integer, List<Integer>>> mapOptional = units.stream()
                .filter(getAbilityPredicate(Ability.BINDING)) // filter for binding units only
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

    private static void setMoralBoosts(@NonNull Collection<UnitEntity> units, @NonNull DamageCalculatorBuilder builder) {
        if (units.stream().noneMatch(getAbilityPredicate(Ability.MORAL_BOOST))) {
            return;
        }
        List<Integer> unitIds = units.stream()
                .filter(getAbilityPredicate(Ability.MORAL_BOOST))
                .map(UnitEntity::getId).collect(Collectors.toList());
        builder.setMoral(unitIds);
    }

    private static void setHorns(boolean horn, @NonNull Collection<UnitEntity> units, @NonNull DamageCalculatorBuilder builder) {
        List<Integer> unitIds = units.stream()
                .filter(getAbilityPredicate(Ability.HORN))
                .map(UnitEntity::getId)
                .collect(Collectors.toList());
        if (horn) {
            unitIds.add(null);
        }
        builder.setHorn(unitIds);
    }

    @NonNull
    @Contract(pure = true)
    private static Predicate<UnitEntity> getAbilityPredicate(Ability ability) {
        return unit -> unit.getAbility() == ability;
    }
}
