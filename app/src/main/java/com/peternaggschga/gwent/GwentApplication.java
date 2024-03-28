package com.peternaggschga.gwent;

import android.app.Application;

import androidx.room.Room;

import com.peternaggschga.gwent.data.AppDatabase;
import com.peternaggschga.gwent.data.UnitRepository;

/**
 * @todo Documentation
 */
public class GwentApplication extends Application {
    private AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database").build();
    }

    public UnitRepository getRepository() {
        return new UnitRepository(database);
    }
}
