package com.ztstech.gpsdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.wifi.WifiInfo;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

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

    //wifi数据
    List<WifiInfo> wifiInfos;

    //横轴 信道号  2.4GHz频带  一共有14个信道（1~14），但第14信道一般不用
    int maxChannel = 196;
    int minChannel = 1;
    //纵轴 强度
    int maxRssi = 0;
    int minRssi = -100;

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

    LinkedHashMap<Integer,Integer> xCoordinateMap=new LinkedHashMap<>();

    List<Integer> netWorkIds=new ArrayList<>();

    public WifiChannelSheet(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;


        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        linePaint.setColor(Color.parseColor("#5dffffff"));
        linePaint.setStrokeWidth(SizeUtil.dip2px(context, 1) / 2);

        mTextPaint=new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.parseColor("#ffffff"));
        mTextPaint.setTextSize(SizeUtil.sp2px(context, 12));

        mLinesPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinesPaint.setColor(Color.RED);
        mLinesPaint.setStrokeWidth(5);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMySize(SizeUtil.getScreenWidth(context), widthMeasureSpec);
        //宽高比16:9
        int height = width / 16 * 9;
        setMeasuredDimension(width, height);
        bottom=SizeUtil.dip2px(context, 30);
        left=SizeUtil.dip2px(context, 30);
        right=SizeUtil.dip2px(context, 12);
        top=SizeUtil.dip2px(context, 15);
        spacing=SizeUtil.dip2px(context, 30);

        xLength=getMeasuredWidth()-right-left;
        yLength=getMeasuredHeight()-bottom-top;

        x=left;
        y=yLength+top;
        netWorkIds.clear();
        netWorkIds.add(44);
        netWorkIds.add(161);
        netWorkIds.add(149);

        //从到大排序
        netWorkIds.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.intValue()-o2.intValue();
            }
        });
        spacing=xLength/(netWorkIds.size()+7+1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawVertical(canvas);
        drawHorizontal(canvas);
        drawParabolas(canvas);
    }

    private void drawParabolas(Canvas canvas) {
        for (WifiInfo info:wifiInfos){
            int topX=xCoordinateMap.get(info.getNetworkId());
            int  topY=Math.abs(info.getLinkSpeed()/100)*100;
            drawParabola(canvas, topX-spacing/2,topX+spacing/2,topY);
        }
    }

    /**
     * 竖直方向刻度
     * @param canvas
     */
    private void drawVertical(Canvas canvas) {
        //y轴
        canvas.drawLine(x, y, x, top , linePaint);
        //绘制y轴文字
        int bottom = canvas.getHeight()-SizeUtil.dip2px(context, 12);
        mTextPaint.getFontMetrics();
//        textY=centerY+a;


        if (maxRssi > -25) {
            //0 -25  -50 -75
            drawHintTextY(canvas,x,top+yLength/ 4,"-75");
            drawHintTextY(canvas,x,top+yLength / 4 * 2,"-50");
            drawHintTextY(canvas,x,top+yLength / 4 * 3,"-25");

        } else if (maxRssi > -40) {
            //-20 -40 -60 -80
        }

    }

    /**
     * 绘制y轴提示文字
     * @param canvas
     * @param x 文字右侧中心的坐标
     * @param y
     * @param text
     */
    private void drawHintTextY(Canvas canvas, int x, int y, String text) {
        mTextPaint.getFontMetrics();

        Rect bounds = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), bounds);
        float textX=(x-bounds.width())/2;
        float textY=y-bounds.height()/2+(bounds.height()/2+(Math.abs(mTextPaint.ascent())-mTextPaint.descent())/2);
        canvas.drawText(text,textX,textY,mTextPaint);
//        canvas.drawPoint(x, y, mLinesPaint);
//        canvas.drawPoint(textX, textY, mLinesPaint);

    }


    /**
     * 绘制y轴提示文字
     * @param canvas
     * @param x 文字顶部中心的坐标
     * @param y
     * @param text
     */
    private void drawHintTextX(Canvas canvas, int x, int y, String text) {
        mTextPaint.getFontMetrics();

        Rect bounds = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), bounds);
        float textX=x-bounds.width()/2;
        float textY=y+(bounds.height()/2+(Math.abs(mTextPaint.ascent())-mTextPaint.descent())/2)+SizeUtil.dip2px(context, 5);
        canvas.drawText(text,textX,textY,mTextPaint);
//        canvas.drawPoint(x, y, mLinesPaint);
//        canvas.drawPoint(textX, textY, mLinesPaint);

    }

    /**
     * 水平方向三根线 强度-100 到0 每隔20画一根
     */
    private void drawHorizontal(Canvas canvas) {
        canvas.drawLine(x, top+yLength/ 4, x+xLength, top+yLength / 4, linePaint);
        canvas.drawLine(x, top+yLength / 4 * 2, x+xLength, top+yLength / 4 * 2, linePaint);
        canvas.drawLine(x, top+yLength / 4 * 3, x+xLength, top+yLength / 4 * 3, linePaint);
        canvas.drawLine(x, y, x+xLength, y, linePaint);
        int i=0;
        //前14个固定；
        for ( i=0;i<14;i++){
            if(i%2==1){
                drawHintTextX(canvas,x+spacing/2*i,y,""+i);
            }
            xCoordinateMap.put(i,x+spacing/2*i);
        }
        for(int j=0;j<netWorkIds.size();j++){
            int id;
            if(j>0&&xCoordinateMap.containsKey(netWorkIds.get(j-1))) {
                id=netWorkIds.get(j-1);
            }else {
                id=13;
            }
            //前一个数字的x坐标
            int oldX=xCoordinateMap.get(id);
                drawHintTextX(canvas,oldX+spacing,y,"...");
                drawHintTextX(canvas,oldX+spacing*2,y,""+netWorkIds.get(j));
                xCoordinateMap.put(id,oldX+spacing*2);

        }

    }

    /**
     * 画抛物线
     * @param canvas
     */
    private void drawParabola(Canvas canvas,int start,int end,int height){
        Path path=new Path();
        path.quadTo(start, y,(end-start)/2,height);
        canvas.drawPath(path,mLinesPaint);

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

    public void setWifiInfos(List<WifiInfo> wifiInfos) {
        this.wifiInfos = wifiInfos;
        preHandleWiFiInfo();
        invalidate();
    }

    private void preHandleWiFiInfo() {
        if (wifiInfos == null || wifiInfos.size() == 0) {
            return;
        }

        for (WifiInfo wifiInfo : wifiInfos) {
            if (wifiInfo.getRssi() > maxRssi) {
                maxRssi = wifiInfo.getRssi();
            }
            if (wifiInfo.getRssi() < minRssi) {
                minRssi = wifiInfo.getRssi();
            }
            if(wifiInfo.getNetworkId()>14){
                netWorkIds.add(wifiInfo.getNetworkId());
            }

        }
        netWorkIds.add(44);
        netWorkIds.add(161);
        netWorkIds.add(149);

        //从到大排序
        netWorkIds.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.intValue()-o2.intValue();
            }
        });
    }
}
