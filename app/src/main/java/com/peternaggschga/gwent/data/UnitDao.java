package com.peternaggschga.gwent.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
interface UnitDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Completable insertUnit(@NonNull UnitEntity unit);

    @Query("INSERT INTO units (epic, damage, ability, squad, `row`) VALUES (:epic, :damage, :ability, :squad, :row)")
    Completable insertUnit(boolean epic, int damage, @NonNull Ability ability, @Nullable Integer squad, @NonNull RowType row);

    @Delete
    Completable deleteUnits(@NonNull List<UnitEntity> units);

    @Query("SELECT * FROM units WHERE `row` = :row")
    Single<List<UnitEntity>> getUnits(@NonNull RowType row);

    @Query("SELECT * FROM units")
    Single<List<UnitEntity>> getUnits();

    @Query("SELECT COUNT(*) FROM units WHERE `row` = :row")
    Single<Integer> countUnits(@NonNull RowType row);

    @Query("SELECT COUNT(*) FROM units")
    Single<Integer> countUnits();
}
