package com.peternaggschga.gwent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.ui.GameBoardViewModel;

public class MainActivity extends AppCompatActivity {
    private GameBoardViewModel gameBoard;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(() -> {
            gameBoard = new ViewModelProvider(MainActivity.this,
                    ViewModelProvider.Factory.from(GameBoardViewModel.initializer)
            ).get(GameBoardViewModel.class);
            new Handler(Looper.getMainLooper()).post(this::initializeViewModel);
        }).start();
    }

    private void initializeViewModel() {
        int[] rowIds = {R.id.firstRow, R.id.secondRow, R.id.thirdRow};
        for (int i = 0; i < rowIds.length; i++) {
            RowType row = RowType.values()[i];
            ConstraintLayout rowLayout = findViewById(rowIds[i]);

            ImageView weather = rowLayout.findViewById(R.id.weatherImage);
            ImageView horn = rowLayout.findViewById(R.id.hornImage);

            weather.setOnClickListener(v -> gameBoard.onWeatherViewPressed(row));
            horn.setOnClickListener(v -> gameBoard.onHornViewPressed(row));

            final RowUiStateObserver observer = new RowUiStateObserver(row, rowLayout.findViewById(R.id.pointView), weather, horn, rowLayout.findViewById(R.id.cardCountView));
            gameBoard.getRowUiState(row).observe(this, observer);
        }

        ImageButton reset = findViewById(R.id.resetButton);
        ImageButton weather = findViewById(R.id.weatherButton);
        ImageButton burn = findViewById(R.id.burnButton);

        final MenuUiStateObserver observer = new MenuUiStateObserver(findViewById(R.id.overallPointView), reset, weather, burn);
        gameBoard.getMenuUiState().observe(this, observer);

        reset.setOnClickListener(v -> gameBoard.onResetButtonPressed());
        weather.setOnClickListener(v -> gameBoard.onWeatherButtonPressed());
        burn.setOnClickListener(v -> gameBoard.onBurnButtonPressed());

        gameBoard.updateUi();
    }
}
