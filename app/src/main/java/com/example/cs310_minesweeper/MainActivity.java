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
    // instance variables
    private static final int ROWS = 12;
    private static final int COLS = 10;
    private static final int BOMBS = 4;
    private final Button[][] cells = new Button[ROWS][COLS];
    private final boolean[][] bombLocations = new boolean[ROWS][COLS];
    private boolean flagMode = false;
    private int flagCount;
    private long startTime;
    private TextView flagCountView;
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
        // initialize instance
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flagCountView = findViewById(R.id.flagCountView);
        timerView = findViewById(R.id.timerView);
        ImageView modeButton = findViewById(R.id.modeButton);

        // initialize flag count
        flagCount = BOMBS;
        flagCountView.setText(String.valueOf(flagCount));

        // initialize grid
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        gridLayout.removeAllViews();
        gridLayout.setColumnCount(COLS);
        gridLayout.setRowCount(ROWS);

        // initialize grid cells
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
                bombLocations[i][j] = false;
            }
        }

        // randomly place bombs
        Random random = new Random();
        for (int i = 0; i < BOMBS; i++) {
            int row, col;
            do {
                row = random.nextInt(ROWS);
                col = random.nextInt(COLS);
            } while (bombLocations[row][col]);
            bombLocations[row][col] = true;
        }

        // start timer
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);

        // add button that changes between flag and mine mode
        modeButton.setOnClickListener(v -> {
            flagMode = !flagMode;
            modeButton.setImageResource(flagMode ? R.drawable.flag : R.drawable.pickaxe);
        });
    }

    private void cellClicked(int row, int col) {
        // if user is in flag mode
        if (flagMode) {
            // ensure that user doesn't place more flags than there are bombs
            if (flagCount > 0 || cells[row][col].getText().equals(".")) {
                // remove flag
                if (cells[row][col].getText().equals(".")) {
                    cells[row][col].setText("");
                    cells[row][col].setBackgroundColor(Color.GREEN);
                    flagCount++;
                }

                // add flag
                else {
                    cells[row][col].setText(".");
                    cells[row][col].setTextColor(Color.GREEN);
                    cells[row][col].setBackgroundResource(R.drawable.flag_cell);
                    flagCount--;
                }
                flagCountView.setText(String.valueOf(flagCount));
            }
        }

        // if user is in mine mode and clicked non-flag cell
        else if (!cells[row][col].getText().equals(".")) {
            // if user clicked bomb
            if (bombLocations[row][col]) {
                gameOver(false);
            }
            
            // reveal cell and check if user has revealed all non-bomb cells
            else {
                revealCell(row, col);
                for (int i = 0; i < ROWS; i++) {
                    for (int j = 0; j < COLS; j++) {
                        if (!bombLocations[i][j] && cells[i][j].isEnabled()) {
                            return;
                        }
                    }
                }
                gameOver(true);
            }
        }
    }

    private void revealCell(int row, int col) {
        // if cell is already revealed
        if (!cells[row][col].isEnabled()) {
            return;
        }

        // if transitively revealed cell has flag
        if (cells[row][col].getText().equals(".")) {
            flagCount++;
            flagCountView.setText(String.valueOf(flagCount));
        }

        // disable cell
        cells[row][col].setEnabled(false);
        cells[row][col].setBackgroundColor(Color.LTGRAY);

        // find number of adjacent bombs
        int adjacentBombs = 0;
        for (int i = Math.max(0, row - 1); i <= Math.min(ROWS - 1, row + 1); i++) {
            for (int j = Math.max(0, col - 1); j <= Math.min(COLS - 1, col + 1); j++) {
                if (bombLocations[i][j]) {
                    adjacentBombs++;
                }
            }
        }

        // reveal adjacent cells if there are no adjacent bombs
        if (adjacentBombs == 0) {
            cells[row][col].setText("");
            for (int i = Math.max(0, row - 1); i <= Math.min(ROWS - 1, row + 1); i++) {
                for (int j = Math.max(0, col - 1); j <= Math.min(COLS - 1, col + 1); j++) {
                    if (i != row || j != col) {
                        revealCell(i, j);
                    }
                }
            }
        }
        
        // show number of adjacent bombs
        else {
            cells[row][col].setText(String.valueOf(adjacentBombs));
            cells[row][col].setTextColor(Color.BLACK);
        }
    }

    private void gameOver(boolean won) {
        // stop timer
        timerHandler.removeCallbacks(timerRunnable);
        long endTime = System.currentTimeMillis() - startTime;

        // enable all cells to be clicked
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                cells[i][j].setEnabled(true);
            }
        }

        // reveal bomb locations
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (bombLocations[i][j]) {
                    cells[i][j].setBackgroundResource(R.drawable.bomb_cell);
                }
            }
        }

        // wait for user to click any cell and start result page
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
