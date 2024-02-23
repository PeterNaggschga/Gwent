package com.peternaggschga.gwent.domain.cases;

import android.content.Context;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.UnitRepository;

abstract class DialogUseCase {
    /**
     * Context of created Dialog objects.
     */
    protected final Context context;

    /**
     * UnitRepository effected by this DialogUseCase.
     */
    @NonNull
    protected final UnitRepository repository;

    /**
     * Constructor of a DialogUseCase object.
     *
     * @param context    Context of created Dialog objects.
     * @param repository UnitRepository effected by this DialogUseCase.
     */
    protected DialogUseCase(@NonNull Context context, @NonNull UnitRepository repository) {
        this.context = context;
        this.repository = repository;
    }
}
