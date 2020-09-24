package bhg.sucks.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.view.GestureDetectorCompat;

import java.util.List;
import java.util.Locale;

import bhg.sucks.activity.MainActivity;
import bhg.sucks.dao.KeepRuleDAO;
import bhg.sucks.helper.ExecuteAsRootBase;
import bhg.sucks.helper.OcrHelper;
import bhg.sucks.helper.ScreenshotHelper;
import bhg.sucks.model.KeepRule;
import bhg.sucks.service.util.ContextUtils;
import bhg.sucks.so.we.need.a.dominationsmuseumcrawler.R;
import bhg.sucks.thread.MyThread;

public class MyService extends Service implements View.OnTouchListener, GestureDetector.OnGestureListener {

    private static final String TAG = "MyService";

    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private ImageView imageIcon;

    private GestureDetectorCompat gestureDetector;

    private int mTouchSlop;
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private boolean running = false;

    private ScreenshotHelper screenshotHelper;
    private OcrHelper ocrHelper;
    private KeepRuleDAO dao;
    private SharedPreferences sharedPref;

    private ContextUtils ctx;

    public MyService() {
        // empty
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.imageIcon = new ImageView(this);
        imageIcon.setImageResource(R.mipmap.red_circle);
        imageIcon.setOnTouchListener(this);

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        this.params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 100;

        this.windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(imageIcon, params);

        ViewConfiguration vc = ViewConfiguration.get(this);
        this.mTouchSlop = vc.getScaledTouchSlop();

        this.gestureDetector = new GestureDetectorCompat(imageIcon.getContext(), this);

        this.ctx = ContextUtils.updateLocale(this, Locale.getDefault());
        this.screenshotHelper = new ScreenshotHelper(this);
        this.ocrHelper = new OcrHelper(ctx);
        this.sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        this.dao = new KeepRuleDAO(sharedPref);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageIcon != null) {
            windowManager.removeView(imageIcon);
        }
    }

    /**
     * Performs test, if in correct screen, and starts the thread.
     */
    private void start() {
        boolean rootEnabled = ExecuteAsRootBase.canRunRootCommands();
        if (!rootEnabled) {
            Toast.makeText(this, "No root permissions", Toast.LENGTH_LONG).show();
            return;
        }

        Bitmap b = screenshotHelper.takeScreenshot3();

        Point p = ocrHelper.isFiveArtifactsAvailable(b);
        if (p == null) {
            Toast.makeText(this, "Not in 'create artifact' screen", Toast.LENGTH_SHORT).show();
            return;
        }

        this.running = true;
        imageIcon.setImageResource(R.mipmap.green_circle);

        MyThread.Delegate d = new MyThread.Delegate() {

            @Override
            public ScreenshotHelper getScreenshotHelper() {
                return screenshotHelper;
            }

            @Override
            public OcrHelper getOcrHelper() {
                return ocrHelper;
            }

            @Override
            public Point getPoint() {
                return p;
            }

            @Override
            public boolean isRunning() {
                return running;
            }

            @Override
            public boolean isKeepThreeStarArtifacts() {
                return sharedPref.getBoolean(getString(R.string.keep_3_artifacts), false);
            }

            @Override
            public List<KeepRule> getKeepRules() {
                return dao.getAll();
            }

        };

        MyThread myThread = new MyThread(d);
        myThread.start();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // Forward event to gesture detector
        return gestureDetector.onTouchEvent(motionEvent);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        // Remember initial location of overlay icon
        initialX = params.x;
        initialY = params.y;
        initialTouchX = motionEvent.getRawX();
        initialTouchY = motionEvent.getRawY();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // Move overlay icon
        int deltaX = (int) (e2.getRawX() - initialTouchX); // delta to initial position
        int deltaY = (int) (e2.getRawY() - initialTouchY); // delta to initial position

        if (Math.abs(deltaX) > mTouchSlop || Math.abs(deltaY) > mTouchSlop) {
            params.x = initialX + deltaX;
            params.y = initialY + deltaY;
            windowManager.updateViewLayout(imageIcon, params);
        }

        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
        // empty by design
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        // Start crawling
        if (!running) {
            start();
        } else {
            running = false;
            imageIcon.setImageResource(R.mipmap.red_circle);
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        // Open Main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        stopSelf();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // empty by design
        return false;
    }
}
