package com.askey.mobile.zwave.control.login.interf;

/**
 * Created by skysoft on 2017/10/11.
 */

public class LogInPage {

    private LogInPageCallback handlePageCallback;
    public static LogInPage getInstance() {
        return SingleInstanceHolder.sInstance;
    }
    private static class SingleInstanceHolder {
        private static LogInPage sInstance = new LogInPage();
    }
    public void setPageCallback(LogInPageCallback handlePageCallback) {
        this.handlePageCallback = handlePageCallback;
    }

    public void handlePage() {
        handlePageCallback.handlePage();
    }
}
