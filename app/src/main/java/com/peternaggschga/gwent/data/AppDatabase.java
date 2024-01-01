package com.peternaggschga.gwent.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {UnitEntity.class, RowEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public static final byte ID_MELEE_ROW = (byte) Row.MELEE.ordinal();
    public static final byte ID_RANGE_ROW = (byte) Row.RANGE.ordinal();
    public static final byte ID_SIEGE_ROW = (byte) Row.SIEGE.ordinal();

    public abstract UnitDao units();

    public abstract RowDao rows();
}
