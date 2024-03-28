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

/**
 * A Dialog class showing the result of a coin-flip.
 * Shows heads or tails with 48.5% probability respectively.
 * The remaining 3% are divided equally between three tie situations.
 *
 * @see R.layout#popup_coin_normal
 * @see R.layout#popup_coin_stewie
 * @see R.layout#popup_coin_terry
 * @see R.layout#popup_coin_vin
 */
public class CoinFlipDialog extends Dialog {
    /**
     * Random used to decide the result of the coin-flip.
     */
    private static final Random random = new Random();

    /**
     * Constructor of a CoinFlipDialog in the given Context.
     * @param context Context in which the Dialog is run.
     */
    public CoinFlipDialog(@NonNull Context context) {
        super(context);
    }

    /**
     * Initializes Layout using #setContentView(int) and sets listeners for each view.
     * The result of the coin-flip is decided here.
     *
     * @param savedInstanceState If this dialog is being reinitialized after
     *                           the hosting activity was previously shut down, holds the result from
     *                           the most recent call to {@link #onSaveInstanceState}, or null if this
     *                           is the first time.
     * @see #setContentView(int)
     */
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
