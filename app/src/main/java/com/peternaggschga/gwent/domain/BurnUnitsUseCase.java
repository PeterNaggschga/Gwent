package com.peternaggschga.gwent.domain;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.RowType;
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

public class BurnUnitsUseCase {
    @NonNull
    private final UnitRepository repository;
    private List<UnitEntity> burnUnits;

    public BurnUnitsUseCase(@NonNull UnitRepository repository) {
        this.repository = repository;
    }

    @NonNull
    public Single<List<UnitEntity>> getBurnUnits() {
        if (burnUnits != null) {
            return Single.just(burnUnits);
        }
        return Single.fromCallable(() -> {
            List<UnitEntity> units = repository.getUnits().blockingGet();
            if (units.isEmpty()) {
                burnUnits = units;
                return burnUnits;
            }

            Map<RowType, DamageCalculator> damageCalculators = new HashMap<>();
            for (RowType row : RowType.values()) {
                damageCalculators.put(row, DamageCalculatorUseCase.getDamageCalculator(repository, row).blockingGet());
            }

            units.sort(Comparator.comparingInt(o -> (-o.calculateDamage(Objects.requireNonNull(damageCalculators.get(o.getRow()))))));
            int maxDamage = units.get(0).calculateDamage(Objects.requireNonNull(damageCalculators.get(units.get(0).getRow())));

            burnUnits = units.stream()
                    .filter(unitEntity -> unitEntity.calculateDamage(Objects.requireNonNull(damageCalculators.get(unitEntity.getRow()))) == maxDamage)
                    .collect(Collectors.toList());
            return burnUnits;
        });
    }

    @NonNull
    public Completable removeBurnUnits() {
        return Completable.fromSingle(getBurnUnits())
                .andThen(repository.delete(burnUnits));
    }
}
