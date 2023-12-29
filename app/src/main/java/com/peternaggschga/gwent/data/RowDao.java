package com.peternaggschga.gwent.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface RowDao {
    byte ID_MELEE_ROW = 0;
    byte ID_RANGE_ROW = 1;
    byte ID_SIEGE_ROW = 2;

    @Insert
    void insertRow(RowEntity row);

    @Query("DELETE FROM rows")
    void clearRows();

    @Query("INSERT INTO rows (id) VALUES (" + ID_MELEE_ROW + "), (" + ID_RANGE_ROW + "), (" + ID_SIEGE_ROW + ")")
    void initializeEmptyRows();

    @Query("UPDATE rows SET weather = NOT weather WHERE id = :rowId")
    void updateWeather(byte rowId);

    @Query("UPDATE rows SET horn = NOT horn WHERE id = :rowId")
    void updateHorn(byte rowId);

    @Query("SELECT * FROM rows WHERE id = :rowId")
    RowEntity getRow(byte rowId);

    @Query("SELECT * FROM rows")
    RowEntity[] getRows();
}
