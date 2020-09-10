package com.ztstech.gpsdemo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.wifi.ScanResult;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import androidx.annotation.Nullable;

/**
 * wifi信道图表
 *
 * @author BugMonkey
 * @link(https://blog.csdn.net/qq_29373285/article/details/88858827)
 */
public class WifiChannelSheet extends View {
    Context context;

    Paint linePaint;
    //文字
    Paint mTextPaint;
    Paint mLinesPaint;
    //抛物线上的文字
    Paint mParabolaPaint;
    //wifi数据
    List<WifiInfo> wifiInfos = new ArrayList<>();

    //纵轴 强度
    int maxRssi = 0;
    //纵轴最大长度
    int yAxisMaxVal=-100;

    //x轴文字高度
    private int bottom;
    //x轴长度
    private int xLength;

    //y轴文字宽度
    private int left;
    //y轴长度
    private int yLength;
    private int right;
    private int top;

    //x轴间距
    private int spacing;

    //坐标轴原点
    private int x;
    private int y;

    //对应信道的横轴的x坐标
    LinkedHashMap<Integer, Integer> xCoordinateMap = new LinkedHashMap<>();

    //当前所有信道
    List<Integer> channels = new ArrayList<>();
    int spaceCount=0;

    //动画执行进度
    private float duration=0f;

    public WifiChannelSheet(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;


        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        linePaint.setColor(Color.parseColor("#5dffffff"));
        linePaint.setStrokeWidth(SizeUtil.dip2px(context, 1) / 2);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.parseColor("#ffffff"));
        mTextPaint.setTextSize(SizeUtil.sp2px(context, 12));

        mParabolaPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mParabolaPaint.setColor(Color.parseColor("#ffffff"));
        mParabolaPaint.setTextSize(SizeUtil.sp2px(context, 12));


        mLinesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinesPaint.setStrokeWidth(1);
        mLinesPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMySize(SizeUtil.getScreenWidth(context), widthMeasureSpec);
        //宽高比16:9
        int height = width / 16 * 9;
        setMeasuredDimension(width, height);
        bottom = SizeUtil.dip2px(context, 30);
        left = SizeUtil.dip2px(context, 30);
        right = SizeUtil.dip2px(context, 12);
        top = SizeUtil.dip2px(context, 15);

        xLength = getMeasuredWidth() - right - left;
        yLength = getMeasuredHeight() - bottom - top;

        x = left;
        y = yLength + top;

//        startAnimation();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //信道间间距
        spacing = xLength / spaceCount;
        drawVertical(canvas);
        drawHorizontal(canvas);
        drawParabolas(canvas);
    }

    private void drawParabolas(Canvas canvas) {
        if (wifiInfos == null) {
            return;
        }


        for (WifiInfo info : wifiInfos) {
            int topX = xCoordinateMap.get(info.channel);
            //抛物线顶点
            float topY = top+(yLength*(yAxisMaxVal+info.level)/yAxisMaxVal);
            //控制点顶点
            float controlY= ((float)(( topY- Math.pow((1 - 0.5f),2) * y - Math.pow(0.5f,2) * y)/(2*0.5*0.5)) );
            canvas.drawPoint(topX, topY, mLinesPaint);

            int color=getRandomColor();
            drawParabola(canvas, topX - 2*spacing , topX + 2*spacing, y, topY,controlY,info.name,color);
        }
    }

    /**
     * 竖直方向刻度
     *
     * @param canvas
     */
    private void drawVertical(Canvas canvas) {
        //y轴
        canvas.drawLine(x, y, x, top, linePaint);

            drawHintTextY(canvas, x, top + yLength / 4, ""+yAxisMaxVal/4);
            drawHintTextY(canvas, x, top + yLength / 4 * 2, ""+yAxisMaxVal/4*2);
            drawHintTextY(canvas, x, top + yLength / 4 * 3, ""+yAxisMaxVal/4*3);



    }

    /**
     * 绘制y轴提示文字
     *
     * @param canvas
     * @param x      文字右侧中心的坐标
     * @param y
     * @param text
     */
    private void drawHintTextY(Canvas canvas, int x, int y, String text) {
        mTextPaint.getFontMetrics();

        Rect bounds = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), bounds);
        float textX = (x - bounds.width()) / 2;
        float textY = y - bounds.height() / 2 + (bounds.height() / 2 + (Math.abs(mTextPaint.ascent()) - mTextPaint.descent()) / 2);
        canvas.drawText(text, textX, textY, mTextPaint);
//        canvas.drawPoint(x, y, mLinesPaint);
//        canvas.drawPoint(textX, textY, mLinesPaint);

    }


    /**
     * 绘制y轴提示文字
     *
     * @param canvas
     * @param x      文字顶部中心的坐标
     * @param y
     * @param text
     */
    private void drawHintTextX(Canvas canvas, int x, int y, String text) {
        mTextPaint.getFontMetrics();

        Rect bounds = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), bounds);
        float textX = x - bounds.width() / 2;
        float textY = y + (bounds.height() / 2 + (Math.abs(mTextPaint.ascent()) - mTextPaint.descent()) / 2) + SizeUtil.dip2px(context, 5);
        canvas.drawText(text, textX, textY, mTextPaint);
//        canvas.drawPoint(x, y, mLinesPaint);
//        canvas.drawPoint(textX, textY, mLinesPaint);

    }

    /**
     * 绘制抛物线上的提示文字
     *
     * @param canvas
     * @param x      文字顶部中心的坐标
     * @param y
     * @param text
     */
    private void drawParabolaText(Canvas canvas, float x, float y, int color,String text) {
        mParabolaPaint.getFontMetrics();

        Rect bounds = new Rect();
        mParabolaPaint.getTextBounds(text, 0, text.length(), bounds);
        float textX = x - bounds.width() / 2;
        float textY = y - (bounds.height() / 2 + (Math.abs(mParabolaPaint.ascent()) - mParabolaPaint.descent()) / 2) + SizeUtil.dip2px(context, 5)-SizeUtil.dip2px(context, 5);
        mParabolaPaint.setColor(color);
        canvas.drawText(text, textX, textY, mParabolaPaint);


    }

    /**
     * 水平方向三根线 强度-100 到0 每隔1/4画一根
     */
    private void drawHorizontal(Canvas canvas) {
        canvas.drawLine(x, top + yLength / 4, x + xLength, top + yLength / 4, linePaint);
        canvas.drawLine(x, top + yLength / 4 * 2, x + xLength, top + yLength / 4 * 2, linePaint);
        canvas.drawLine(x, top + yLength / 4 * 3, x + xLength, top + yLength / 4 * 3, linePaint);
        canvas.drawLine(x, y, x + xLength, y, linePaint);
        //前14个固定；
        Iterator<Integer> iterator = channels.iterator();
        int lastChannel = 0;
        int oldX = x;

        while (iterator.hasNext()) {
            //当前信道号
            int curChannel = iterator.next();
            //前一个数字的x坐标

            if (xCoordinateMap.containsKey(lastChannel)) {
                oldX = xCoordinateMap.get(lastChannel);
            }

//            if (curChannel <= 14 && curChannel - lastChannel <= 1 || curChannel <= 64 && curChannel - lastChannel <= 2||curChannel>64&&curChannel-lastChannel==4) {
            if (curChannel <= 14 && curChannel - lastChannel <= 1 ) {

                if (curChannel % 2 == 1) {
                    drawHintTextX(canvas, oldX + spacing, y, "" + curChannel);
                }
                    xCoordinateMap.put(curChannel, oldX + spacing);
            }else if( curChannel > 14 && curChannel - lastChannel ==4){
                drawHintTextX(canvas, oldX + spacing*3, y, "" + curChannel);
                xCoordinateMap.put(curChannel, oldX + spacing*3);
            } else {
                //当前与前一个不相邻时，用"..."
                int ellipsisX=oldX + 3*spacing;
                if(lastChannel==14){
                    //14和其他本身有间距，直接在15上画
                    ellipsisX=oldX+2*spacing;
                }
                drawHintTextX(canvas, ellipsisX, y, "...");
                xCoordinateMap.put(-curChannel,oldX + spacing);
                drawHintTextX(canvas, ellipsisX + 3*spacing , y, "" + curChannel);
                xCoordinateMap.put(curChannel, ellipsisX +3*spacing);
            }

            lastChannel = curChannel;
        }


    }

    /**
     * 画抛物线
     *
     * @param canvas
     */
    private void drawParabola(final Canvas canvas, final int startX, final int endX, final int y, final float topY, final float controlY, final String text, final int color) {
        if(duration==0){
            return;
        }
        mLinesPaint.setColor(color);
        mLinesPaint.setAlpha(15);
        mLinesPaint.setStrokeWidth(1);
        mLinesPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        int topX = (startX + endX) / 2;
        Path path = new Path();
        path.moveTo(startX, y);
        path.quadTo(topX, controlY*duration, endX, y);
        canvas.drawPath(path, mLinesPaint);
        drawParabolaText(canvas, topX, topY, color,text);
        mLinesPaint.setStyle(Paint.Style.STROKE);
        mLinesPaint.setStrokeWidth(3);
        mLinesPaint.setAlpha(255);
        canvas.drawPath(path, mLinesPaint);




//       canvas.drawPoint(topX, topY, mLinesPaint);
    }


    private int getRandomColor() {
        Random random = new Random();
        String r = Integer.toHexString(random.nextInt(256)).toUpperCase();
        String g = Integer.toHexString(random.nextInt(256)).toUpperCase();
        String b = Integer.toHexString(random.nextInt(256)).toUpperCase();
        r = r.length() == 1 ? "0" + r : r;
        g = g.length() == 1 ? "0" + g : g;
        b = b.length() == 1 ? "0" + b : b;
        return Color.parseColor("#" + r + g + b);
    }

    private int getMySize(int defaultSize, int measureSpec) {
        int mySize = defaultSize;

        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        switch (mode) {
            case MeasureSpec.UNSPECIFIED: {
                //如果没有指定大小，就设置为默认大小
                mySize = defaultSize;
                break;
            }
            case MeasureSpec.AT_MOST: {
                //如果测量模式是最大取值为size
                //我们将大小取最大值,你也可以取其他值
                mySize = size;
                break;
            }
            case MeasureSpec.EXACTLY: {
                //如果是固定的大小，那就不要去改变它
                mySize = size;
                break;
            }
        }
        return mySize;
    }

    public void setWifiInfos(final List<ScanResult> scanResults) {
        wifiInfos.clear();
        scanResults.forEach(new Consumer<ScanResult>() {
            @Override
            public void accept(ScanResult scanResult) {
                wifiInfos.add(new WifiInfo(getChannelByFrequency(scanResult.frequency), scanResult.level, scanResult.SSID));
            }
        });
        preHandleWiFiInfo();
        invalidate();
    }

    private void preHandleWiFiInfo() {
        if (wifiInfos == null || wifiInfos.size() == 0) {
            return;
        }
        xCoordinateMap.clear();
        channels.clear();
        for (WifiInfo wifiInfo : wifiInfos) {
            //level dBm的检测信号电平,也被称为RSSI。
            if (wifiInfo.level > maxRssi) {
                maxRssi = wifiInfo.level;
            }
            if (maxRssi > -25) {
                //0 -25  -50 -75
                yAxisMaxVal=100;


            } else {
                //-20 -40 -60 -80
                yAxisMaxVal=-80;

            }
            if (!channels.contains(wifiInfo.channel)) {
                channels.add(wifiInfo.channel);
            }

        }

        //增加1~14
        for (int i = 0; i <= 14; i++) {
            if (!channels.contains(i)) {
                channels.add(i);
            }
        }
        //去重
        Set set=new HashSet(channels);
        channels=new ArrayList<>(set);
        //从到大排序
        channels.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.intValue() - o2.intValue();
            }
        });

        int lastChannel=0;
        spaceCount=0;
        for (int i:channels) {
          if(i<=14){
              spaceCount= spaceCount+1;
          }else {
              if(i-lastChannel<=4){
                  spaceCount= spaceCount+4;
              }else {
                  spaceCount= spaceCount+6;
              }
          }
            lastChannel=i;
        }


    }


    /**
     * 根据频率获得信道
     *
     * @param frequency
     * @return
     */
    private int getChannelByFrequency(int frequency) {
        int channel = -1;
        switch (frequency) {
            case 2412:
                channel = 1;
                break;
            case 2417:
                channel = 2;
                break;
            case 2422:
                channel = 3;
                break;
            case 2427:
                channel = 4;
                break;
            case 2432:
                channel = 5;
                break;
            case 2437:
                channel = 6;
                break;
            case 2442:
                channel = 7;
                break;
            case 2447:
                channel = 8;
                break;
            case 2452:
                channel = 9;
                break;
            case 2457:
                channel = 10;
                break;
            case 2462:
                channel = 11;
                break;
            case 2467:
                channel = 12;
                break;
            case 2472:
                channel = 13;
                break;
            case 2484:
                channel = 14;
                break;
            case 5170:
                channel = 34;
                break;

            case 5180:
                channel = 36;
                break;

            case 5190:
                channel = 38;
                break;

            case 5200:
                channel = 40;
                break;

            case 5210:
                channel = 42;
                break;

            case 5220:
                channel = 44;
                break;

            case 5230:
                channel = 46;
                break;

            case 5240:
                channel = 48;
                break;

            case 5260:
                channel = 52;
                break;

            case 5280:
                channel = 56;
                break;

            case 5300:
                channel = 60;
                break;

            case 5320:
                channel = 64;
                break;

            case 5500:
                channel = 100;
                break;

            case 5520:
                channel = 104;

            break;
            case 5540:
                channel = 108;
                break;

            case 5560:
                channel = 112;
                break;

            case 5580:
                channel = 116;
                break;

            case 5600:
                channel = 120;
                break;

            case 5620:
                channel = 124;
                break;

            case 5640:
                channel = 128;
                break;

            case 5660:
                channel = 132;
                break;

            case 5680:
                channel = 136;
                break;

            case 5700:
                channel = 140;
                break;

            case 5745:
                channel = 149;
                break;
            case 5765:
                channel = 153;
                break;
            case 5785:
                channel = 157;
                break;
            case 5805:
                channel = 161;
                break;
            case 5825:
                channel = 165;
                break;
        }
        return channel;
    }

    /**
     * 根据频率获得x坐标轴下标来判断是否连续
     *
     * @param frequency
     * @return
     */
    private int getFrequencyIndexInXaxis(int frequency, int before) {
        int channel = -1;
        switch (frequency) {
            case 2412:
                channel = 1;
                break;
            case 2417:
                channel = 2;
                break;
            case 2422:
                channel = 3;
                break;
            case 2427:
                channel = 4;
                break;
            case 2432:
                channel = 5;
                break;
            case 2437:
                channel = 6;
                break;
            case 2442:
                channel = 7;
                break;
            case 2447:
                channel = 8;
                break;
            case 2452:
                channel = 9;
                break;
            case 2457:
                channel = 10;
                break;
            case 2462:
                channel = 11;
                break;
            case 2467:
                channel = 12;
                break;
            case 2472:
                channel = 13;
                break;
            case 2484:
                channel = 14;
                break;
            case 5170:
                channel = 15;
            case 5180:
                channel = 16;
            case 5190:
                channel = 17;
            case 5200:
                channel = 18;
            case 5210:
                channel = 19;
            case 5220:
                channel = 20;
            case 5230:
                channel = 21;
            case 5240:
                channel = 22;
            case 5260:
                channel = 23;
            case 5280:
                channel = 24;
            case 5300:
                channel = 25;
            case 5320:
                channel = 26;
            case 5500:
                channel = 27;
            case 5520:
                channel = 28;
                break;
            case 5540:
                channel = 29;
            case 5560:
                channel = 30;
            case 5580:
                channel = 31;
            case 5600:
                channel = 32;
            case 5620:
                channel = 33;
            case 5640:
                channel = 34;
            case 5660:
                channel = 35;
            case 5680:
                channel = 36;
            case 5700:
                channel = 37;
            case 5745:
                channel = 38;
                break;
            case 5765:
                channel = 39;
                break;
            case 5785:
                channel = 40;
                break;
            case 5805:
                channel = 41;
                break;
            case 5825:
                channel = 42;
                break;
        }
        return channel;
    }

    class WifiInfo {
        public int channel;
        public int level;
        public String name;

        public WifiInfo(int channel, int level, String name) {
            this.channel = channel;
            this.level = level;
            this.name = name;
        }
    }

    public void startAnimation(){

        ValueAnimator anim = ValueAnimator.ofFloat(path,0f,0.1f,0.3f,0.6f,1f);
        anim.setDuration(300);
        anim.start();
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                duration=animation.getAnimatedFraction();
                invalidate();
            }
        });
    }
}
