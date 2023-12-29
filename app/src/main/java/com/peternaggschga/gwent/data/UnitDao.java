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

    @Query("SELECT * FROM units WHERE id = :rowId")
    UnitEntity getUnit(int rowId);

    @Query("SELECT * FROM units WHERE `row` = :rowId")
    List<UnitEntity> getUnits(byte rowId);

    @Query("SELECT * FROM units")
    List<UnitEntity> getUnits();
}
