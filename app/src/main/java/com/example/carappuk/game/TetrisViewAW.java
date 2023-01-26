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
import java.util.Arrays;
import java.util.List;

@SuppressLint("DrawAllocation")
public class TetrisViewAW extends View {
    /** 网格开始坐标值，横纵坐标的开始值都是此值 */
    public static final int beginPoint = 10;
    /** 俄罗斯方块的最大坐标 */
    private static int max_x, max_y;
    /** 行数和列数 */
    private static int num_x = 0, num_y = 0;
    /** 背景墙画笔 */
    private static Paint paintWall = null;
    /** 俄罗斯方块的单元块画笔 */
    private static Paint paintBlock = null;
    private static final int BOUND_WIDTH_OF_WALL = 2;
    /** 当前正在下落的方块 */
    private List<BlockUnit> blockUnits = new ArrayList<BlockUnit>();
    /** 下一个要显示的方块 */
    private List<BlockUnit> blockUnitBufs = new ArrayList<BlockUnit>();
    /** 下一个要显示的方块 */
    private List<BlockUnit> routeBlockUnitBufs = new ArrayList<BlockUnit>();
    /** 全部的方块allBlockUnits */
    private List<BlockUnit> allBlockUnits = new ArrayList<BlockUnit>();
    /** 调用此对象的Activity对象 */
    private TetrisActivityAW father = null;
    private int[] map = new int[100]; // 保存每行网格中包含俄罗斯方块单元的个数
    /** 游戏的主线程 */
    private Thread mainThread = null;
    // 游戏的几种状态
    /** 标识游戏是开始还是停止 */
    private boolean gameStatus = false;
    /** 标识游戏是暂停还是运行 */
    private boolean runningStatus = false;
    /** 俄罗斯方块颜色数组 */
    private static final int color[] = { Color.parseColor("#FF6600"), Color.BLUE, Color.RED, Color.GREEN, Color.GRAY };
    /** 方块的中心方块单元的坐标, */
    private int xx, yy;
    /** 方块,用户随机获取各种形状的方块 */
    private TetrisBlock tetrisBlock;
    /** 分数 */
    private int score = 0;
    /** 当前方块的类型 */
    private int blockType = 0;

    public TetrisViewAW(Context context) {
        this(context, null);
    }

    public TetrisViewAW(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (paintWall == null) {// 初始化化背景墙画笔
            paintWall = new Paint();
            paintWall.setColor(Color.LTGRAY);
            paintWall.setStyle(Paint.Style.STROKE);
            paintWall.setStrokeWidth(BOUND_WIDTH_OF_WALL + 1);
        }
        if (paintBlock == null) {// 初始化化背景墙画笔
            paintBlock = new Paint();
            paintBlock.setColor(Color.parseColor("#FF6600"));
        }
        tetrisBlock = new TetrisBlock();
        routeBlockUnitBufs = tetrisBlock.getUnits(beginPoint, beginPoint);
        Arrays.fill(map, 0); // 每行网格中包含俄罗斯方块单元的个数全部初始化为0
        // 绘制方块
    }

    /**
     * 设置当前游戏页面的父类activity
     *
     * @param tetrisActivityAW
     */
    public void setFather(TetrisActivityAW tetrisActivityAW) {
        father = tetrisActivityAW;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        max_x = getWidth();
        max_y = getHeight();
        RectF rel;
        // 绘制网格
        num_x = 0;
        num_y = 0;
//        for (int i = beginPoint; i < max_x - BlockUnit.UNIT_SIZE; i += BlockUnit.UNIT_SIZE) {
//            for (int j = beginPoint; j < max_y - BlockUnit.UNIT_SIZE; j += BlockUnit.UNIT_SIZE) {
//                rel = new RectF(i, j, i + BlockUnit.UNIT_SIZE, j + BlockUnit.UNIT_SIZE);
//                canvas.drawRoundRect(rel, 8, 8, paintWall);
//                num_y++;
//            }
//            num_x++;
//        }
        for (int i = beginPoint; i < max_x - BlockUnit.UNIT_SIZE; i += BlockUnit.UNIT_SIZE) {
            for (int j = beginPoint; j < max_y - BlockUnit.UNIT_SIZE; j += BlockUnit.UNIT_SIZE) {
                rel = new RectF(i, j, i + BlockUnit.UNIT_SIZE, j + BlockUnit.UNIT_SIZE);
                canvas.drawRoundRect(rel, 8, 8, paintWall);
                num_y++;
            }
            num_x++;
        }
        // 随机产生一个俄罗斯方块
        int len = blockUnits.size();
        // 绘制方块
        // Toast.makeText(context, "" + len, Toast.LENGTH_SHORT).show();
        for (int i = 0; i < len; i++) {
            int x = blockUnits.get(i).x;
            int y = blockUnits.get(i).y;
            // 设置当前方块的颜色
            paintBlock.setColor(color[blockUnits.get(i).color]);
            rel = new RectF(x + BOUND_WIDTH_OF_WALL, y + BOUND_WIDTH_OF_WALL,
                    x + BlockUnit.UNIT_SIZE - BOUND_WIDTH_OF_WALL, y + BlockUnit.UNIT_SIZE - BOUND_WIDTH_OF_WALL);
            canvas.drawRoundRect(rel, 8, 8, paintBlock);
        }
        // 随机产生一个俄罗斯方块
        len = allBlockUnits.size();
        // 绘制方块
        // Toast.makeText(context, "" + len, Toast.LENGTH_SHORT).show();
        for (int i = 0; i < len; i++) {
            int x = allBlockUnits.get(i).x;
            int y = allBlockUnits.get(i).y;
            paintBlock.setColor(color[allBlockUnits.get(i).color]);
            rel = new RectF(x + BOUND_WIDTH_OF_WALL, y + BOUND_WIDTH_OF_WALL,
                    x + BlockUnit.UNIT_SIZE - BOUND_WIDTH_OF_WALL, y + BlockUnit.UNIT_SIZE - BOUND_WIDTH_OF_WALL);
            canvas.drawRoundRect(rel, 8, 8, paintBlock);
        }
    }

    /**
     * 开始游戏
     */
    public void startGame() {
        gameStatus = true;
        runningStatus = true;
        if (mainThread == null || !mainThread.isAlive()) {
            getNewBlock();
            mainThread = new Thread(new MainThread());
            mainThread.start();
        }

    }

    /**
     * 暂停游戏
     */
    public void pauseGame() {
        runningStatus = false;
    }

    /**
     * 继续游戏
     */
    public void continueGame() {
        runningStatus = true;
    }

    /**
     * 停止游戏
     */
    public void stopGame() {
        // 停止游戏,释放游戏主线程
        runningStatus = false;
        gameStatus = false;
        mainThread.interrupt();
        blockUnits.clear();
        allBlockUnits.clear();
        score = 0;
        invalidate();
    }

    /**
     * 向左滑动
     */
    public void toLeft() {
        if (BlockUnit.toLeft(blockUnits, max_x, allBlockUnits)) {
            xx = xx - BlockUnit.UNIT_SIZE;
        }
        invalidate();
    }

    /**
     * 向右滑动
     */
    public void toRight() {
        if (BlockUnit.toRight(blockUnits, max_x, allBlockUnits)) {
            xx = xx + BlockUnit.UNIT_SIZE;
        }
        invalidate();
    }

    /**
     * 按顺时针旋转
     */
    public void route() {
        if (blockType == 3) {// 如果当前正在下落的方块为正方形,则不进行旋转
            return;
        }
        if (routeBlockUnitBufs.size() != blockUnits.size()) {
            routeBlockUnitBufs = tetrisBlock.getUnits(xx, yy);
        }
        for (int i = 0; i < blockUnits.size(); i++) {
            routeBlockUnitBufs.get(i).x = blockUnits.get(i).x;
            routeBlockUnitBufs.get(i).y = blockUnits.get(i).y;
        }
        for (BlockUnit blockUnit : routeBlockUnitBufs) {
            int tx = blockUnit.x;
            int ty = blockUnit.y;
            blockUnit.x = -(ty - yy) + xx;
            blockUnit.y = tx - xx + yy;
        }
        routeTran(routeBlockUnitBufs);
        if (!BlockUnit.canRoute(routeBlockUnitBufs, allBlockUnits)) {
            // Toast.makeText(father, "不可旋转", Toast.LENGTH_SHORT).show();
            return;
        }
        for (BlockUnit blockUnit : blockUnits) {
            int tx = blockUnit.x;
            int ty = blockUnit.y;
            blockUnit.x = -(ty - yy) + xx;
            blockUnit.y = tx - xx + yy;
        }
        routeTran(blockUnits);
        invalidate();
    }

    /**
     * 如果方块处于边缘,则翻转过后,会出现方块部分处于边缘之外的情况, 因此,通过递归判断是否有超出边缘的部分,
     * 如果有,则进行左右平移,把处于边缘外的方块移动到边缘内
     */
    public void routeTran(List<BlockUnit> blockUnitsBuf) {
        boolean needLeftTran = false;
        boolean needRightTran = false;
        for (BlockUnit u : blockUnitsBuf) {
            if (u.x < beginPoint) {
                needLeftTran = true;
            }
            if (u.x > max_x - BlockUnit.UNIT_SIZE) {
                needRightTran = true;
            }
        }
        if (needLeftTran || needRightTran) {
            for (BlockUnit u : blockUnitsBuf) {
                if (needLeftTran) {
                    u.x = u.x + BlockUnit.UNIT_SIZE;
                } else if (needRightTran) {
                    u.x = u.x - BlockUnit.UNIT_SIZE;
                }
            }
            routeTran(blockUnitsBuf);
        } else {
            return;
        }

    }

    /**
     * 获取一个新的方块
     */
    private void getNewBlock() {
        // 新的方块的坐标，x坐标位于x轴的中间，y 位于起始位置
        this.xx = beginPoint + (num_x / 2) * BlockUnit.UNIT_SIZE;
        this.yy = beginPoint;
        if (blockUnitBufs.size() == 0) {
            // 当游戏第一次开始的时候，先初始化一个方块
            blockUnitBufs = tetrisBlock.getUnits(xx, yy);
        }
        blockUnits = blockUnitBufs;
        blockType = tetrisBlock.blockType;
        blockUnitBufs = tetrisBlock.getUnits(xx, yy);
        if (father != null) {// 显示出下一个要出现的方块
            father.setNextBlockView(blockUnitBufs, (num_x / 2) * BlockUnit.UNIT_SIZE);
        }
    }

    /**
     * 游戏的主线程
     *
     * @sign Created by yingjie on 2023年1月10日
     */
    private class MainThread implements Runnable {

        @Override
        public void run() {
            while (gameStatus) {
                while (runningStatus) {

                    if (BlockUnit.canMoveToDown(blockUnits, max_y, allBlockUnits)) {
                        // 判断是否可以继续下落，如果可以下落，则下落
                        BlockUnit.toDown(blockUnits, max_y, allBlockUnits);
                        yy = yy + BlockUnit.UNIT_SIZE;
                    } else {
                        /**
                         * 当不可以继续下落的时候，把当前的方块添加到allBlockUnits中，
                         * 并且判断是否有需要消除的方块，然后再产生一个新的方块
                         */
                        if ((yy - beginPoint) / 80 == 0) {
                            stopGame();
                        }
                        for (BlockUnit blockUnit : blockUnits) {
                            blockUnit.y = blockUnit.y + BlockUnit.UNIT_SIZE;
                            allBlockUnits.add(blockUnit);
                        }
                        for (BlockUnit u : blockUnits) {
                            // 更新map，即更新每行网格中静止俄罗斯方块单元的个数
                            int index = (int) ((u.y - beginPoint) / 80); // 计算所在行数
                            map[index]++;

                        }
                        // 每行最大个数
                        int end = (int) ((max_y - 50 - beginPoint) / BlockUnit.UNIT_SIZE);
                        int full = (int) ((max_x - 50 - beginPoint) / BlockUnit.UNIT_SIZE) + 1;
                        try {
                            Thread.sleep(GameConfig.SPEED);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        for (int i = 0; i <= end; i++) {
                            /***
                             * 消除需要消除的方块（触发条件，某一行中被塞满了方块，没有空白）
                             * 注意顺序，先消除某一行，再移动这一行上边的方块
                             */
                            if (map[i] >= full) {
                                BlockUnit.remove(allBlockUnits, i);
                                score += 100;
                                map[i] = 0;
                                for (int j = i; j > 0; j--)
                                    map[j] = map[j - 1];
                                map[0] = 0;
                                for (BlockUnit blockUnit : allBlockUnits) {
                                    if (blockUnit.y < (i * BlockUnit.UNIT_SIZE + beginPoint)) {
                                        blockUnit.y = blockUnit.y + BlockUnit.UNIT_SIZE;
                                    }
                                }
                            }
                        }
                        father.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /**
                                 * 刷新分数
                                 */
                                father.score.setText("" + score);
                                invalidate();
                            }
                        });
                        try {
                            Thread.sleep(GameConfig.SPEED * 3);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        father.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getNewBlock();
                                score += 10;
                                father.score.setText("" + score);
                            }
                        });
                    }
                    father.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            invalidate();
                        }
                    });
                    try {
                        Thread.sleep(GameConfig.SPEED);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }

        }

    }

}