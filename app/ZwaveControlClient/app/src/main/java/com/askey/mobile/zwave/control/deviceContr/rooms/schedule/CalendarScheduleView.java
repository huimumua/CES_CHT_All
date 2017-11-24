package com.askey.mobile.zwave.control.deviceContr.rooms.schedule;

/**
 * Created by skysoft on 2017/10/30.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * 日历型排班表
 * 注意：只能显示本月的日期
 * 功能1：对两种日期进行圆圈背景显示：可排班日期-mSelectableDayColor、已预约日期-mSelectedDayColor
 * 注意：这些日期必须在今日及今日之后，以及，“已预约日期应该包含于可预约日期等逻辑”应该由外部判断
 * 功能2；对点击的日期进行回调，返回点击日期值给外部调用者，日期格式为 年-月-日 分别返回
 */
public class CalendarScheduleView extends View {
    // 画笔
    private Paint paint;

    // 列数
    private static final int NUMS_COLUMN = 8;
    // 行数（星期一行加日期六行）
    private static final int NUMS_ROW = 13;

    // 周日到周六的颜色
    private int mWeekColor = Color.parseColor("#8B8B8B");
    // 本月日期的颜色
    private int mMonthDateColor = Color.parseColor("#000000");
    // 非本月日期的颜色
    private int mOtherDateColor = Color.parseColor("#AEAEAE");
    // 可选日期的背景颜色
    private int mSelectableDayColor = Color.parseColor("#9EB7B5");
    // 选中日期的背景颜色
    private int mSelectedDayColor = Color.parseColor("#E40F57");

    // 星期字体大小
    private int mWeekSize = 18;
    // 日期字体大小
    private int mDateSize = 15;
    // 可选、选择日期的圆圈半径
    private float mCircleR;

    // 当前年
    private int mCurrentYear;
    // 当前月
    private int mCurrentMonth;
    // 可选的日期（排班日期）
    // 已选日期（已预约日期）
    private List<Integer> mSelectedDates = new ArrayList<Integer>();
    // 本月日期-在绘图时将数据储存在此，点击日历时做出判断
    // 7行7列（第一行没有数据，为了计算位置方便，将星期那一行考虑进去）
    private int[][] days = new int[NUMS_ROW - 1][NUMS_COLUMN -1];//7 12

    // 列宽
    private int mColumnWidth;
    // 行高
    private int mRowHeight;

    // DisplayMetrics对象
    private DisplayMetrics displayMetrics;

    // 点击事件接口
    private OnDateClick onDateClick;
    private Canvas mCanvas;
    private int lastRow, lastColumn;
    private int currentRow = 0, currentColumn = 0;

    /**
     * 构造函数
     *
     * @param context
     * @param attrs
     * @description 初始化
     */
    public CalendarScheduleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 获取DisplayMetrics实例
        displayMetrics = getResources().getDisplayMetrics();
        // 获取日历实例
        Calendar calendar = Calendar.getInstance();
        // new一个Paint实例(抗锯齿)
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // 获取当前年份
        mCurrentYear = calendar.get(Calendar.YEAR);
        // 获取当前月份
        mCurrentMonth = calendar.get(Calendar.MONTH);

        // 假数据，测试用，TODO:记得删除
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = displayMetrics.densityDpi * 100;
        }

        int heightSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(widthMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = displayMetrics.densityDpi * 120;
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 计算每一列宽度
        mColumnWidth = getWidth() / NUMS_COLUMN;
        // 计算每一行高度
        mRowHeight = getHeight() / NUMS_ROW;
        // 绘制本月日期
        drawDateText(canvas);
        // 绘制星期
        drawDayOfWeekText(canvas);

        // 绘制前一个月的日期
//        drawDateTextOfLastMonth(canvas);
        // 绘制下一个月的日期
//        drawDateTextOfNextMonth(canvas);



    }

    private int downX = 0, downY = 0, upX = 0, upY = 0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            // 若是按下，则获取坐标
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                downY = (int) event.getY();
                break;

            // 若是点击后放开
            case MotionEvent.ACTION_UP:
                upX = (int) event.getX();
                upY = (int) event.getY();
                if(Math.abs(downX - upX) < 10 && Math.abs(downY - upY) < 10) {
                    // （点击事件）因为这里返回true，导致事件不会往上传，因此“手动”上传
                    performClick();
                    // 处理点击事件
                    handleClick((upX + downX) / 2, (upY + downY) / 2);
                }
                break;

            default:
                break;

        }
        // 返回true表示已经消费此事件，不上传了（这样才能监听所有动作，而不是只有ACTION_DOWN）
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    /**
     * 处理点击事件
     * @param x
     * @param y
     */
    private void handleClick(int x, int y) {
        lastRow = currentRow;
        lastColumn = currentColumn;
        // 获取行
        currentRow = y / mRowHeight;
        // 获取列
        currentColumn = x / mColumnWidth;
        mSelectedDates.clear();
//        Log.i("aaaaaaaaaaa", "row=" + row);
//        Log.i("aaaaaaaaaaa", "column=" + column);
        if (currentRow != 0 && currentColumn != 0) {
            // 获取点击的日期
        int clickedDay = days[currentRow - 1][currentColumn - 1];
            Log.i("aaaaaaaaaaa", "day=" + clickedDay);

            if (lastRow == 0 || lastColumn == 0) {
                mSelectedDates.add(clickedDay);//
            } else {
                if (lastRow <= currentRow && lastColumn <= currentColumn) {
                    for (int i = lastRow; i < currentRow + 1; i++) {
                        for (int j = lastColumn; j < currentColumn + 1; j++) {
                            mSelectedDates.add(days[i - 1][j - 1]);//
                        }
                    }
                } else if (lastRow >= currentRow && lastColumn <= currentColumn) {
                    for (int i = currentRow; i < lastRow + 1; i++) {
                        for (int j = lastColumn; j < currentColumn + 1; j++) {
                            mSelectedDates.add(days[i - 1][j - 1]);//
                        }
                    }
                }else if (lastRow <= currentRow && lastColumn >= currentColumn) {
                    for (int i = lastRow; i < currentRow + 1; i++) {
                        for (int j = currentColumn; j < lastColumn + 1; j++) {
                            mSelectedDates.add(days[i - 1][j - 1]);//
                        }
                    }
                }else if (lastRow >= currentRow && lastColumn >= currentColumn) {
                    for (int i = currentRow; i < lastRow + 1; i++) {
                        for (int j = currentColumn; j < lastColumn + 1; j++) {
                            mSelectedDates.add(days[i - 1][j - 1]);//
                        }
                    }
                }

            }
            invalidate();
        }

        // 将点击的日期传给接口
//        onDateClick.onClick(mCurrentYear, mCurrentMonth, clickedDay);
//        invalidate(drawSelectedBackground(row,column,x,y));

    }

    /**
     * 绘制星期
     *
     * @param canvas
     */
    private void drawDayOfWeekText(Canvas canvas) {
        for (int column = 1; column < 8; column++) {
            // 星期在第0行
            int row = 0;
            // 列数
            int columnTemp = column;
            // 填写星期
            String day[] = {"M", "T", "W", "T", "F", "S", "S"};
            // 设置字体大小
            paint.setTextSize(mWeekSize * displayMetrics.scaledDensity);
            // 设置画笔颜色
            paint.setColor(mWeekColor);
            // 左边坐标（居中显示）
            int left = (int) (mColumnWidth * columnTemp + (mColumnWidth - paint.getTextSize()) / 2);
            // 顶部坐标 (注意，竖直方向上是以baseline为基准写字的，因此要 - (paint.ascent() + paint.descent()) / 2)
            int top = (int) (mRowHeight * row + mRowHeight / 2 - (paint.ascent() + paint.descent()) / 2);
            // 绘制文字
            canvas.drawText(day[column - 1] + "", left, top, paint);

            for (int rowTemp = 1 ;rowTemp < 13; rowTemp++) {
                //横线
                canvas.drawLine(column * mColumnWidth, rowTemp * mRowHeight, (column + 1) * mColumnWidth, rowTemp * mRowHeight, paint);
                canvas.drawLine(column * mColumnWidth, rowTemp * mRowHeight, column * mColumnWidth, (rowTemp + 1) * mRowHeight, paint);
            }
            //画最后一行
            canvas.drawLine(column * mColumnWidth, 13 * mRowHeight, (column + 1) * mColumnWidth, 13 * mRowHeight, paint);
            }

            for (int rowTemp = 1; rowTemp < 13;rowTemp++) {
                //画最后一列
                canvas.drawLine(8 * mColumnWidth, rowTemp * mRowHeight, 8 * mColumnWidth, (rowTemp + 1) * mRowHeight, paint);
            }
        for (int rowTemp = 0; rowTemp < 13;rowTemp++) {
            //画时间
            paint.setTextSize(10 * displayMetrics.scaledDensity);
            canvas.drawText(String.format("%02d",(rowTemp * 2)), 0, (rowTemp + 1) * mRowHeight, paint);
        }
        }




    /**
     * 绘制本月日期(若有背景，需要先绘制背景，否则会覆盖文字)
     *
     * @param canvas
     */
    private void drawDateText(Canvas canvas) {
        int row = 0;
        int day = 0;
            for (int i = 0;i < (NUMS_ROW - 1);i++) {
                for (int j = 0;j < (NUMS_COLUMN - 1);j++) {
                    day++;
                    days[i][j] = day;//day 里面存时间 改
                }
            }

            // 若是可选日期，绘制背景
            for (int dayTemp : mSelectedDates) {//装数据
                for (int i=0;i < days.length;i++) {
                    for (int j = 0;j < days[i].length;j++) {
                        if (days[i][j] == dayTemp) {
                            drawSelectedBackground(canvas, i + 1, j + 1);
                        }
                    }

                }

            }
    }

    /**
     * 绘制可选日期背景
     *
     * @param canvas
     * @param row
     * @param column
     */
    private void drawSelectableBackground(Canvas canvas, int row, int column) {//row column从1开始
        // 画笔颜色
        paint.setColor(mSelectableDayColor);
        /*// 圆心位置
        float cX = (float) (mColumnWidth * column + mColumnWidth / 2);
        float cY = (float) (mRowHeight * row + mRowHeight / 2);
        // 圆形半径
        mCircleR = (float) (mColumnWidth / 2 * 0.8);
        // 绘制圆形背景
        canvas.drawCircle(cX, cY, mCircleR, paint);*/

    }

    /**
     * 绘制已选日期背景
     *
     * @param canvas
     * @param row
     * @param column
     */
    private void drawSelectedBackground(Canvas canvas, int row, int column) {
        // 画笔颜色
        paint.setColor(mSelectedDayColor);

        int left = (column) * mColumnWidth;
        int top = (row) * mRowHeight;
        int right = (column + 1) * mColumnWidth;
        int bottom = (row + 1) * mRowHeight;

        canvas.drawRect(left,top,right,bottom,paint);
    }
    private Rect drawSelectedBackground(int row, int column,int x,int y) {
        // 画笔颜色
        paint.setColor(mSelectedDayColor);

        int left = (column - 1) * mColumnWidth;
        int top = (row - 1) * mRowHeight;
        int right = column * mColumnWidth;
        int bottom = row * mRowHeight;
//        canvas.drawRect(left,top,right,bottom,paint);
        Rect rect = new Rect(left,top,right,bottom);
        rect.union(x, y);
        return rect;
    }


    /**
     * 绘制前一个月的日期
     *
     * @param canvas
     */
    private void drawDateTextOfLastMonth(Canvas canvas) {
        // 获取上一个月的最后一天是一周内的第几天
        int lastDayOfWeek = TimeUtils.getLastDayOfWeekInLastMonth(mCurrentYear, mCurrentMonth);
        // 获取上一个月的最后一天
        int lastDay = TimeUtils.getLastDayOfLastMonth(mCurrentYear, mCurrentMonth);
        // 写入文字
        for (int column = lastDayOfWeek - 1, i = 0; column >= 0; column--, i++) {
            // 设置字体大小
            paint.setTextSize(mDateSize * displayMetrics.scaledDensity);
            // 设置画笔颜色
            paint.setColor(mOtherDateColor);
            // 日期左边坐标
            int left = (int) (mColumnWidth * column + (mColumnWidth - paint.measureText((lastDay - i) + "")) / 2);
            // 日期顶部坐标 (所在行数为第二行)
            int top = (int) (mRowHeight * 1 + mRowHeight / 2 - (paint.ascent() + paint.descent()) / 2);
            // 绘制文字
            canvas.drawText((lastDay - i) + "", left, top, paint);
        }
    }

    /**
     * 绘制下一个月的日期
     *
     * @param canvas
     */
    private void drawDateTextOfNextMonth(Canvas canvas) {
        // 获取下一个月的第一天是一周内的第几天
        int firstDayOfWeekInNextMonth = TimeUtils.getFirstDayOfWeekInNextMonth(mCurrentYear, mCurrentMonth);
        // 获取本月的第一天是一周内的第几天
        int firstDayOfWeek = TimeUtils.getFirstDayOfWeekInMonth(mCurrentYear, mCurrentMonth);
        // 下个月的第一天
        int firstDay = 1;
        // 所在行数
        int row;
        // 所在列数
        int column = firstDayOfWeekInNextMonth - 1;

        // 如果本月第一天是周五、周六或者周日，下个月从最后一行开始绘制（第6行）,否则，从倒数第二行开始绘制（第5行）
        if (firstDayOfWeek == 6 || firstDayOfWeek == 7 || firstDayOfWeek == 1) {
            row = 6;
        } else {
            row = 5;
        }

        // 写入文字
        for (; row <= 6; row++) {
            for (; column <= 6; column++) {
                // 设置字体大小
                paint.setTextSize(mDateSize * displayMetrics.scaledDensity);
                // 设置画笔颜色
                paint.setColor(mOtherDateColor);
                // 日期左边坐标
                int left = (int) (mColumnWidth * column + (mColumnWidth - paint.measureText(firstDay + "")) / 2);
                // 日期顶部坐标 (所在行数为第七行即最后一行)
                int top = (int) (mRowHeight * row + mRowHeight / 2 - (paint.ascent() + paint.descent()) / 2);
                // 绘制文字
                canvas.drawText(firstDay + "", left, top, paint);
                // 日期加一
                firstDay += 1;
            }
            // 若还有一行要绘制，则column置为0
            column = 0;
        }
    }

    public int getmWeekColor() {
        return mWeekColor;
    }

    public void setmWeekColor(int mWeekColor) {
        this.mWeekColor = mWeekColor;
    }

    public int getmMonthDateColor() {
        return mMonthDateColor;
    }

    public void setmMonthDateColor(int mMonthDateColor) {
        this.mMonthDateColor = mMonthDateColor;
    }

    public int getmOtherDateColor() {
        return mOtherDateColor;
    }

    public void setmOtherDateColor(int mOtherDateColor) {
        this.mOtherDateColor = mOtherDateColor;
    }

    public int getmSelectableDayColor() {
        return mSelectableDayColor;
    }

    public void setmSelectableDayColor(int mSelectableDayColor) {
        this.mSelectableDayColor = mSelectableDayColor;
    }

    public int getmSelectedDayColor() {
        return mSelectedDayColor;
    }

    public void setmSelectedDayColor(int mSelectedDayColor) {
        this.mSelectedDayColor = mSelectedDayColor;
    }

    public int getmWeekSize() {
        return mWeekSize;
    }

    public void setmWeekSize(int mWeekSize) {
        this.mWeekSize = mWeekSize;
    }

    public int getmDateSize() {
        return mDateSize;
    }

    public void setmDateSize(int mDateSize) {
        this.mDateSize = mDateSize;
    }

    public float getmCircleR() {
        return mCircleR;
    }

    public void setmCircleR(float mCircleR) {
        this.mCircleR = mCircleR;
    }


    public List<Integer> getmSelectedDates() {
        return mSelectedDates;
    }

    public void setmSelectedDates(List<Integer> mSelectedDates) {
        this.mSelectedDates = mSelectedDates;
    }

    public void setOnDateClick(OnDateClick onDateClick) {
        this.onDateClick = onDateClick;
    }

    //获取当天（按当前传入的时区）00:00:00所对应时刻的long型值
    private long getStartTimeOfDay(long now, String timeZone) {
        String tz = TextUtils.isEmpty(timeZone) ? "GMT+8" : timeZone;
        TimeZone curTimeZone = TimeZone.getTimeZone(tz);
        Calendar calendar = Calendar.getInstance(curTimeZone);
        calendar.setTimeInMillis(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}

