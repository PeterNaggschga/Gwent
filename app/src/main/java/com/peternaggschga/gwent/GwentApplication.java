package com.peternaggschga.gwent;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.peternaggschga.gwent.data.AppDatabase;
import com.peternaggschga.gwent.data.UnitRepository;

import io.reactivex.rxjava3.core.Single;

/**
 * An {@link Application} encapsulating the Gwent app.
 * This class is responsible for maintaining a Singleton {@link UnitRepository} connected to the {@link AppDatabase} of this app.
 */
public class GwentApplication extends Application {
    /**
     * {@link UnitRepository} used for communication to {@link #database}.
     * Is lazily initialized in {@link #getRepository()} and provided as a Singleton.
     * @see #getRepository()
     * @see #getRepository(Context)
     */
    private static UnitRepository repository = null;
    /**
     * {@link AppDatabase} used by this app.
     * Is initialized in {@link #onCreate()}.
     *
     * @see #onCreate()
     */
    private AppDatabase database;

    /**
     * Returns a {@link UnitRepository} used by the {@link GwentApplication} referenced by the given {@link Context}.
     * Wrapper of {@link #getRepository()}.
     * @see #getRepository()
     * @param context {@link Context} of a {@link GwentApplication}.
     * @return A {@link UnitRepository} used by the given {@link GwentApplication} to access game state.
     */
    public static Single<UnitRepository> getRepository(@NonNull Context context) {
        return ((GwentApplication) context.getApplicationContext()).getRepository();
    }

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Initializes {@link #database}.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(this, AppDatabase.class, "database").build();
    }

    /**
     * Returns the {@link #repository} used to create, read, update and delete game state.
     * Lazily initializes {@link #repository}, if not yet done so.
     * @see #getRepository(Context)
     * @return A {@link UnitRepository} used to access game state.
     */
    public Single<UnitRepository> getRepository() {
        if (repository != null) {
            return Single.just(repository);
        }
        return UnitRepository.getRepository(database).doOnSuccess(unitRepository -> repository = unitRepository);
    }
}
