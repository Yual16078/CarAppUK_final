package com.example.carappuk.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


@SuppressLint("DrawAllocation")
public class NextBlockView extends View {

    public static final int beginPoint = 10;

    private static int max_x, max_y;
    private List<BlockUnit> blockUnits = new ArrayList<BlockUnit>();

    private static Paint paintWall = null;
    private static final int BOUND_WIDTH_OF_WALL = 2;
    private static Paint paintBlock = null;
    private int div_x = 0;

    private static final int color[] ={ Color.parseColor("#FF6600"), Color.BLUE, Color.RED, Color.GREEN, Color.GRAY };

    public NextBlockView(Context context) {
        this(context, null);
    }

    public NextBlockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (paintWall == null) {
            paintWall = new Paint();
            paintWall.setColor(Color.LTGRAY);
            paintWall.setStyle(Paint.Style.STROKE);
            paintWall.setStrokeWidth(BOUND_WIDTH_OF_WALL + 1);
        }
        if (paintBlock == null) {
            paintBlock = new Paint();
            paintBlock.setColor(Color.parseColor("#FF6600"));
        }
    }

    public void setBlockUnits(List<BlockUnit> blockUnits, int div_x) {
        this.blockUnits = blockUnits;
        this.div_x = div_x;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        max_x = getWidth();
        max_y = getHeight();
        RectF rel;

        int len = blockUnits.size();


        for (int i = 0; i < len; i++) {
            paintBlock.setColor(color[blockUnits.get(i).color]);
            int x = blockUnits.get(i).x - div_x + BlockUnit.UNIT_SIZE * 2;
            int y = blockUnits.get(i).y + BlockUnit.UNIT_SIZE * 2;
            rel = new RectF(x + BOUND_WIDTH_OF_WALL, y + BOUND_WIDTH_OF_WALL,
                    x + BlockUnit.UNIT_SIZE - BOUND_WIDTH_OF_WALL, y + BlockUnit.UNIT_SIZE - BOUND_WIDTH_OF_WALL);
            canvas.drawRoundRect(rel, 8, 8, paintBlock);
            rel = new RectF(x, y, x + BlockUnit.UNIT_SIZE, y + BlockUnit.UNIT_SIZE);
            canvas.drawRoundRect(rel, 8, 8, paintWall);
        }
    }

}
