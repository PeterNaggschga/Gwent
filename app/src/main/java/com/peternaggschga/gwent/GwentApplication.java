package com.peternaggschga.gwent;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.peternaggschga.gwent.data.AppDatabase;
import com.peternaggschga.gwent.data.UnitRepository;

import io.reactivex.rxjava3.core.Single;

/**
 * @todo Documentation
 */
public class GwentApplication extends Application {
    private AppDatabase database;
    private static UnitRepository repository = null;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database").build();
    }

    public Single<UnitRepository> getRepository() {
        if (repository != null) {
            return Single.just(repository);
        }
        return UnitRepository.getRepository(database).doOnSuccess(unitRepository -> repository = unitRepository);
    }

    public static Single<UnitRepository> getRepository(@NonNull Context context) {
        return ((GwentApplication) context.getApplicationContext()).getRepository();
    }
}
