package com.example.carappuk.game;

import java.util.List;


public class BlockUnit {
    public static final int UNIT_SIZE = 80;
    public static final int BEGIN = 10;
    public int color;

    public int x, y;

    public BlockUnit() {
    }

    public BlockUnit(int x, int y, int color) {
        /*
         * @param
         */
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public static boolean canMoveToLeft(List<BlockUnit> blockUnits, int max_x, List<BlockUnit> allBlockUnits) {
        for (BlockUnit blockUnit : blockUnits) {
            int x = blockUnit.x;
            if (x - UNIT_SIZE < BEGIN) {
                return false;
            }
            int y = blockUnit.y;
            if (isSameUnit(x - UNIT_SIZE, y, allBlockUnits)) {
                return false;
            }
        }
        return true;
    }


    public static boolean canMoveToRight(List<BlockUnit> blockUnits, int max_x, List<BlockUnit> allBlockUnits) {
        for (BlockUnit blockUnit : blockUnits) {
            int x = blockUnit.x;
            if (x + UNIT_SIZE > max_x - UNIT_SIZE) {
                return false;
            }
            int y = blockUnit.y;
            if (isSameUnit(x + UNIT_SIZE, y, allBlockUnits)) {
                return false;
            }
        }
        return true;
    }


    public static boolean canMoveToDown(List<BlockUnit> blockUnits, int max_y, List<BlockUnit> allBlockUnits) {
        for (BlockUnit blockUnit : blockUnits) {
            int x = blockUnit.x;
            int y = blockUnit.y + UNIT_SIZE * 2;
            if (y > max_y - UNIT_SIZE) {
                return false;
            }
            if (isSameUnit(x, y, allBlockUnits)) {
                return false;
            }
        }
        return true;
    }

    public static boolean canRoute(List<BlockUnit> blockUnits, List<BlockUnit> allBlockUnits) {
        for (BlockUnit blockUnit : blockUnits) {
            if (isSameUnit(blockUnit.x, blockUnit.y, allBlockUnits)) {
                return false;
            }
        }
        return true;
    }


    public static boolean toLeft(List<BlockUnit> blockUnits, int max_x, List<BlockUnit> allBlockUnits) {
        if (canMoveToLeft(blockUnits, max_x, allBlockUnits)) {
            for (BlockUnit blockUnit : blockUnits) {
                blockUnit.x = blockUnit.x - UNIT_SIZE;
            }
            return true;
        }
        return false;
    }


    public static boolean toRight(List<BlockUnit> blockUnits, int max_x, List<BlockUnit> allBlockUnits) {
        if (canMoveToRight(blockUnits, max_x, allBlockUnits)) {
            for (BlockUnit blockUnit : blockUnits) {
                blockUnit.x = blockUnit.x + UNIT_SIZE;
            }
            return true;
        }
        return false;
    }


    public static void toDown(List<BlockUnit> blockUnits, int max_Y, List<BlockUnit> allBlockUnits) {
        for (BlockUnit blockUnit : blockUnits) {
            blockUnit.y = blockUnit.y + BlockUnit.UNIT_SIZE;
        }
    }


    public static boolean isSameUnit(int x, int y, List<BlockUnit> allBlockUnits) {
        for (BlockUnit blockUnit : allBlockUnits) {
            if (Math.abs(x - blockUnit.x) < UNIT_SIZE && Math.abs(y - blockUnit.y) < UNIT_SIZE) {
                return true;
            }
        }
        return false;
    }


    public static void remove(List<BlockUnit> allBlockUnits, int j) {
        for (int i = allBlockUnits.size() - 1; i >= 0; i--) {

            if ((int) ((allBlockUnits.get(i).y - BEGIN) / 50) == j)
                allBlockUnits.remove(i);
        }
    }
}