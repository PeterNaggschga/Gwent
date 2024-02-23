package com.peternaggschga.gwent.domain.cases;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.peternaggschga.gwent.Ability;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableEmitter;

/**
 * A use case class responsible for removing units or resetting the UnitRepository.
 * Also, capable of invoking an AlertDialog if a UnitEntity with the Ability#REVENGE ability is removed.
 */
public class ResetRepositoryUseCase {
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
                            getRevengeDialog(context, repository, emitter, keptUnit, revengeUnits).show()
                    );
                });
    }

    @NonNull
    private static Dialog getRevengeDialog(@NonNull Context context, @NonNull UnitRepository repository,
                                           @NonNull CompletableEmitter emitter, @Nullable UnitEntity keptUnit,
                                           @IntRange(from = 1) long revengeUnits) {
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

    @NonNull
    public static Completable reset(@NonNull Context context, @NonNull UnitRepository repository) {
        return reset(context, repository, null);
    }
}
