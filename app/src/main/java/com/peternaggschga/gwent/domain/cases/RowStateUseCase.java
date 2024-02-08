package com.peternaggschga.gwent.domain.cases;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;
import com.peternaggschga.gwent.ui.main.RowUiState;

import java.util.Collection;

import io.reactivex.rxjava3.core.Single;

/**
 * A use case class responsible for creating RowUiState objects.
 * Is used as a factory for RowUiState.
 *
 * @see RowUiState
 */
public class RowStateUseCase {
    /**
     * Returns a Single emitting an up-to-date RowUiState object for the given row
     * retrieved from the given UnitRepository.
     * @param repository UnitRepository used for data collection.
     * @param row RowType defining which row is queried for status generation.
     * @return A Single emitting an up-to-date RowUiState for the given row.
     */
    @NonNull
    public static Single<RowUiState> getRowState(@NonNull UnitRepository repository, @NonNull RowType row) {
        return Single.fromCallable(() -> {
            boolean weather = repository.isWeather(row).blockingGet();
            boolean horn = repository.isHorn(row).blockingGet();
            Collection<UnitEntity> units = repository.getUnits(row).blockingGet();
            DamageCalculator calculator = DamageCalculatorUseCase.getDamageCalculator(weather, horn, units);
            int damage = units.stream()
                    .map(unit -> unit.calculateDamage(calculator))
                    .reduce(0, Integer::sum);
            return new RowUiState(damage, weather, horn, units.size());
        });
    }
}
