package com.peternaggschga.gwent.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.R;

import java.util.Objects;
import java.util.Random;

public class CoinFlipDialog extends Dialog {
    private static final Random random = new Random();

    public CoinFlipDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        switch (random.nextInt(100)) {
            case 0:
                setContentView(R.layout.popup_coin_stewie);
                break;
            case 1:
                setContentView(R.layout.popup_coin_terry);
                break;
            case 2:
                setContentView(R.layout.popup_coin_vin);
                break;
            default:
                setContentView(R.layout.popup_coin_normal);
                if (random.nextBoolean()) {
                    ((ImageView) findViewById(R.id.popup_coin_normal_coinView)).setImageResource(R.drawable.coin_lose);
                    ((TextView) findViewById(R.id.popup_coin_normal_textView)).setText(R.string.popUp_coin_normal_lose);
                }
        }

        Window window = Objects.requireNonNull(getWindow());
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setCancelable(true);

        findViewById(R.id.coinflipBackground).setOnClickListener(v -> cancel());
    }
}
