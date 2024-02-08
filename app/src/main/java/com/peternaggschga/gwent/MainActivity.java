package com.peternaggschga.gwent;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.peternaggschga.gwent.ui.main.GameBoardViewModel;
import com.peternaggschga.gwent.ui.main.MenuUiStateObserver;
import com.peternaggschga.gwent.ui.main.RowUiStateObserver;

import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private GameBoardViewModel gameBoard;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Schedulers.io().scheduleDirect(() -> {
            gameBoard = new ViewModelProvider(MainActivity.this,
                    ViewModelProvider.Factory.from(GameBoardViewModel.initializer)
            ).get(GameBoardViewModel.class);
            new Handler(Looper.getMainLooper()).post(MainActivity.this::initializeViewModel);
        });

        findViewById(R.id.factionButton).setOnClickListener(v -> inflateFactionPopup());
        findViewById(R.id.coinButton).setOnClickListener(v -> inflateCoinFlipPopup());
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
    }

    private void initializeViewModel() {
        int[] rowIds = {R.id.firstRow, R.id.secondRow, R.id.thirdRow};
        for (int rowId = 0; rowId < rowIds.length; rowId++) {
            RowType row = RowType.values()[rowId];
            ConstraintLayout rowLayout = findViewById(rowIds[rowId]);

            ImageView weather = rowLayout.findViewById(R.id.weatherView);
            ImageView horn = rowLayout.findViewById(R.id.hornView);

            weather.setOnClickListener(v -> gameBoard.onWeatherViewPressed(row));
            horn.setOnClickListener(v -> gameBoard.onHornViewPressed(row));

            final RowUiStateObserver observer = RowUiStateObserver.getObserver(row,
                    rowLayout.findViewById(R.id.pointView),
                    weather,
                    horn,
                    rowLayout.findViewById(R.id.cardCountView));
            gameBoard.getRowUiState(row).observe(this, observer);
        }

        ImageButton reset = findViewById(R.id.resetButton);
        ImageButton weather = findViewById(R.id.weatherButton);
        ImageButton burn = findViewById(R.id.burnButton);

        final MenuUiStateObserver observer = new MenuUiStateObserver(findViewById(R.id.overallPointView),
                reset,
                weather,
                burn);
        gameBoard.getMenuUiState().observe(this, observer);

        reset.setOnClickListener(v -> gameBoard.onResetButtonPressed());
        weather.setOnClickListener(v -> gameBoard.onWeatherButtonPressed());
        burn.setOnClickListener(v -> gameBoard.onBurnButtonPressed());

        gameBoard.updateUi();
    }

    private void inflateFactionPopup() {
        Toast.makeText(this, "Not yet implemented!", Toast.LENGTH_SHORT).show();
        // TODO
    }

    private void inflateCoinFlipPopup() {
        Toast.makeText(this, "Not yet implemented!", Toast.LENGTH_SHORT).show();
        // TODO
    }
}
