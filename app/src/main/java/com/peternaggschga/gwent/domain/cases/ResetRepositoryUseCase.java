package com.peternaggschga.gwent.domain.cases;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.ui.sounds.SoundManager;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableEmitter;
import io.reactivex.rxjava3.core.Maybe;

/**
 * A use case class responsible for resetting the UnitRepository.
 * Capable of invoking a Dialog if a UnitEntity with the Ability#REVENGE ability is removed.
 * Should not be used directly by the UI layer.
 *
 * @see ResetDialogUseCase
 */
class ResetRepositoryUseCase {
    /**
     * Resets the given UnitRepository and keeps one random unit if keepUnit is true.
     * If a removed UnitEntity has the Ability#REVENGE ability,
     * a Dialog asking whether the ability should be used is shown.
     *
     * @param context    Context of the shown Dialog.
     * @param repository UnitRepository that is being reset.
     * @param keepUnit   Boolean defining whether a single UnitEntity should be kept.
     * @param soundManager SoundManager used, if an UnitEntity has the Ability#REVENGE ability.
     * @return A Maybe emitting the kept UnitEntity or nothing if keepUnit is false.
     * @see #getRevengeDialog(Context, UnitRepository, CompletableEmitter, UnitEntity, int, SoundManager)
     * @see UnitRepository#reset(UnitEntity)
     */
    @NonNull
    static Maybe<UnitEntity> reset(@NonNull Context context, @NonNull UnitRepository repository,
                                   boolean keepUnit, @NonNull SoundManager soundManager) {
        return repository.getUnits()
                .flatMapMaybe(units -> {
                    Optional<UnitEntity> keptUnit = keepUnit ? getRandomUnit(units) : Optional.empty();
                    long revengeUnits = units.stream()
                            .filter(unit -> unit.getAbility() == Ability.REVENGE)
                            .count() - (keptUnit.isPresent() && keptUnit.get().getAbility() == Ability.REVENGE ? 1 : 0);
                    Completable resultAction = (revengeUnits == 0) ?
                            repository.reset(keptUnit.orElse(null)) :
                            Completable.create(emitter ->
                                    getRevengeDialog(context, repository, emitter, keptUnit.orElse(null), (int) revengeUnits, soundManager).show()
                            );
                    return resultAction.andThen(Maybe.fromOptional(keptUnit));
                });
    }

    /**
     * Selects a random unit that is not epic.
     * If all units are epic or if there are no units at all, an empty Optional is returned.
     *
     * @param units List of UnitEntity objects, one of which is selected.
     * @return An Optional containing the selected unit or nothing if no unit could be selected.
     */
    @NonNull
    private static Optional<UnitEntity> getRandomUnit(@NonNull List<UnitEntity> units) {
        units = units.stream()
                .filter(unit -> !unit.isEpic())
                .collect(Collectors.toList());
        return units.isEmpty() ? Optional.empty() : Optional.of(units.get(new Random().nextInt(units.size())));
    }

    /**
     * Creates a Dialog asking whether the Ability#REVENGE ability should be activated.
     * The Dialog is created using an RevengeAlertDialogBuilderAdapter.
     *
     * @param context      Context of the shown Dialog.
     * @param repository   UnitRepository where the UnitEntity objects are removed and avengers are inserted.
     * @param emitter      CompletableEmitter where CompletableEmitter#onComplete must be called,
     *                     when the user makes a decision.
     * @param keptUnit     UnitEntity that should be kept.
     * @param revengeUnits Long representing the number of revenge units.
     * @param soundManager SoundManager used, if an Avenger is added.
     * @return A Dialog asking whether the Ability#REVENGE ability should be activated.
     * @see RevengeAlertDialogBuilderAdapter#insertAvengers(UnitRepository, int, SoundManager)
     */
    @NonNull
    private static Dialog getRevengeDialog(@NonNull Context context, @NonNull UnitRepository repository,
                                           @NonNull CompletableEmitter emitter, @Nullable UnitEntity keptUnit,
                                           @IntRange(from = 1) int revengeUnits, @NonNull SoundManager soundManager) {
        return new RevengeAlertDialogBuilderAdapter(context)
                .setPositiveCallback((dialogInterface, which) -> {
                    // noinspection CheckResult, ResultOfMethodCallIgnored
                    repository.reset(keptUnit)
                            .andThen(RevengeAlertDialogBuilderAdapter.insertAvengers(repository, revengeUnits, soundManager))
                            .subscribe(emitter::onComplete);
                })
                .setNegativeCallback(((dialog, which) -> {
                    // noinspection CheckResult, ResultOfMethodCallIgnored
                    repository.reset(keptUnit).subscribe(emitter::onComplete);
                }))
                .create();
    }

    /**
     * Resets the given UnitRepository.
     * If a removed UnitEntity has the Ability#REVENGE ability,
     * a Dialog asking whether the ability should be used is shown.
     * Wrapper of #reset(Context, UnitRepository, boolean, SoundManager).
     *
     * @param context    Context of the shown Dialog.
     * @param repository UnitRepository that is being reset.
     * @param soundManager SoundManager used, if an UnitEntity has the Ability#REVENGE ability.
     * @return A Completable tracking operation status.
     * @see #reset(Context, UnitRepository, boolean, SoundManager)
     */
    @NonNull
    public static Completable reset(@NonNull Context context, @NonNull UnitRepository repository, @NonNull SoundManager soundManager) {
        return Completable.fromMaybe(reset(context, repository, false, soundManager));
    }
}
