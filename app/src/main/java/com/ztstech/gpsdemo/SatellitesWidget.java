package com.ztstech.gpsdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.GnssStatus;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.LinkedHashMap;
import java.util.Map;

import androidx.annotation.Nullable;

/**
 * 搜星图控件
 */
public class SatellitesWidget extends View {
    Context context;
    private Paint mOvalPaintGrey;
    private Paint mCirclePaintGrey;
    private Paint mLinePaint;
    private Paint mTextPaint;

    //卫星数据
    private GnssStatus status;
    //圆心坐标
    private float centerX;
    private float centerY;
    //半径
    private float circleRadius;
    private int dp5;

    private Bitmap bmCn;
    private Bitmap bmEu;
    private Bitmap bmIndia;
    private Bitmap bmJp;
    private Bitmap bmRussia;
    private Bitmap bmSbas;
    private Bitmap bmUfo;
    private Bitmap bmUsa;

    public SatellitesWidget(Context context) {
        super(context);
        this.context = context;
    }

    public SatellitesWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        mOvalPaintGrey = new Paint();
        mOvalPaintGrey.setStyle(Paint.Style.STROKE);
        mOvalPaintGrey.setAntiAlias(true);
        mOvalPaintGrey.setColor(Color.parseColor("#5dffffff"));

        mCirclePaintGrey = new Paint();
        mCirclePaintGrey.setStyle(Paint.Style.STROKE);
        mCirclePaintGrey.setAntiAlias(true);
        mCirclePaintGrey.setColor(Color.parseColor("#5dffffff"));

        mLinePaint = new Paint();
        mLinePaint.setColor(Color.parseColor("#5dffffff"));
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        dp5 = SizeUtil.dip2px(context, 5);

        mTextPaint = new TextPaint();
        mTextPaint.setColor(Color.parseColor("#ffcb6429"));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setTextSize(SizeUtil.dip2px(context, 9));


        bmCn = BitmapFactory.decodeResource(context.getResources(), R.drawable.flag_cn);
        bmEu = BitmapFactory.decodeResource(context.getResources(), R.drawable.flag_eu);
        bmIndia = BitmapFactory.decodeResource(context.getResources(), R.drawable.flag_india);
        bmJp = BitmapFactory.decodeResource(context.getResources(), R.drawable.flag_jp);
        bmRussia = BitmapFactory.decodeResource(context.getResources(), R.drawable.flag_russia);
        bmSbas = BitmapFactory.decodeResource(context.getResources(), R.drawable.flag_sbas);
        bmUfo = BitmapFactory.decodeResource(context.getResources(), R.drawable.flag_ufo);
        bmUsa = BitmapFactory.decodeResource(context.getResources(), R.drawable.flag_usa);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMySize(SizeUtil.getScreenWidth(context), widthMeasureSpec);
        //宽高比16:9
        int height = width / 16 * 9;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //页面宽
        int w = getMeasuredWidth();
        //页面高
        int h = getMeasuredHeight();
        //每份长度
        float unit = getMeasuredWidth() / 16;
        //圆半径
        circleRadius = 8 * unit / 2;
        //圆 圆心
        centerX = w / 2;
        centerY = h / 2;

        //画第一个圆环
        float ovalWidth = circleRadius / 3;
        mOvalPaintGrey.setStrokeWidth(ovalWidth);
        RectF oval = new RectF(centerX - (circleRadius * 2 / 3 + ovalWidth / 2), centerY - (circleRadius * 2 / 3 + ovalWidth / 2), centerX + (circleRadius * 2 / 3 + ovalWidth / 2), centerY + (circleRadius * 2 / 3 + ovalWidth / 2));
        canvas.drawOval(oval, mOvalPaintGrey);
//        canvas.drawRect(oval, mLinePaint);

        //画第二个圆
        mCirclePaintGrey.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, circleRadius / 3, mCirclePaintGrey);

        //画线
        canvas.drawLine(centerX, centerY, centerX + circleRadius, centerY, mLinePaint);
        canvas.save();

        int i = 0;
        while (i < 360) {
            canvas.rotate(30, centerX, centerY);
            canvas.drawLine(centerX, centerY, centerX + circleRadius, centerY, mLinePaint);
            i += 30;
        }
        canvas.restore();


//        canvas.save();
//        canvas.rotate(360,centerX,centerY);
//        canvas.drawBitmap(bitmap,null,new RectF( mDestRect),null);
//        canvas.restore();

        if (status != null) {
            int satelliteCount = status.getSatelliteCount();
            //画点 初始在竖线上 先把坐标原点移动到圆心再绘制；最后要恢复
            canvas.save();
            canvas.translate(centerX, centerY);
            for (int index = 0; index < satelliteCount; index++) {
                drawPoint(canvas, status.getAzimuthDegrees(index), status.getElevationDegrees(index), status.getSvid(index), status.getConstellationType(index));
            }
            canvas.restore();
            drawLegend(canvas, status);

        }


    }

    /**
     * 绘制图例
     *
     * @param canvas
     * @param status
     */
    private void drawLegend(Canvas canvas, GnssStatus status) {
        int satelliteCount = status.getSatelliteCount();
        Map<Integer, Integer> satelliteTypeMap = new LinkedHashMap<>();
        for (int index = 0; index < satelliteCount; index++) {
            int type=status.getConstellationType(index);
            if (satelliteTypeMap.containsKey(type)) {
                satelliteTypeMap.put(type, satelliteTypeMap.get(type) + 1);
            } else {
                satelliteTypeMap.put(type, 1);
            }
        }

         canvas.translate(50, 50);
        int count = 0;
        for (Map.Entry<Integer, Integer> entry : satelliteTypeMap.entrySet()) {
            int x = dp5 * 2;
            int y = dp5 * 2 + (dp5 * 2+dp5) * count;
            canvas.drawPoint(x,y,mTextPaint);
            RectF rectF = new RectF();
            Rect mDestRect = new Rect((int) x - dp5, (int) (y) - dp5, ((int) x) + dp5, (int) (y) + dp5);

            Bitmap bitmap = getBitmap(entry.getKey());

            if (bitmap == null) {
                return;
            }
            canvas.drawBitmap(bitmap, null, new RectF(mDestRect), null);
            Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
//            float baseline = mDestRect.top + (mDestRect.bottom - mDestRect.top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
            float baseline = mDestRect.top + (mDestRect.bottom - mDestRect.top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top-dp5/5;

            canvas.drawText(entry.getValue().toString(), mDestRect.right+dp5, baseline, mTextPaint);
            count++;



        }
    }

    public void updateSatellites(GnssStatus status) {
        this.status = status;
        postInvalidate();
    }

    /**
     * 画卫星点
     *
     * @param azimuthDegrees    方位角
     * @param elevationDegrees  高度角
     * @param svid              id
     * @param constellationType 卫星类型
     */
    private void drawPoint(Canvas canvas, float azimuthDegrees, float elevationDegrees, int svid, int constellationType) {
        float l = ((90 - elevationDegrees) * circleRadius / 90);
        int pointX = (int) (Math.sin(Math.PI * azimuthDegrees / 180) * l);
        int pointY = -(int) (Math.cos(Math.PI * azimuthDegrees / 180) * l);

        Rect mDestRect = new Rect((int) pointX - dp5, (int) (pointY) - dp5, ((int) pointX) + dp5, (int) (pointY) + dp5);
        Bitmap bitmap = getBitmap(constellationType);

        if (bitmap == null) {
            return;
        }
        canvas.drawBitmap(bitmap, null, new RectF(mDestRect), null);
        //再绘制文字
        Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
        int baseline = mDestRect.top + (mDestRect.bottom - mDestRect.top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        canvas.drawText("" + svid, (float) (mDestRect.centerX() + mDestRect.width() / 2 + dp5 / 2), baseline, mTextPaint);
    }

    private Bitmap getBitmap(int constellationType) {
        Bitmap bitmap;
        switch (constellationType) {
            case GnssStatus.CONSTELLATION_GPS:
                bitmap = bmUsa;
                break;
            case GnssStatus.CONSTELLATION_SBAS:
                bitmap = bmSbas;
                break;
            case GnssStatus.CONSTELLATION_GLONASS:
                bitmap = bmRussia;
                break;
            case GnssStatus.CONSTELLATION_QZSS:
                bitmap = bmJp;
                break;
            case GnssStatus.CONSTELLATION_BEIDOU:
                bitmap = bmCn;
                break;
            case GnssStatus.CONSTELLATION_GALILEO:
                bitmap = bmEu;
                break;
            case GnssStatus.CONSTELLATION_IRNSS:
                bitmap = bmIndia;
                break;
            default:
                bitmap = bmUfo;
        }
        return bitmap;
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


}
