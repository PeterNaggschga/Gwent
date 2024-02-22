package com.peternaggschga.gwent.domain.cases;

import android.content.Context;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * A use case class responsible for the execution of the burn operation.
 */
public class BurnUnitsUseCase {
    /**
     * UnitRepository that is queried
     * when searching for the units to be burned and where the units are deleted.
     */
    @NonNull
    private final UnitRepository repository;

    /**
     * RemoveUnitUseCase used for removing the burned units from the #repository.
     */
    @NonNull
    private final RemoveUnitsUseCase removeUseCase;

    /**
     * List of UnitEntity objects that are to be burned.
     * Are lazily computed when #getBurnUnits() or #removeBurnUnits() is called.
     */
    private List<UnitEntity> burnUnits;

    /**
     * Constructor of a BurnUnitsUseCase.
     *
     * @param repository UnitRepository where units are searched and deleted.
     */
    public BurnUnitsUseCase(@NonNull Context context, @NonNull UnitRepository repository) {
        this.repository = repository;
        this.removeUseCase = new RemoveUnitsUseCase(context, repository);
    }

    /**
     * Returns the list of units that would be affected by a burn operation, i.e. #burnUnits.
     * Lazily calculates #burnUnits if this method hasn't been called already.
     *
     * @return A Single emitting the List of UnitEntity objects that would be affected by the operation.
     */
    @NonNull
    public Single<List<UnitEntity>> getBurnUnits() {
        if (burnUnits != null) {
            return Single.just(burnUnits);
        }
        return repository.getUnits()
                .concatMap(units -> {
                    if (units.isEmpty()) {
                        burnUnits = units;
                        return Single.just(burnUnits);
                    }

                    Single<Map<RowType, DamageCalculator>> calculators = Single.just(new HashMap<>());
                    for (RowType row : RowType.values()) {
                        calculators = calculators.zipWith(DamageCalculatorUseCase.getDamageCalculator(repository, row), (calculatorMap, damageCalculator) -> {
                            calculatorMap.put(row, damageCalculator);
                            return calculatorMap;
                        });
                    }
                    return calculators.flatMap(damageCalculators -> {
                        units.sort(Comparator.comparingInt(o -> (-o.calculateDamage(Objects.requireNonNull(damageCalculators.get(o.getRow()))))));
                        int maxDamage = units.get(0).calculateDamage(Objects.requireNonNull(damageCalculators.get(units.get(0).getRow())));

                        burnUnits = units.stream()
                                .filter(unitEntity -> unitEntity.calculateDamage(Objects.requireNonNull(damageCalculators.get(unitEntity.getRow()))) == maxDamage)
                                .collect(Collectors.toList());
                        return Single.just(burnUnits);
                    });
                });
    }

    /**
     * Deletes units in #burnUnits.
     * Calculates #burnUnits lazily beforehand by calling #getBurnUnits() if not yet done so.
     *
     * @return A Completable tracking operation status
     * @see #getBurnUnits()
     */
    @NonNull
    public Completable removeBurnUnits() {
        return Completable.create(emitter -> {
            // noinspection CheckResult, ResultOfMethodCallIgnored
            getBurnUnits().subscribe(units -> {
                // noinspection CheckResult, ResultOfMethodCallIgnored
                removeUseCase.remove(units).subscribe(emitter::onComplete);
            });
        });
    }
}
