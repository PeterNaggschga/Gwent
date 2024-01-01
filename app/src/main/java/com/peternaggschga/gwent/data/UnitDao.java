package com.peternaggschga.gwent.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UnitDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertUnit(UnitEntity unit);

    @Delete
    void deleteUnit(UnitEntity unit);

    @Query("SELECT * FROM units WHERE id = :id")
    UnitEntity getUnit(int id);

    @Query("SELECT * FROM units WHERE `row` = :row")
    List<UnitEntity> getUnits(RowType row);

    @Query("SELECT * FROM units")
    List<UnitEntity> getUnits();
}
