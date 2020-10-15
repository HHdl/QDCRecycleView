package com.widget.qdcrecyclerview.footview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.qdcares.qdcrecyclerview.R;
import com.widget.qdcrecyclerview.listener.OnErrorListener;

/**
 * Author:handa on 2019/7/16
 * Description:
 */
public class FooterView extends BaseFooterView{
    private TextView tvFootviewSample;
    private TextView tvFootviewError;
    private ProgressBar pbFootviewSample;
    private LinearLayout llFootviewError;
    private OnErrorListener onErrorListener;

    public FooterView(@NonNull Context context) {
        this(context, null);
    }

    public FooterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FooterView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        View view = LayoutInflater.from(getContext()).inflate(R.layout.widget_recycle_footview, this);
        pbFootviewSample = (ProgressBar) view.findViewById(R.id.pb_footview_sample);
        tvFootviewSample = (TextView) view.findViewById(R.id.tv_footview_sample);
        tvFootviewError = (TextView) view.findViewById(R.id.tv_footview_error);
        llFootviewError = (LinearLayout) view.findViewById(R.id.ll_footview_error);
        llFootviewError.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onErrorListener != null) {
                    onErrorListener.onError();
                }
            }
        });
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    @Override
    public void onLoadMore() {
        pbFootviewSample.setVisibility(VISIBLE);
        llFootviewError.setVisibility(GONE);
        tvFootviewSample.setVisibility(GONE);
    }

    @Override
    public void onNoMore(CharSequence message) {
        showSampleText(message);
    }

    @Override
    public void onError(CharSequence message) {
        showErrorText(message);
    }

    private void showSampleText(CharSequence message) {
        pbFootviewSample.setVisibility(GONE);
        llFootviewError.setVisibility(GONE);
        tvFootviewSample.setVisibility(VISIBLE);
        tvFootviewSample.setText(message);
    }

    private void showErrorText(CharSequence message) {
        pbFootviewSample.setVisibility(GONE);
        llFootviewError.setVisibility(VISIBLE);
        tvFootviewSample.setVisibility(GONE);
        tvFootviewError.setText(message);
    }
}
