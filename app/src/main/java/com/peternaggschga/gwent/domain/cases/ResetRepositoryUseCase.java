package com.peternaggschga.gwent.domain.cases;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.peternaggschga.gwent.Ability;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableEmitter;

/**
 * A use case class responsible for resetting the UnitRepository.
 * Capable of invoking a Dialog if a UnitEntity with the Ability#REVENGE ability is removed.
 */
public class ResetRepositoryUseCase {
    /**
     * Resets the given UnitRepository and keeps the given UnitEntity (if not null).
     * If a removed UnitEntity has the Ability#REVENGE ability,
     * a Dialog asking whether the ability should be used is shown.
     *
     * @param context    Context of the shown Dialog.
     * @param repository UnitRepository that is being reset.
     * @param keptUnit   UnitEntity that should be kept.
     * @return A Completable tracking operation status.
     * @see #getRevengeDialog(Context, UnitRepository, CompletableEmitter, UnitEntity, int)
     * @see UnitRepository#reset(UnitEntity)
     */
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
                    return Completable.create(emitter ->
                            getRevengeDialog(context, repository, emitter, keptUnit, (int) revengeUnits).show()
                    );
                });
    }

    /**
     * Creates a Dialog asking whether the Ability#REVENGE ability should be activated.
     * The Dialog is created using RevengeDialogFactory.
     *
     * @param context      Context of the shown Dialog.
     * @param repository   UnitRepository where the UnitEntity objects are removed and avengers are inserted.
     * @param emitter      CompletableEmitter where CompletableEmitter#onComplete must be called,
     *                     when the user makes a decision.
     * @param keptUnit     UnitEntity that should be kept.
     * @param revengeUnits Long representing the number of revenge units.
     * @return A Dialog asking whether the Ability#REVENGE ability should be activated.
     * @see RevengeDialogFactory#getRevengeDialog(Context, DialogInterface.OnClickListener, DialogInterface.OnClickListener)
     * @see RevengeDialogFactory#insertAvengers(UnitRepository, int)
     */
    @NonNull
    private static Dialog getRevengeDialog(@NonNull Context context, @NonNull UnitRepository repository,
                                           @NonNull CompletableEmitter emitter, @Nullable UnitEntity keptUnit,
                                           @IntRange(from = 1) int revengeUnits) {
        return RevengeDialogFactory.getRevengeDialog(context,
                (dialogInterface, which) -> {
                    // noinspection CheckResult, ResultOfMethodCallIgnored
                    repository.reset(keptUnit)
                            .andThen(RevengeDialogFactory.insertAvengers(repository, revengeUnits))
                            .subscribe(emitter::onComplete);
                },
                ((dialog, which) -> {
                    // noinspection CheckResult, ResultOfMethodCallIgnored
                    repository.reset(keptUnit).subscribe(emitter::onComplete);
                })
        );
    }

    /**
     * Resets the given UnitRepository.
     * If a removed UnitEntity has the Ability#REVENGE ability,
     * a Dialog asking whether the ability should be used is shown.
     * Wrapper of #reset(Context, UnitRepository, UnitEntity).
     *
     * @param context    Context of the shown Dialog.
     * @param repository UnitRepository that is being reset.
     * @return A Completable tracking operation status.
     * @see #reset(Context, UnitRepository, UnitEntity)
     */
    @NonNull
    public static Completable reset(@NonNull Context context, @NonNull UnitRepository repository) {
        return reset(context, repository, null);
    }
}
