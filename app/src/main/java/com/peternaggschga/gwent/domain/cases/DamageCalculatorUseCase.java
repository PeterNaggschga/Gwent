package com.peternaggschga.gwent.domain.cases;

import android.util.Pair;

import androidx.annotation.NonNull;

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
                        getDamageCalculator(weatherHorn.first, weatherHorn.second, units));
    }

    /**
     * Creates a DamageCalculator for a row with the given weather status, horn status and units.
     * Basically calls DamageCalculatorBuildDirector#getCalculator with the given parameters.
     *
     * @param weather Boolean defining whether the calculation encompasses the weather debuff.
     * @param horn    Boolean defining whether a commander's horn is in the row.
     * @param units   Collection of UnitEntity objects that are in the row.
     * @return A DamageCalculator object capable of calculating damage for the given units.
     * @see DamageCalculatorBuildDirector#getCalculator(boolean, boolean, Collection)
     */
    @NonNull
    public static DamageCalculator getDamageCalculator(boolean weather, boolean horn, @NonNull Collection<UnitEntity> units) {
        return DamageCalculatorBuildDirector.getCalculator(weather, horn, units);
    }
}
