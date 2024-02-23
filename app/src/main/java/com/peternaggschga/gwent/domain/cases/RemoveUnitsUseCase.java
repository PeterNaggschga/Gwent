package com.peternaggschga.gwent.domain.cases;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.peternaggschga.gwent.Ability;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;

import java.util.Collection;

import io.reactivex.rxjava3.core.Completable;

public class RemoveUnitsUseCase {
    private static final boolean AVENGER_EPIC = false;
    private static final Ability AVENGER_ABILITY = Ability.NONE;
    private static final int AVENGER_DAMAGE = 8;
    private static final Integer AVENGER_SQUAD = null;
    private static final RowType AVENGER_ROW = RowType.MELEE;

    @NonNull
    private final UnitRepository repository;

    @NonNull
    private final Context context;

    public RemoveUnitsUseCase(@NonNull Context context, @NonNull UnitRepository repository) {
        this.context = context;
        this.repository = repository;
    }

    /**
     * @param context
     * @param repository
     * @param units
     * @return
     * @throws NullPointerException When units contains a null value.
     */
    @NonNull
    public static Completable remove(@NonNull Context context, @NonNull UnitRepository repository, @NonNull Collection<UnitEntity> units) {
        long revengeUnits = units.stream()
                .filter(unit -> unit.getAbility() == Ability.REVENGE)
                .count();
        if (revengeUnits == 0) {
            return repository.delete(units);
        }
        return Completable.create(emitter -> getRevengeDialog(context,
                (dialog, which) -> {
                    // noinspection CheckResult, ResultOfMethodCallIgnored
                    repository.delete(units)
                            .andThen(repository.insertUnit(AVENGER_EPIC, AVENGER_DAMAGE, AVENGER_ABILITY, AVENGER_SQUAD, AVENGER_ROW, revengeUnits))
                            .subscribe(emitter::onComplete);
                },
                ((dialog, which) -> {
                    // noinspection CheckResult, ResultOfMethodCallIgnored
                    repository.delete(units).subscribe(emitter::onComplete);
                })
        ).show());
    }

    @NonNull
    public static Completable reset(@NonNull Context context, @NonNull UnitRepository repository, @Nullable UnitEntity keptUnit) {
        return repository.getUnits()
                .flatMapCompletable(units -> {
                    long revengeUnits = units.stream()
                            .filter(unit -> unit.getAbility() == Ability.REVENGE)
                            .count() - (keptUnit != null && keptUnit.getAbility() == Ability.REVENGE ? 1 : 0);
                    if (revengeUnits == 0) {
                        return repository.reset(keptUnit);
                    }
                    return Completable.create(emitter -> getRevengeDialog(context,
                            (dialogInterface, which) -> {
                                // noinspection CheckResult, ResultOfMethodCallIgnored
                                repository.reset(keptUnit)
                                        .andThen(repository.insertUnit(AVENGER_EPIC, AVENGER_DAMAGE, AVENGER_ABILITY, AVENGER_SQUAD, AVENGER_ROW, revengeUnits))
                                        .subscribe(emitter::onComplete);
                            },
                            ((dialog, which) -> {
                                // noinspection CheckResult, ResultOfMethodCallIgnored
                                repository.reset(keptUnit).subscribe(emitter::onComplete);
                            })
                    ).show());
                });
    }

    @NonNull
    private static Dialog getRevengeDialog(@NonNull Context context, @NonNull DialogInterface.OnClickListener onPositiveClickListener,
                                           @NonNull DialogInterface.OnClickListener onNegativeClickListener) {
        return new AlertDialog.Builder(context)
                .setTitle(R.string.alertDialog_revenge_title)
                .setMessage(R.string.alertDialog_revenge_msg)
                .setCancelable(false)
                .setPositiveButton(R.string.alertDialog_revenge_positive, onPositiveClickListener)
                .setNegativeButton(R.string.alertDialog_revenge_negative, onNegativeClickListener)
                .create();
    }

    @NonNull
    public Completable remove(@NonNull Collection<UnitEntity> units) {
        return remove(context, repository, units);
    }

    @NonNull
    public Completable reset() {
        return reset(null);
    }

    @NonNull
    public Completable reset(@Nullable UnitEntity keptUnit) {
        return reset(context, repository, keptUnit);
    }
}
