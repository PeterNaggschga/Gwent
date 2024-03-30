package com.peternaggschga.gwent.ui.dialogs;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.RowType;

public class ShowUnitsDialog extends OverlayDialog {
    @NonNull
    private final RowType row;

    public ShowUnitsDialog(@NonNull Context context, @NonNull RowType row) {
        super(context, R.layout.popup_cards);
        this.row = row;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
}
