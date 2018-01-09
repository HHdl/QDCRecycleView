package com.qdcares.qdcaresrecyclerview.listener;

/**
 * Created by handaolin on 2018/1/8.
 */

public interface FooterViewListener {

    /**
     * 网络不好时候调用
     *
     * @param message
     */
    void onNetChange(CharSequence message);

    /**
     * 正常loadingMore布局
     */
    void onLoadMore();

    /**
     * 没有更多数据
     *
     * @param message
     */
    void onNoMore(CharSequence message);

    /**
     * 显示错误信息
     *
     * @param message
     */
    void onError(CharSequence message);
}
