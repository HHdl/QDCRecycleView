package com.widget.qdcrecyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.qdcares.qdcrecyclerview.R;
import com.widget.qdcrecyclerview.footview.BaseFooterView;
import com.widget.qdcrecyclerview.footview.FooterView;
import com.widget.qdcrecyclerview.listener.OnErrorListener;
import com.widget.qdcrecyclerview.listener.OnLoadMoreListener;
import com.widget.qdcrecyclerview.listener.OnRefreshListener;


/**
 * Author:handa on 2019/7/16
 * Description:
 */
public class QDCRecyclerView extends FrameLayout {
    private Context context;
    //数据为空时候默认布局
    private LinearLayout simpleEmptyView;
    private TextView tvRefresh;
    //尾布局
    private BaseFooterView footerView;
    private RecyclerView recyclerView;
    //刷新控件
    private SwipeRefreshLayout swipeRefreshLayout;

    private GridLayoutManager.SpanSizeLookup spanSizeLookup;
    private QdCaresRecyclerViewAdapter adapter;

    private OnLoadMoreListener onLoadMoreListener;
    private OnRefreshListener onRefreshListener;

    //是否正在上拉加载
    private boolean isLoadingMore;
    //是否正在下拉刷新
    private boolean isComplete;

    //是否使用上拉加载
    private boolean refreshEnable;
    //是否使用下拉刷新
    private boolean loadMoreEnable;

    //最后可见
    private int lastVisiablePosition = 0;

    //----------------------- 实例化方法-----------------------------------

    public QDCRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public QDCRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QDCRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setQdCaresRecyclerView();
    }

    //----------------------- getset-----------------------------------

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        if (footerView != null) {
            ((FooterView) footerView).setOnErrorListener(onErrorListener);
        }
    }

//----------------------- RecyclerView的实例化设置 -----------------------------------

    private void setQdCaresRecyclerView() {
        //初始化
        refreshEnable = true;
        loadMoreEnable = true;

        isLoadingMore = false;
        isComplete = false;

        //view
        setView();
        //监听事件
        setListener();
    }

    private void setView() {
        footerView = new FooterView(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.widget_recycle_recyclerview, this);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.sfl_recyclerview);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_recyclerview);

        simpleEmptyView = new LinearLayout(getContext());
        simpleEmptyView.setOrientation(LinearLayout.VERTICAL);
        simpleEmptyView.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(140, 140);// 定义布局管理器的参数
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.ic_hourglass_empty_gray_24dp);
        imageView.setLayoutParams(imageParams);
        simpleEmptyView.addView(imageView);

        TextView tvNoData = new TextView(context);
        tvNoData.setText(context.getText(R.string.tv_empty));
        tvNoData.setTextSize(20);
        LinearLayout.LayoutParams noDataText = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noDataText.setMargins(0, 8, 0, 0);
        tvNoData.setLayoutParams(noDataText);
        simpleEmptyView.addView(tvNoData);

        tvRefresh = new TextView(context);
        tvRefresh.setText(context.getText(R.string.tv_refresh));
        tvRefresh.setTextColor(context.getResources().getColor(R.color.view_tv_refresh));
        tvRefresh.getPaint().setFakeBoldText(true);

        LinearLayout.LayoutParams refreshText = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        refreshText.setMargins(0, 24, 0, 0);
        tvRefresh.setLayoutParams(refreshText);
        simpleEmptyView.addView(tvRefresh);
    }

    /**
     * 监听事件
     */
    private void setListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isLoadingMore()) {
                    swipeRefreshLayout.setRefreshing(false);
                } else if (onRefreshListener != null) {
                    onRefreshListener.onRefresh();
                    isComplete = false;
                }
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!loadMoreEnable || isLoadingMore || isRefreshing() || isComplete) {
                    return;
                }
                //判断执行onLoadMore listener
                loadMore();
            }
        });
        //默认无数据刷新方法
        tvRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startRefresh();
            }
        });
    }

    private void loadMore() {
        //找到最后一个可见position
        lastVisiablePosition = RecylerViewUtils.getLastPosition(recyclerView.getLayoutManager());

        //判断到最后一个item执行加载更多
        int childCount = adapter == null ? 0 : adapter.getItemCount();
        if (childCount > 1 && lastVisiablePosition == childCount - 1 && !isRefreshing() && !isLoadingMore) {
            if (onLoadMoreListener != null) {
                isLoadingMore = true;
                isComplete = false;
                onLoadMoreListener.onLoadMore();
            }
            if (footerView != null) {
                footerView.onLoadMore();
            }
        }
    }

    /**
     * 设置RecyclerView的Adapter
     *
     * @param adapter
     */
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (adapter != null) {
            this.adapter = new QdCaresRecyclerViewAdapter(adapter);
            recyclerView.setAdapter(this.adapter);
        }
    }

    //----------------------- RecyclerView的boolean的判断 -----------------------------------


    public boolean isLoadingMore() {
        return isLoadingMore;
    }

    public boolean isLoadMoreEnable() {
        return loadMoreEnable;
    }

    public boolean isRefreshing() {
        return swipeRefreshLayout.isRefreshing();
    }

    public boolean isRefreshEnable() {
        return swipeRefreshLayout.isRefreshing();
    }

    //----------------------- RecyclerView的执行方法 -----------------------------------

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setLoadMoreEnable(boolean loadMoreEnable) {
        if (!loadMoreEnable) {
            stopLoadingMore();
        }
        this.loadMoreEnable = loadMoreEnable;
    }

    public void setRefreshEnable(boolean refreshEnable) {
        this.refreshEnable = refreshEnable;
        swipeRefreshLayout.setEnabled(this.refreshEnable);
    }

    public void startRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        isComplete = false;
        if (!isLoadingMore && onRefreshListener != null) {
            onRefreshListener.onRefresh();
        }
    }

    //    可能不符合使用场景
    public void startLoadMore() {
        loadMore();
    }

    public void stopLoadingMore() {
        isLoadingMore = false;
    }

    public void stopRefresh() {
        swipeRefreshLayout.setRefreshing(false);
    }

    public void onLoadMore() {
        if (footerView != null) {
            isComplete = false;
            footerView.onLoadMore();
        }
    }

    public void onNoMore(CharSequence message) {
        if (footerView != null) {
            isComplete = true;
            footerView.onNoMore(message);
        }
    }

    public void onError(CharSequence message) {
        if (footerView != null) {
            isComplete = true;
            footerView.onError(message);
        }
    }


    public void showEmptyView() {
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        removeView(simpleEmptyView);
        addView(simpleEmptyView, params);
        simpleEmptyView.setVisibility(VISIBLE);
        recyclerView.setVisibility(GONE);
    }

    public void hideEmptyView() {
        removeView(simpleEmptyView);
        simpleEmptyView.setVisibility(GONE);
        recyclerView.setVisibility(VISIBLE);
    }

    public void showEmptyView(View view) {
        if (view != null) {
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            removeView(view);
            addView(view, params);
            view.setVisibility(VISIBLE);
            recyclerView.setVisibility(GONE);
        }
    }

    public void hideEmptyView(View view) {
        if (view != null) {
            removeView(view);
            view.setVisibility(GONE);
            recyclerView.setVisibility(VISIBLE);
        }
    }

    //----------------------- Adapter,ViewHolder-----------------------------------
    class QdCaresRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        static final int TYPE_FOOTER = 0x100;
        RecyclerView.Adapter<RecyclerView.ViewHolder> mAdapter;

        QdCaresRecyclerViewAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
            this.mAdapter = adapter;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (TYPE_FOOTER == viewType) {
                return new FooterViewHolder(footerView);
            }
            return mAdapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
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
            return loadMoreEnable ? count + 1 : count;
        }

        @Override
        public long getItemId(int position) {
            return mAdapter.getItemId(position);
        }

        boolean isLoadMoreItem(int position) {
            return loadMoreEnable && position == getItemCount() - 1;
        }

        @Override
        public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp instanceof StaggeredGridLayoutManager.LayoutParams
                    && isLoadMoreItem(holder.getLayoutPosition())) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
            mAdapter.onViewAttachedToWindow(holder);
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
            mAdapter.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
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
        public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
            mAdapter.onDetachedFromRecyclerView(recyclerView);
        }

        @Override
        public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder holder) {
            return mAdapter.onFailedToRecycleView(holder);
        }

        @Override
        public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
            mAdapter.registerAdapterDataObserver(observer);
        }

        @Override
        public void unregisterAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
            mAdapter.unregisterAdapterDataObserver(observer);
        }

        @Override
        public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
            mAdapter.onViewRecycled(holder);
        }
    }

    private class FooterViewHolder extends RecyclerView.ViewHolder {
        FooterViewHolder(View itemView) {
            super(itemView);
        }
    }
}
