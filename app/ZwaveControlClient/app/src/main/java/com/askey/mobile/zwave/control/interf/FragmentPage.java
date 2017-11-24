package com.askey.mobile.zwave.control.interf;

/**
 * Created by skysoft on 2017/10/11.
 */

public class FragmentPage {

    private FragmentCallback handlePageCallback;
    public static FragmentPage getInstance() {
        return SingleInstanceHolder.sInstance;
    }
    private static class SingleInstanceHolder {
        private static FragmentPage sInstance = new FragmentPage();
    }
    public void setPageCallback(FragmentCallback handlePageCallback) {
        this.handlePageCallback = handlePageCallback;
    }

    public void handlePage() {
        handlePageCallback.handlePage();
    }
}
