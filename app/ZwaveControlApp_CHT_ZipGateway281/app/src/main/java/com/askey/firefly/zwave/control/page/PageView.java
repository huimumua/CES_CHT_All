package com.askey.firefly.zwave.control.page;

import android.content.Context;
import android.widget.RelativeLayout;

/**
 * Created by chiapin on 2017/10/24.
 */

public abstract class PageView extends RelativeLayout {

    public PageView(Context context) {
        super(context);
    }
    public abstract void refreshView();
}
