package com.askey.mobile.zwave.control.util;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;


public class ToastShow
{

	private ToastShow()
	{
		/* cannot be instantiated */
		throw new UnsupportedOperationException("cannot be instantiated");
	}

	public static boolean isShow = true;

	/**
	 * Toast.LENGTH_SHORT
	 * 
	 * @param context
	 * @param message
	 */
	public static void showShort(Context context, String message)
	{
		if (isShow)
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Toast.LENGTH_SHORT
	 * 
	 * @param context
	 * @param message
	 */
	public static void showShort(Context context, int message)
	{
		if (isShow)
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	/**
	 *  Toast.LENGTH_LONG
	 * 
	 * @param context
	 * @param message
	 */
	public static void showLong(Context context, CharSequence message)
	{
		if (isShow)
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

	/**
	 * Toast.LENGTH_LONG
	 * 
	 * @param context
	 * @param message
	 */
	public static void showLong(Context context, int message)
	{
		if (isShow)
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

	/**
	 * @param context
	 * @param message
	 * @param duration
	 */
	public static void show(Context context, String message, int duration)
	{
		if (isShow)
			Toast.makeText(context, message, duration).show();
	}

	/**
	 * @param context
	 * @param message
	 * @param duration
	 */
	public static void show(Context context, int message, int duration)
	{
		if (isShow)
			Toast.makeText(context, message, duration).show();
	}

	private static Toast mToast = null;
	/**
	 * 利用Toast更新UI显示提示信息
	 */
	public static void showToast(final Context mContext, final String text) {
		if(mToast != null)
			mToast.cancel();
		mToast = Toast.makeText(mContext, text, Toast.LENGTH_LONG);
		mToast.show();
	}


	public static void showToastOnUiThread(final Context mContext, final String content) {
		Logg.i("ToastShow","=====showToastOnUiThread====message====="+content);
		((Activity) mContext).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast toast = Toast.makeText(mContext, content,
						Toast.LENGTH_SHORT);
				toast.show();
			}
		});
	}


}