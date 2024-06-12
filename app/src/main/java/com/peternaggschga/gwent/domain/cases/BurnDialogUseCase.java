package com.peternaggschga.gwent.domain.cases;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.core.Single;

/**
 * A use case class responsible for dispatching a remove call to RemoveUnitsUseCase.
 */
public class BurnDialogUseCase {
    /**
     * Returns the list of units that would be affected by a burn operation.
     * The returned list may be empty.
     *
     * @param repository UnitRepository where units are fetched.
     * @return A Single emitting the List of UnitEntity objects that would be affected by the operation.
     */
    @NonNull
    private static Single<List<UnitEntity>> getBurnUnits(@NonNull UnitRepository repository) {
        return repository.getUnits()
                .flatMap(units -> {
                    if (units.isEmpty()) {
                        return Single.just(units);
                    }

                    Single<Map<RowType, DamageCalculator>> calculators = Single.just(new HashMap<>(RowType.values().length));
                    for (RowType row : RowType.values()) {
                        calculators = calculators.zipWith(DamageCalculatorUseCase.getDamageCalculator(repository, row), (calculatorMap, damageCalculator) -> {
                            calculatorMap.put(row, damageCalculator);
                            return calculatorMap;
                        });
                    }

                    return calculators.map(damageCalculators -> {
                        List<UnitEntity> maxDamageUnits = new ArrayList<>(units.size());
                        final int[] maxDamage = {0};
                        units.stream()
                                .filter(unit -> !unit.isEpic())
                                .forEach(unit -> {
                                    int damage = unit.calculateDamage(Objects.requireNonNull(damageCalculators.get(unit.getRow())));
                                    if (damage > maxDamage[0]) {
                                        maxDamage[0] = damage;
                                        maxDamageUnits.clear();
                                        maxDamageUnits.add(unit);
                                    } else if (damage == maxDamage[0]) {
                                        maxDamageUnits.add(unit);
                                    }
                                });
                        return maxDamageUnits;
                    });
                });
    }

    /**
     * Burns the strongest UnitEntity objects in UnitRepository.
     * Invokes a Dialog asking whether the user really wants to remove those units.
     * ResetRepositoryUseCase is used for resetting.
     * Wrapper for #burn(Context, UnitRepository).
     *
     * @param context Context where a Dialog can be inflated.
     * @return A Single emitting a Boolean defining whether the units really were burned.
     * @see #burn(Context, UnitRepository)
     * @see RemoveUnitsUseCase#remove(Context, UnitRepository, Collection)
     */
    @NonNull
    public static Single<Boolean> burn(@NonNull Context context) {
        return GwentApplication.getRepository(context).flatMap(repository -> burn(context, repository));
    }

    /**
     * Burns the strongest UnitEntity objects in UnitRepository.
     * Invokes a Dialog asking whether the user really wants to remove those units.
     * ResetRepositoryUseCase is used for resetting.
     *
     * @param context    Context where a Dialog can be inflated.
     * @param repository UnitRepository where units are burned.
     * @return A Single emitting a Boolean defining whether the units really were burned.
     * @see RemoveUnitsUseCase#remove(Context, UnitRepository, Collection)
     */
    @NonNull
    protected static Single<Boolean> burn(@NonNull Context context, @NonNull UnitRepository repository) {
        return getBurnUnits(repository).flatMap(units -> {
            if (units.isEmpty()) {
                return Single.just(false);
            }
            return Single.create(emitter -> new AlertDialog.Builder(context)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setTitle(R.string.alertDialog_burn_title)
                    .setMessage(context.getString(R.string.alertDialog_burn_msg, UnitEntity.collectionToString(context, units)))
                    .setNegativeButton(R.string.alertDialog_burn_negative, (dialog, which) -> dialog.cancel())
                    .setPositiveButton(R.string.alertDialog_burn_positive, (dialog, which) -> {
                        // noinspection CheckResult, ResultOfMethodCallIgnored
                        RemoveUnitsUseCase.remove(context, repository, units).subscribe(() -> emitter.onSuccess(true));
                    })
                    .setCancelable(true)
                    .setOnCancelListener(dialog -> emitter.onSuccess(false))
                    .create()
                    .show());
        });
    }
}
