package com.peternaggschga.gwent.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {UnitEntity.class, RowEntity.class}, version = 1)
abstract class AppDatabase extends RoomDatabase {
    abstract UnitDao units();

    abstract RowDao rows();
}
