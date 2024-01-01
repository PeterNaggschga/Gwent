package com.peternaggschga.gwent.data;

import static com.peternaggschga.gwent.data.AppDatabase.ID_MELEE_ROW;
import static com.peternaggschga.gwent.data.AppDatabase.ID_RANGE_ROW;
import static com.peternaggschga.gwent.data.AppDatabase.ID_SIEGE_ROW;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface RowDao {
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
