package com.example.cs310_minesweeper;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView resultView = findViewById(R.id.resultView);
        TextView timeView = findViewById(R.id.timeView);
        Button restartButton = findViewById(R.id.restartButton);

        boolean gameResult = getIntent().getBooleanExtra("gameResult", false);
        long timeUsed = getIntent().getLongExtra("timeUsed", 0);

        int seconds = (int) (timeUsed / 1000);
        resultView.setText(gameResult ? "You won!" : "You lost!");
        timeView.setText("Time: " + seconds + " seconds");

        restartButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
