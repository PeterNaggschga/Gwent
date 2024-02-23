package com.peternaggschga.gwent.domain.cases;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.peternaggschga.gwent.Ability;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;

import java.util.Collection;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableEmitter;

/**
 * A use case class responsible for removing units or resetting the UnitRepository.
 * Also, capable of invoking an AlertDialog if a UnitEntity with the Ability#REVENGE ability is removed.
 */
public class DeleteUnitsUseCase extends RemoveUnitsUseCase {

    public DeleteUnitsUseCase(@NonNull Context context, @NonNull UnitRepository repository) {
        super(repository, context);
    }

    /**
     * @param context
     * @param repository
     * @param units
     * @return
     * @throws NullPointerException When units contains a null value.
     */
    @NonNull
    public static Completable delete(@NonNull Context context, @NonNull UnitRepository repository, @NonNull Collection<UnitEntity> units) {
        long revengeUnits = units.stream()
                .filter(unit -> unit.getAbility() == Ability.REVENGE)
                .count();
        if (revengeUnits == 0) {
            return repository.delete(units);
        }
        return Completable.create(emitter ->
                getRevengeDialog(context, repository, emitter, units, revengeUnits).show()
        );
    }

    @NonNull
    private static Dialog getRevengeDialog(@NonNull Context context, @NonNull UnitRepository repository,
                                           @NonNull CompletableEmitter emitter, @NonNull Collection<UnitEntity> units,
                                           @IntRange(from = 1) long revengeUnits) {
        return getRevengeDialog(context,
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
        );
    }

    @NonNull
    public Completable delete(@NonNull Collection<UnitEntity> units) {
        return delete(context, repository, units);
    }
}
