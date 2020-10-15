package com.qdcares.qdcrecyclerview;


import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Author:handa on 2019/7/16
 * Description:
 */
public class RecylerViewUtils {
    /**
     * 获取最后一个可见position位置
     *
     * @param layoutManager
     * @return 最后一个可见position位置
     */
    public static int getLastPosition(RecyclerView.LayoutManager layoutManager) {
        int lastPosition = 0;
        if (layoutManager instanceof LinearLayoutManager) {
            lastPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
            ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
            int max = into[0];
            for (int value : into) {
                if (value > max) {
                    max = value;
                }
            }
            lastPosition = max;
        }
        return lastPosition;
    }
}
