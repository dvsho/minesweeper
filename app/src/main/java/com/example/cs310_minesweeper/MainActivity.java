package com.example.cs310_minesweeper;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int ROWS = 12;
    private static final int COLS = 10;
    private static final int MINES = 4;
    private final Button[][] cells = new Button[ROWS][COLS];
    private final boolean[][] mineLocations = new boolean[ROWS][COLS];
    private boolean flagMode = false;
    private int mineCount;
    private long startTime;
    private TextView mineCountView;
    private TextView timerView;
    private final Handler timerHandler = new Handler();
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            timerView.setText(String.format("%02d", seconds));
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mineCountView = findViewById(R.id.mineCountView);
        timerView = findViewById(R.id.timerView);
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        ImageView modeButton = findViewById(R.id.modeButton);

        gridLayout.removeAllViews();
        gridLayout.setColumnCount(COLS);
        gridLayout.setRowCount(ROWS);
        mineCount = MINES;
        mineCountView.setText(String.valueOf(mineCount));
        Random random = new Random();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int cellWidth = screenWidth / (COLS + 2);
        int marginSize = 6;

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                cells[i][j] = new Button(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellWidth - 2 * marginSize;
                params.height = cellWidth - 2 * marginSize;
                params.setMargins(marginSize, marginSize, marginSize, marginSize);
                cells[i][j].setLayoutParams(params);
                cells[i][j].setBackgroundColor(Color.GREEN);
                final int finalI = i;
                final int finalJ = j;
                cells[i][j].setOnClickListener(view -> cellClicked(finalI, finalJ));
                gridLayout.addView(cells[i][j]);
                cells[i][j].setTextSize(10);
                mineLocations[i][j] = false;
            }
        }

        for (int i = 0; i < MINES; i++) {
            int row, col;
            do {
                row = random.nextInt(ROWS);
                col = random.nextInt(COLS);
            } while (mineLocations[row][col]);
            mineLocations[row][col] = true;
        }

        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
        modeButton.setOnClickListener(v -> {
            flagMode = !flagMode;
            modeButton.setImageResource(flagMode ? R.drawable.flag : R.drawable.pickaxe);
        });
    }

    private void cellClicked(int row, int col) {
        if (flagMode) {
            if (mineCount > 0 || cells[row][col].getText().equals("'")) {
                if (cells[row][col].getText().equals("'")) {
                    cells[row][col].setText("");
                    cells[row][col].setBackgroundColor(Color.GREEN);
                    mineCount++;
                }
                else {
                    cells[row][col].setText("'");
                    cells[row][col].setBackgroundResource(R.drawable.flag_cell);
                    mineCount--;
                }
                mineCountView.setText(String.valueOf(mineCount));
            }
        }
        else {
            if (mineLocations[row][col]) {
                gameOver(false);
            }
            else if (!cells[row][col].getText().equals("'")) {
                revealCell(row, col);
                for (int i = 0; i < ROWS; i++) {
                    for (int j = 0; j < COLS; j++) {
                        if (!mineLocations[i][j] && cells[i][j].isEnabled()) {
                            return;
                        }
                    }
                }
                gameOver(true);
            }
        }
    }

    private void revealCell(int row, int col) {
        if (!cells[row][col].isEnabled()) {
            return;
        }
        if (cells[row][col].getText().equals("'")) {
            mineCount++;
            mineCountView.setText(String.valueOf(mineCount));
        }
        cells[row][col].setEnabled(false);
        cells[row][col].setBackgroundColor(Color.LTGRAY);
        int adjacentMines = 0;
        for (int i = Math.max(0, row - 1); i <= Math.min(ROWS - 1, row + 1); i++) {
            for (int j = Math.max(0, col - 1); j <= Math.min(COLS - 1, col + 1); j++) {
                if (mineLocations[i][j]) {
                    adjacentMines++;
                }
            }
        }

        if (adjacentMines == 0) {
            cells[row][col].setText("");
            for (int i = Math.max(0, row - 1); i <= Math.min(ROWS - 1, row + 1); i++) {
                for (int j = Math.max(0, col - 1); j <= Math.min(COLS - 1, col + 1); j++) {
                    if (i != row || j != col) {
                        revealCell(i, j);
                    }
                }
            }
        }
        else {
            cells[row][col].setText(String.valueOf(adjacentMines));
        }
    }

    private void gameOver(boolean won) {
        timerHandler.removeCallbacks(timerRunnable);
        long endTime = System.currentTimeMillis() - startTime;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                cells[i][j].setEnabled(true);
            }
        }

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (mineLocations[i][j]) {
                    cells[i][j].setBackgroundResource(R.drawable.bomb_cell);
                }
            }
        }

        View.OnClickListener listener = view -> {
            Intent intent = new Intent(MainActivity.this, ResultActivity.class);
            intent.putExtra("timeUsed", endTime);
            intent.putExtra("gameResult", won);
            startActivity(intent);
            finish();
        };

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                cells[i][j].setOnClickListener(listener);
            }
        }
    }
}
