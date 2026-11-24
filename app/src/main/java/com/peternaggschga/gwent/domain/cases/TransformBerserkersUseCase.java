package com.peternaggschga.gwent.domain.cases;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowBuffState;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Permanently transforms Berserkers through UnitRepository and informs the user after persistence.
 * Invoked by the repository, not by UI callbacks.
 */
public final class TransformBerserkersUseCase {
    /**
     * Prevents instantiation of this utility class.
     */
    private TransformBerserkersUseCase() {
    }

    /**
     * Transforms all eligible Berserkers in a row and saves them in one batch.
     * Does not check Mardroeme: the repository calls this only after activating it.
     *
     * @param context    Application context used for the localized notification.
     * @param repository Repository used to read and update units.
     * @param row        Row whose Berserkers are transformed.
     * @return A Completable tracking the update and its single notification.
     */
    @NonNull
    public static Completable transformRow(@NonNull Context context, @NonNull UnitRepository repository,
                                           @NonNull RowType row) {
        return repository.getUnits(row).flatMapCompletable(units -> {
            List<UnitEntity> transformed = units.stream().filter(unit -> unit.getAbility() == Ability.BERSERKER)
                    .map(TransformBerserkersUseCase::transform).collect(Collectors.toList());
            if (transformed.isEmpty()) {
                return Completable.complete();
            }
            return repository.updateUnits(transformed).andThen(showToast(context, transformed.size()));
        });
    }

    /**
     * Retrieves newly inserted units by ID and transforms Berserkers affected by Mardroeme.
     * Newly added Mardroeme units affect all Berserkers in their row; newly added Berserkers
     * transform if their row has the Mardroeme buff or a Mardroeme unit.
     * Updates eligible units in one batch and notifies once without changing row buffs.
     *
     * @param context    Application context used for the localized notification.
     * @param repository Repository used to retrieve the units, read buffs, and update units.
     * @param ids        Successfully inserted unit IDs. Missing IDs are ignored.
     * @return A Completable tracking any required update and its notification.
     */
    @NonNull
    public static Completable transformAddedUnits(@NonNull Context context, @NonNull UnitRepository repository,
                                                  @NonNull List<Long> ids) {
        if (ids.isEmpty()) {
            return Completable.complete();
        }
        return repository.getUnitsByIds(ids).flatMapCompletable(units -> Flowable.fromIterable(units)
                .filter(unit -> unit.getAbility() == Ability.BERSERKER || unit.getAbility() == Ability.MARDROEME)
                .groupBy(UnitEntity::getRow)
                .flatMapSingle(group -> group.toList().flatMap(added -> {
                    if (added.stream().anyMatch(unit -> unit.getAbility() == Ability.MARDROEME)) {
                        return repository.getUnits(group.getKey());
                    }
                    return repository.getBuffState(group.getKey()).flatMap(state -> state == RowBuffState.MARDROEME
                            ? Single.just(added)
                            : repository.getUnits(group.getKey()).map(rowUnits -> rowUnits.stream()
                            .anyMatch(unit -> unit.getAbility() == Ability.MARDROEME)
                            ? added : Collections.<UnitEntity>emptyList()));
                }).map(eligible -> eligible.stream().filter(unit -> unit.getAbility() == Ability.BERSERKER)
                        .map(TransformBerserkersUseCase::transform).collect(Collectors.toList())))
                .collect(ArrayList<UnitEntity>::new, List::addAll)
                .flatMapCompletable(transformed -> transformed.isEmpty() ? Completable.complete()
                        : repository.updateUnits(transformed).andThen(showToast(context, transformed.size()))));
    }

    /**
     * Creates a transformed copy of a Berserker, retaining its ID, row, and epic status.
     * Does not modify the original unit.
     *
     * @param unit Berserker whose transformed stats are selected according to its combat row.
     * @return A copy with transformed damage, ability, and squad values.
     */
    @NonNull
    private static UnitEntity transform(@NonNull UnitEntity unit) {
        return unit.getRow() == RowType.MELEE ? unit.withStats(14, Ability.MORAL_BOOST, null)
                : unit.withStats(8, Ability.BINDING, UnitEntity.BERSERKER_SQUAD);
    }

    /**
     * Shows one localized Toast after a successful batch; zero transformations produce no Toast.
     *
     * @param context Application context used to resolve resources and display the message.
     * @param count   Number of units transformed by the completed operation.
     * @return A Completable scheduling the notification on the main thread.
     */
    @NonNull
    private static Completable showToast(@NonNull Context context, int count) {
        if (count == 0) {
            return Completable.complete();
        }
        return Completable.fromAction(() -> Toast.makeText(context, context.getResources().getQuantityString(
                        R.plurals.berserkers_transformed, count, count), Toast.LENGTH_SHORT).show())
                .subscribeOn(AndroidSchedulers.mainThread());
    }
}
