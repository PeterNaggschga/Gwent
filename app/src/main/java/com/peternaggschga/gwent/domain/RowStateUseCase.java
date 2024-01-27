package com.peternaggschga.gwent.domain;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;
import com.peternaggschga.gwent.domain.damage.DamageCalculatorBuildDirector;
import com.peternaggschga.gwent.ui.RowUiState;

import java.util.Collection;
import java.util.function.Function;

import io.reactivex.rxjava3.core.Single;

public class RowStateUseCase {
    @NonNull
    public static Single<RowUiState> getRowState(@NonNull UnitRepository repository, @NonNull RowType row) {
        return Single.fromCallable(() -> {
            boolean weather = repository.isWeather(row).blockingGet();
            boolean horn = repository.isHorn(row).blockingGet();
            Collection<UnitEntity> units = repository.getUnits(row).blockingGet();
            DamageCalculator calculator = DamageCalculatorBuildDirector.getCalculator(weather, horn, units);
            int damage = units.stream()
                    .map((Function<UnitEntity, Integer>) unit -> unit.calculateDamage(calculator))
                    .reduce(0, Integer::sum);
            return new RowUiState(damage, weather, horn, units.size());
        });
    }
}
