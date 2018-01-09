package com.qdcares.qdcaresrecyclerview.footview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qdcares.qdcaresrecyclerview.R;


/**
 * Created by handaolin on 2018/1/8.
 */

public class FooterView extends BaseFooterView {
    private TextView textView;
    private ProgressBar progressBar;

    public FooterView(@NonNull Context context) {
        this(context, null, 0);
    }

    public FooterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FooterView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        View view = LayoutInflater.from(getContext()).inflate(R.layout.recyclerview_footer_view, this);
        progressBar = (ProgressBar) view.findViewById(R.id.footer_view_progressbar);
        textView = (TextView) view.findViewById(R.id.footer_view_tv);
    }

    @Override
    public void onLoadMore() {
        progressBar.setVisibility(VISIBLE);
        textView.setVisibility(GONE);
    }

    @Override
    public void onNetChange(CharSequence message) {
        showText(message);
    }

    @Override
    public void onNoMore(CharSequence message) {
        showText(message);
    }

    @Override
    public void onError(CharSequence message) {
        showText(message);
    }

    private void showText(CharSequence message) {
        progressBar.setVisibility(GONE);
        textView.setText(message);
        textView.setVisibility(VISIBLE);
    }
}
