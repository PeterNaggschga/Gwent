package com.peternaggschga.gwent;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room3.Room;

import com.peternaggschga.gwent.data.AppDatabase;
import com.peternaggschga.gwent.data.UnitRepository;

import io.reactivex.rxjava3.core.Single;

/**
 * An Application encapsulating the Gwent app.
 * This class is responsible for maintaining a Singleton com.peternaggschga.gwent.data.UnitRepository connected to the com.peternaggschga.gwent.data.AppDatabase of this app.
 */
public class GwentApplication extends Application {
    /**
     * UnitRepository used for communication to database.
     * Is lazily initialized in getRepository() and provided as a Singleton.
     * @see .getRepository()
     * @see .getRepository(Context)
     */
    private static UnitRepository repository = null;
    /**
     * AppDatabase used by this app.
     * Is initialized in onCreate().
     *
     * @see .onCreate()
     */
    private AppDatabase database;

    /**
     * Returns a UnitRepository used by the GwentApplication referenced by the given Context.
     * Wrapper of getRepository().
     * @see .getRepository()
     * @param context Context of a GwentApplication.
     * @return A UnitRepository used by the given GwentApplication to access game state.
     */
    public static Single<UnitRepository> getRepository(@NonNull Context context) {
        return ((GwentApplication) context.getApplicationContext()).getRepository();
    }

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Initializes database.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(this, AppDatabase.class, "database").build();
    }

    /**
     * Returns the repository used to create, read, update and delete game state.
     * Lazily initializes repository, if not yet done so.
     * @see .getRepository(Context)
     * @return A UnitRepository used to access game state.
     */
    public Single<UnitRepository> getRepository() {
        if (repository != null) {
            return Single.just(repository);
        }
        return UnitRepository.getRepository(database).doOnSuccess(unitRepository -> repository = unitRepository);
    }
}
