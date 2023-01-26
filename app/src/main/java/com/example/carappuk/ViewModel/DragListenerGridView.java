package com.example.carappuk.ViewModel;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.GridLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stark f**k double Y add J on 2023/1/20.
 *
 */

public class DragListenerGridView extends GridLayout {
    private static final String TAG = "DragListenerGridView";
    private int column = getColumnCount();
    private int row = getRowCount();
    private MyDragListener myDragListener = new MyDragListener();
    private List<View> orderViewList = new ArrayList<>();
    private View dragView;
    public DragListenerGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setChildrenDrawingOrderEnabled(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            orderViewList.add(view);
            view.setOnDragListener(myDragListener);
            view.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    dragView = v;
                    v.startDrag(null,new DragShadowBuilder(v),v,0);
                    return false;
                }
            });

        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int with = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        measureChildren(MeasureSpec.makeMeasureSpec(with/column,MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height/row,MeasureSpec.EXACTLY));
        setMeasuredDimension(with,height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int childLeft;
        int childTop;
        int childWidth = getWidth() / column;
        int childHeight = getHeight() / row;
        for (int index = 0; index < count; index++) {
            View child = getChildAt(index);
            childLeft = index % column * childWidth;
            childTop = index / column * childHeight;
            child.layout(0, 0, childWidth,childHeight);
            child.setTranslationX(childLeft);
            child.setTranslationY(childTop);
        }
    }

    //onDragEvent() ⽅方法也会收到拖拽回调(界⾯面中的每个 View 都会收到)
    class MyDragListener implements OnDragListener{

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()){
                case DragEvent.ACTION_DRAG_STARTED:
                    Log.d(TAG, "onDrag: start");
                    if(event.getLocalState() == v){
                        v.setVisibility(INVISIBLE);
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    Log.d(TAG, "onDrag: entered");
                    if(event.getLocalState() != v){
                        sort(v);
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    Log.d(TAG, "onDrag: ended");
                    v.setVisibility(VISIBLE);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d(TAG, "onDrag: exited");

                    break;
            }
            return true;//别忘记return true
        }
    }

    private void sort(View v) {
        //v依次往后移动一位到dragview
        int dragId = -1;
        int targetId = -1;
        for (int i = 0; i < getChildCount(); i++) {
            if(orderViewList.get(i) == v){
                targetId = i;
            }else if (orderViewList.get(i) == dragView){
                dragId = i;
            }
        }

        if (targetId != dragId) {
            try {
                orderViewList.remove(dragId);
                orderViewList.add(targetId, dragView);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println(e.toString());
            }
        }

        int childWith = getWidth() / column;
        int height = getHeight() / row;

        for (int i = 0; i < getChildCount(); i++) {
            View child = orderViewList.get(i);
            int left = i % column * childWith;
            int top = i / column * height ;
            child.animate().
                    translationX(left).
                    translationY(top)
                    .setDuration(150);
        }
    }

}

