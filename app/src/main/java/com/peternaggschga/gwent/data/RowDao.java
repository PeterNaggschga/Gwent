package com.peternaggschga.gwent.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface RowDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertRow(RowEntity row);

    @Query("DELETE FROM rows")
    void clearRows();

    @Query("UPDATE rows SET weather = NOT weather WHERE id = :row")
    void updateWeather(RowType row);

    @Query("UPDATE rows SET horn = NOT horn WHERE id = :row")
    void updateHorn(RowType row);

    @Query("SELECT * FROM rows WHERE id = :row")
    RowEntity getRow(RowType row);

    @Query("SELECT * FROM rows")
    RowEntity[] getRows();
}
