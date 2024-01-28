package com.peternaggschga.gwent.domain;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;
import com.peternaggschga.gwent.domain.damage.DamageCalculatorBuildDirector;

import java.util.Collection;

import io.reactivex.rxjava3.core.Single;

public class DamageCalculatorUseCase {
    @NonNull
    public static Single<DamageCalculator> getDamageCalculator(@NonNull UnitRepository repository, @NonNull RowType row) {
        return Single.fromCallable(() -> {
            boolean weather = repository.isWeather(row).blockingGet();
            boolean horn = repository.isHorn(row).blockingGet();
            Collection<UnitEntity> units = repository.getUnits(row).blockingGet();
            return DamageCalculatorBuildDirector.getCalculator(weather, horn, units);
        });
    }
}
