package com.peternaggschga.gwent.data;

import androidx.annotation.NonNull;

import io.reactivex.rxjava3.core.Completable;

/**
 * Simple interface for a pull-observer.
 * Intended for (but not limited to) use with the android.database.Observable class.
 * @see android.database.Observable
 */
public interface Observer {
    /**
     * Notifies the observer of updates to the observed Subject.
     *
     * @return A Completable tracking operation status.
     */
    @NonNull
    Completable update();
}
