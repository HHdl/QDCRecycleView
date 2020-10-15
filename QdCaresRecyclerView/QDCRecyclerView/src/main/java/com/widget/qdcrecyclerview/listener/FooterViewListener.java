package com.widget.qdcrecyclerview.listener;

/**
 * Author:handa on 2019/7/16
 * Description:
 */
public interface FooterViewListener {
    /**
     * 正常loadingMore布局
     */
    void onLoadMore();

    /**
     * 没有更多数据，完成加载
     *
     * @param message
     */
    void onNoMore(CharSequence message);

    /**
     * 显示错误信息，不仅限于网络错误，有点击事件
     *
     * @param message
     */
    void onError(CharSequence message);
}
