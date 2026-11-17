package com.peternaggschga.gwent.domain.cases;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;
import com.peternaggschga.gwent.domain.damage.DamageCalculatorBuildDirector;

import java.util.Collection;

import io.reactivex.rxjava3.core.Single;

/**
 * A use case class responsible for creating DamageCalculator objects for rows using DamageCalculatorBuildDirector.
 *
 * @see DamageCalculatorBuildDirector
 * @see DamageCalculator
 */
public class DamageCalculatorUseCase {
    /**
     * Returns a Single emitting a DamageCalculator object for the given row with properties retrieved from the given UnitRepository.
     *
     * @param repository UnitRepository used for data collection.
     * @param row        RowType defining which row is queried for DamageCalculator generation.
     * @return A Single emitting a DamageCalculator object for the given row.
     */
    @NonNull
    public static Single<DamageCalculator> getDamageCalculator(@NonNull UnitRepository repository, @NonNull RowType row) {
        return repository.isWeather(row)
                .zipWith(repository.isHorn(row), Pair::create)
                .zipWith(repository.getUnits(row), (weatherHorn, units) ->
                        getDamageCalculator(weatherHorn.first, weatherHorn.second, repository.isKingBran(), units));
    }

    /**
     * Builds a calculator using explicit row and leader state.
     *
     * @param weather  Whether weather is active.
     * @param horn     Whether the row has a commander's horn.
     * @param kingBran Whether King Bran's weather effect is active.
     * @param units    Units in the row.
     * @return A calculator applying the selected weather effect before other modifiers.
     */
    @NonNull
    public static DamageCalculator getDamageCalculator(boolean weather, boolean horn, boolean kingBran,
                                                       @NonNull Collection<UnitEntity> units) {
        return DamageCalculatorBuildDirector.getCalculator(weather, horn, kingBran, units);
    }
}
