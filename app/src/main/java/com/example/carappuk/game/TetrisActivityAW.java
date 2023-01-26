package com.example.carappuk.game;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.carappuk.R;

import java.util.List;

public class TetrisActivityAW extends Activity {
    private NextBlockView nextBlockView;
    private TetrisViewAW tetrisViewAW;
    private TextView gameStatusTip;
    public TextView score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tetris_aw);
        nextBlockView = (NextBlockView) findViewById(R.id.nextBlockView1);
        tetrisViewAW = (TetrisViewAW) findViewById(R.id.tetrisViewAW1);
        tetrisViewAW.setFather(this);
        gameStatusTip = (TextView) findViewById(R.id.game_staus_tip);
        score = (TextView) findViewById(R.id.score);
    }

    public void setNextBlockView(List<BlockUnit> blockUnits, int div_x) {
        nextBlockView.setBlockUnits(blockUnits, div_x);
    }


    public void startGame(View view) {
        tetrisViewAW.startGame();
        gameStatusTip.setText("Game is running");
    }


    public void pauseGame(View view) {
        tetrisViewAW.pauseGame();
        gameStatusTip.setText("Game is paused");
    }


    public void continueGame(View view) {
        tetrisViewAW.continueGame();
        gameStatusTip.setText("Game is running");
    }


    public void stopGame(View view) {
        tetrisViewAW.stopGame();
        score.setText("" + 0);
        gameStatusTip.setText("Game is paused");
    }


    public void toLeft(View view) {
        tetrisViewAW.toLeft();
    }


    public void toRight(View view) {
        tetrisViewAW.toRight();
    }

    public void toRoute(View view) {
        tetrisViewAW.route();
    }
}