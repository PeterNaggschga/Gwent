package com.peternaggschga.gwent.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
interface RowDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Completable insertRow(RowEntity row);

    @Query("DELETE FROM rows")
    Completable clearRows();

    @Query("UPDATE rows SET weather = NOT weather WHERE id = :row")
    Completable updateWeather(RowType row);

    @Query("UPDATE rows SET horn = NOT horn WHERE id = :row")
    Completable updateHorn(RowType row);

    @Query("SELECT * FROM rows WHERE id = :row")
    Single<RowEntity> getRow(RowType row);

    @Query("SELECT * FROM rows")
    Single<RowEntity[]> getRows();
}
