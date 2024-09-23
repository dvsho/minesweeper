package com.example.cs310_minesweeper;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // initialize instance
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        TextView resultView = findViewById(R.id.resultView);
        TextView timeView = findViewById(R.id.timeView);
        Button restartButton = findViewById(R.id.restartButton);

        // display result message
        boolean gameResult = getIntent().getBooleanExtra("gameResult", false);
        resultView.setText(gameResult ? "You won!" : "You lost.");

        // display time used
        long timeUsed = getIntent().getLongExtra("timeUsed", 0);
        int seconds = (int) (timeUsed / 1000);
        if (seconds == 1) {
            timeView.setText("Used " + seconds + " second.");
        }
        else {
            timeView.setText("Used " + seconds + " seconds.");
        }

        // wait for user to play again and start main page
        restartButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
