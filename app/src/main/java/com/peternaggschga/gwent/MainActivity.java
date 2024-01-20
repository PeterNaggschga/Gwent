package com.peternaggschga.gwent;

import android.os.Bundle;

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

        new Thread(this::initializeViewModel).start();
    }

    private void initializeViewModel() {
        gameBoard = new ViewModelProvider(this,
                ViewModelProvider.Factory.from(GameBoardViewModel.initializer)
        ).get(GameBoardViewModel.class);

        int[] rowIds = {R.id.firstRow, R.id.secondRow, R.id.thirdRow};
        for (int i = 0; i < rowIds.length; i++) {
            RowType row = RowType.values()[i];
            ConstraintLayout rowLayout = findViewById(rowIds[i]);
            final RowUiStateObserver observer = new RowUiStateObserver(row,
                    rowLayout.findViewById(R.id.pointView),
                    rowLayout.findViewById(R.id.weatherImage),
                    rowLayout.findViewById(R.id.hornImage),
                    rowLayout.findViewById(R.id.cardCountView));
            gameBoard.getRowUiState(row).observe(this, observer);
        }
        final MenuUiStateObserver observer = new MenuUiStateObserver(findViewById(R.id.overallPointView),
                findViewById(R.id.resetButton),
                findViewById(R.id.weatherButton),
                findViewById(R.id.burnButton));
        gameBoard.getMenuUiState().observe(this, observer);
    }
}
