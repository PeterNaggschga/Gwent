package com.peternaggschga.gwent.data;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
interface RowDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Completable insertRow(@NonNull RowEntity row);

    @Query("DELETE FROM rows")
    Completable clearRows();

    @Query("UPDATE rows SET weather = NOT weather WHERE id = :row")
    Completable updateWeather(@NonNull RowType row);

    @Query("UPDATE rows SET weather = 0")
    Completable clearWeather();

    @Query("UPDATE rows SET horn = NOT horn WHERE id = :row")
    Completable updateHorn(@NonNull RowType row);

    @Query("SELECT weather FROM rows WHERE id = :row")
    Single<Boolean> isWeather(@NonNull RowType row);

    @Query("SELECT horn FROM rows WHERE id = :row")
    Single<Boolean> isHorn(@NonNull RowType row);
}
