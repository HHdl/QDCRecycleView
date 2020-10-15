package com.widget.qdcrecyclerview.footview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;



import com.widget.qdcrecyclerview.listener.FooterViewListener;


/**
 * Author:handa on 2019/7/16
 * Description:
 */
public abstract class BaseFooterView extends FrameLayout implements FooterViewListener {
    public BaseFooterView(@NonNull Context context) {
        super(context);
    }

    public BaseFooterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseFooterView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
