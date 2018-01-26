package com.qdcares.qdcaresrecyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.qdcares.qdcaresrecyclerview.footview.BaseFooterView;
import com.qdcares.qdcaresrecyclerview.footview.FooterView;
import com.qdcares.qdcaresrecyclerview.listener.LoadMoreListener;
import com.qdcares.qdcaresrecyclerview.listener.TouchFromListener;
import com.qdcares.qdcaresrecyclerview.swipemenu.SwipeMenuRecyclerView;

/**
 * Created by handaolin on 2018/1/8.
 */

public class QdCaresRecyclerView extends FrameLayout implements SwipeRefreshLayout.OnRefreshListener {

    //布局为空时布局
    private View emptyView;
    //脚布局
    private BaseFooterView footerView;
    //列表
    private SwipeMenuRecyclerView recyclerView;
    //刷新空间
    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView.LayoutManager layoutManager;
    private LoadMoreListener loadMoreListener;
    private TouchFromListener touchListener;

    private GridLayoutManager.SpanSizeLookup spanSizeLookup;
    private DataObserver dataObserver;
    private RecyclerViewAdapter adapter;

    private boolean isEmptyViewShowing;
    private boolean isLoadingMore;
    private boolean isLoadMoreEnable;
    private boolean isRefreshEnable;

    private int lastVisiablePosition = 0;

    //----------------------- 实例化方法-----------------------------------

    public QdCaresRecyclerView(@NonNull Context context) {
        this(context, null, 0);
    }

    public QdCaresRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QdCaresRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setQdCaresRecyclerView();
    }

    //----------------------- RecyclerView的实例化设置 -----------------------------------

    private void setQdCaresRecyclerView() {
        //初始化
        isEmptyViewShowing = false;
        isRefreshEnable = true;
        isLoadingMore = false;
        isLoadMoreEnable = true;

        //脚布局
        footerView = new FooterView(getContext());
        //
        View view = LayoutInflater.from(getContext()).inflate(R.layout.recyclerview_swipe_recyclerview, this);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefreshlayout);
        recyclerView = (SwipeMenuRecyclerView) view.findViewById(R.id.recyclerview);
        layoutManager = recyclerView.getLayoutManager();

        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            int distance = 0;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isLoadMoreEnable || isLoadingMore || isRefreshing()) {
                    return;
                }

                if (null != touchListener) {
                    if (dy > 0 && distance <= 0) {
                        touchListener.onTouchFrom(dy);
                    } else if (dy < 0 && distance >= 0) {
                        touchListener.onTouchFrom(dy);
                    }
                    distance = dy;
                }

                //找到最后一个可见position
                layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    lastVisiablePosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                } else if (layoutManager instanceof GridLayoutManager) {
                    lastVisiablePosition = ((GridLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
                } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                    int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                    ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                    int max = into[0];
                    for (int value : into) {
                        if (value > max) {
                            max = value;
                        }
                    }
                    lastVisiablePosition = max;
                }

                //判断到最后一个item执行加载更多
                int childCount = adapter == null ? 0 : adapter.getItemCount();
                if (childCount > 1 && lastVisiablePosition == childCount - 1) {
                    if (loadMoreListener != null) {
                        isLoadingMore = true;
                        loadMoreListener.onLoadMore();
                    }
                }
            }
        });
    }

    /**
     * 设置RecyclerView的Adapter
     * 绑定数据观察着
     *
     * @param adapter
     */
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (adapter != null) {
            if (dataObserver == null) {
                dataObserver = new DataObserver();
            }
            this.adapter = new RecyclerViewAdapter(adapter);
            recyclerView.setAdapter(this.adapter);
            adapter.registerAdapterDataObserver(dataObserver);
            dataObserver.onChanged();
        }
    }


    /**
     * 设置listener
     *
     * @param listener
     */
    public void setOnLoadListener(LoadMoreListener listener) {
        loadMoreListener = listener;
    }

    /**
     * 设置滑动接口监听
     *
     * @param touchListener
     */
    public void setTouchListener(TouchFromListener touchListener) {
        this.touchListener = touchListener;
    }

    @Override
    public void onRefresh() {
        if (loadMoreListener != null) {
            if (footerView != null) {
                footerView.onLoadMore();
            }
            loadMoreListener.onRefresh();
        }
    }

    //----------------------- RecyclerView的boolean的判断 -----------------------------------

    public boolean isEmptyViewShowing() {
        return isEmptyViewShowing;
    }

    public boolean isLoadingMore() {
        return isLoadingMore;
    }

    public boolean isLoadMoreEnable() {
        return isLoadMoreEnable;
    }

    public boolean isRefreshing() {
        return swipeRefreshLayout.isRefreshing();
    }

    public boolean isRefreshEnable() {
        return swipeRefreshLayout.isRefreshing();
    }

    //----------------------- RecyclerView的执行方法 -----------------------------------

    public void stopLoadingMore() {
        isLoadingMore = false;
        if (adapter != null) {
            adapter.notifyItemRemoved(adapter.getItemCount());
        }
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setEmptyViewShowing(boolean emptyViewShowing) {
        isEmptyViewShowing = emptyViewShowing;
    }

    public void setLoadingMore(boolean loadingMore) {
        isLoadingMore = loadingMore;
    }

    public void setLoadMoreEnable(boolean loadMoreEnable) {
        if (!loadMoreEnable) {
            stopLoadingMore();
        }
        isLoadMoreEnable = loadMoreEnable;
    }

    public void setRefreshEnable(boolean refreshEnable) {
        isRefreshEnable = refreshEnable;
        swipeRefreshLayout.setEnabled(isRefreshEnable);
    }

    public void setFooterView(BaseFooterView footerView) {
        if (footerView != null) {
            this.footerView = footerView;
        }
    }

    public void setRefreshing(boolean refreshing) {
        swipeRefreshLayout.setRefreshing(refreshing);
        if (refreshing && !isLoadingMore && loadMoreListener != null) {
            loadMoreListener.onRefresh();
        }
    }

    public void setEmptyView(View emptyView) {
        if (emptyView != null) {
            removeView(emptyView);
        }
        this.emptyView = emptyView;
        if (dataObserver != null) {
            dataObserver.onChanged();
        }
    }

    public void complete() {
        swipeRefreshLayout.setRefreshing(false);
        stopLoadingMore();
    }

    public void onNetChange(CharSequence message) {
        if (footerView != null) {
            footerView.onNetChange(message);
        }
    }


    public void onLoadingMore() {
        if (footerView != null) {
            footerView.onLoadMore();
        }
    }


    public void onNoMore(CharSequence message) {
        if (footerView != null) {
            footerView.onNoMore(message);
        }
    }


    public void onError(CharSequence message) {
        if (footerView != null) {
            footerView.onError(message);
        }
    }

    //----------------------- Adapter和Adapter的数据观察者-----------------------------------

    class DataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            RecyclerView.Adapter mAdapter = recyclerView.getAdapter();
            if (mAdapter != null && emptyView != null) {
                int count = 0;
                if (isLoadMoreEnable && mAdapter.getItemCount() != 0) {
                    count++;
                }
                if (mAdapter.getItemCount() == count) {//无数据
                    isEmptyViewShowing = true;
                    if (emptyView.getParent() == null) {
                        FrameLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.gravity = Gravity.CENTER;
                        addView(emptyView, params);
                    }
                    emptyView.setVisibility(VISIBLE);
                } else {//有数据
                    isEmptyViewShowing = false;
                    emptyView.setVisibility(GONE);
                    recyclerView.setVisibility(VISIBLE);
                }
            }
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            adapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            super.onItemRangeChanged(positionStart, itemCount, payload);
            adapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            adapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            adapter.notifyItemRangeRemoved(fromPosition, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            adapter.notifyItemRangeRemoved(positionStart, itemCount);
        }
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        public static final int TYPE_FOOTER = 0x100;
        RecyclerView.Adapter<RecyclerView.ViewHolder> mAdapter;

        public RecyclerViewAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
            this.mAdapter = adapter;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (TYPE_FOOTER == viewType) {
                return new FooterViewHolder(footerView);
            }
            return mAdapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (isLoadMoreItem(position)) {
                return;
            }
            mAdapter.bindViewHolder(holder, position);
        }

        @Override
        public int getItemViewType(int position) {
            if (isLoadMoreItem(position)) {
                return TYPE_FOOTER;
            } else {
                return mAdapter.getItemViewType(position);
            }
        }

        @Override
        public int getItemCount() {
            int count = mAdapter == null ? 0 : mAdapter.getItemCount();
            if (count == 0) {
                return 0;
            }
            return isLoadMoreEnable ? count + 1 : count;
        }

        @Override
        public long getItemId(int position) {
            return mAdapter.getItemId(position);
        }

        public boolean isLoadMoreItem(int position) {
            return isLoadMoreEnable && position == getItemCount() - 1;
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null
                    && lp instanceof StaggeredGridLayoutManager.LayoutParams
                    && isLoadMoreItem(holder.getLayoutPosition())) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
            mAdapter.onViewAttachedToWindow(holder);
        }

        @Override
        public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
            mAdapter.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) manager);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        boolean isLoadMore = isLoadMoreItem(position);
                        if (spanSizeLookup != null && !isLoadMore) {
                            return spanSizeLookup.getSpanSize(position);
                        }
                        return isLoadMore ? gridManager.getSpanCount() : 1;
                    }
                });
            }
            mAdapter.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            mAdapter.onDetachedFromRecyclerView(recyclerView);
        }

        @Override
        public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
            return mAdapter.onFailedToRecycleView(holder);
        }

        @Override
        public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
            mAdapter.registerAdapterDataObserver(observer);
        }

        @Override
        public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
            mAdapter.unregisterAdapterDataObserver(observer);
        }

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            mAdapter.onViewRecycled(holder);
        }
    }

    //----------------------- Adapter的ViewHolder-----------------------------------

    private class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }
}
