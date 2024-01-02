package com.peternaggschga.gwent.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class UnitRepository {
    @NonNull
    private final AppDatabase database;

    public UnitRepository(@NonNull AppDatabase database) {
        this.database = database;
        initializeRows();
    }

    private Completable initializeRows() {
        Completable result = Completable.complete();
        for (RowType row :
                RowType.values()) {
            result = result.andThen(database.rows().insertRow(new RowEntity(row)));
        }
        return result;
    }

    public Completable reset() {
        return reset(null);
    }

    public Completable reset(@Nullable UnitEntity keptUnit) {
        Completable result = database.rows().clearRows().andThen(initializeRows());
        if (keptUnit != null) {
            result = result.andThen(insertUnit(keptUnit));
        }
        return result;
    }

    private Completable insertUnit(@NonNull UnitEntity unit) {
        return database.units().insertUnit(unit);
    }

    public Completable switchWeather(@NonNull RowType row) {
        return database.rows().updateWeather(row);
    }

    public Completable switchHorn(@NonNull RowType row) {
        return database.rows().updateHorn(row);
    }

    public Completable clearWeather() {
        return database.rows().clearWeather();
    }

    public Completable delete(@NonNull List<UnitEntity> units) {
        return database.units().deleteUnits(units);
    }

    public Completable copy(@NonNull List<UnitEntity> units) {
        Completable result = Completable.complete();
        for (UnitEntity unit : units) {
            result = result.andThen(insertUnit(unit.isEpic(), unit.getDamage(), unit.getAbility(), unit.getSquad(), unit.getRow()));
        }
        return result;
    }

    public Completable insertUnit(boolean epic, int damage, @NonNull Ability ability, @Nullable Integer squad, @NonNull RowType row) {
        assert damage >= 0;
        assert squad == null || (ability == Ability.BINDING && squad >= 0);
        return database.units().insertUnit(epic, damage, ability, squad, row);
    }

    public Completable insertUnit(boolean epic, int damage, @NonNull Ability ability, @Nullable Integer squad, @NonNull RowType row, int number) {
        Completable result = Completable.complete();
        for (int i = 0; i < number; i++) {
            result = result.andThen(insertUnit(epic, damage, ability, squad, row));
        }
        return result;
    }

    public Single<Integer> countUnits(@NonNull RowType row) {
        return database.units().countUnits(row);
    }

    public Single<Integer> countUnits() {
        return database.units().countUnits();
    }

    public Single<Boolean> isWeather(@NonNull RowType row) {
        return database.rows().isWeather(row);
    }

    public Single<Boolean> isHorn(@NonNull RowType row) {
        return database.rows().isHorn(row);
    }

    public Single<List<UnitEntity>> getUnits(@NonNull RowType row) {
        return database.units().getUnits(row);
    }

    public Single<List<UnitEntity>> getUnits() {
        return database.units().getUnits();
    }
}
