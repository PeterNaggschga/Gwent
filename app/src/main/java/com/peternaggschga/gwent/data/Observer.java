package com.peternaggschga.gwent.data;

import io.reactivex.rxjava3.core.Completable;

/**
 * @todo Documentation
 */
public interface Observer {
    Completable update();
}
