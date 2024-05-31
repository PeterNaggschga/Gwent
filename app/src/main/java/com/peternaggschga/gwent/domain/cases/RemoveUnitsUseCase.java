package com.peternaggschga.gwent.domain.cases;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;

import java.util.Collection;
import java.util.Collections;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableEmitter;

/**
 * A use case class responsible for removing units from a UnitRepository.
 * Capable of invoking a Dialog if a UnitEntity with the Ability#REVENGE ability is removed.
 * Should not be used directly by the UI layer.
 *
 * @see BurnDialogUseCase
 */
public class RemoveUnitsUseCase {
    /**
     * Removes the given UnitEntity objects from the given UnitRepository.
     * If a UnitEntity has the Ability#REVENGE ability,
     * a Dialog asking whether the ability should be used is shown.
     *
     * @param context    Context of the shown Dialog.
     * @param repository UnitRepository where the UnitEntity objects are removed.
     * @param units      Collection of UnitEntity objects that are removed.
     * @return A Completable tracking operation status.
     * @throws NullPointerException When units contains a null value.
     * @see #getRevengeDialog(Context, UnitRepository, CompletableEmitter, Collection, int)
     * @see UnitRepository#delete(Collection)
     */
    @NonNull
    public static Completable remove(@NonNull Context context, @NonNull UnitRepository repository,
                                     @NonNull Collection<UnitEntity> units) {
        long revengeUnits = units.stream()
                .filter(unit -> unit.getAbility() == Ability.REVENGE)
                .count();
        if (revengeUnits == 0) {
            return repository.delete(units);
        }
        return Completable.create(emitter ->
                getRevengeDialog(context, repository, emitter, units, (int) revengeUnits).show()
        );
    }

    /**
     * Removes the unit with the given id from the given UnitRepository.
     * If the unit has the Ability#REVENGE ability,
     * a Dialog asking whether the ability should be used is shown.
     * Wrapper of #remove(Context, UnitRepository, Collection).
     *
     * @param context    Context of the shown Dialog.
     * @param repository UnitRepository where the UnitEntity objects are removed.
     * @param id         Integer
     * @return A Completable tracking operation status.
     * @todo Add testing.
     * @see #remove(Context, UnitRepository, Collection)
     */
    public static Completable remove(@NonNull Context context, @NonNull UnitRepository repository,
                                     int id) {
        return repository.getUnit(id)
                .flatMapCompletable(unitEntity ->
                        remove(context, repository, Collections.singletonList(unitEntity)));
    }

    /**
     * Creates a Dialog asking whether the Ability#REVENGE ability should be activated.
     * The Dialog is created using RevengeDialogFactory.
     *
     * @param context      Context of the shown Dialog.
     * @param repository   UnitRepository where the UnitEntity objects are removed and avengers are inserted.
     * @param emitter      CompletableEmitter where CompletableEmitter#onComplete must be called,
     *                     when the user makes a decision.
     * @param units        Collection of UnitEntity objects that are removed.
     * @param revengeUnits Long representing the number of revenge units.
     * @return A Dialog asking whether the Ability#REVENGE ability should be activated.
     * @see RevengeDialogFactory#getRevengeDialog(Context, DialogInterface.OnClickListener, DialogInterface.OnClickListener)
     * @see RevengeDialogFactory#insertAvengers(UnitRepository, int)
     */
    @NonNull
    private static Dialog getRevengeDialog(@NonNull Context context, @NonNull UnitRepository repository,
                                           @NonNull CompletableEmitter emitter, @NonNull Collection<UnitEntity> units,
                                           @IntRange(from = 1) int revengeUnits) {
        return RevengeDialogFactory.getRevengeDialog(context,
                (dialog, which) -> {
                    // noinspection CheckResult, ResultOfMethodCallIgnored
                    repository.delete(units)
                            .andThen(RevengeDialogFactory.insertAvengers(repository, revengeUnits))
                            .subscribe(emitter::onComplete);
                },
                ((dialog, which) -> {
                    // noinspection CheckResult, ResultOfMethodCallIgnored
                    repository.delete(units).subscribe(emitter::onComplete);
                })
        );
    }
}
