package com.peternaggschga.gwent.data;

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
    Completable insertUnit(UnitEntity unit);

    @Delete
    Completable deleteUnit(UnitEntity unit);

    @Query("SELECT * FROM units WHERE id = :id")
    Single<UnitEntity> getUnit(int id);

    @Query("SELECT * FROM units WHERE `row` = :row")
    Single<List<UnitEntity>> getUnits(RowType row);

    @Query("SELECT * FROM units")
    Single<List<UnitEntity>> getUnits();
}
