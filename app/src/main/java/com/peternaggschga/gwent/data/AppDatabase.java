package com.peternaggschga.gwent.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {UnitEntity.class, RowEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UnitDao units();

    public abstract RowDao rows();
}
