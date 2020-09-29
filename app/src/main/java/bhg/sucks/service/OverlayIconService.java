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
import bhg.sucks.thread.TappingThread;

/**
 * Background service, that display an overlay icon.
 * <p>
 * That icon can be dragged to another position, short-tapped to start {@link TappingThread} and long-tapped to open {@link MainActivity}.
 */
public class OverlayIconService extends Service implements View.OnTouchListener, GestureDetector.OnGestureListener {

    private static final String TAG = "MyService";

    private OverlayData overlayData;
    private GestureDetectorCompat gestureDetector;
    private ContextUtils ctx;

    private boolean running = false;

    private ScreenshotHelper screenshotHelper;
    private OcrHelper ocrHelper;
    private KeepRuleDAO dao;
    private SharedPreferences sharedPref;

    public OverlayIconService() {
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

        this.overlayData = new OverlayData();
        overlayData.init(this);

        this.gestureDetector = new GestureDetectorCompat(overlayData.imageIcon.getContext(), this);

        this.ctx = ContextUtils.updateLocale(this, Locale.getDefault());
        this.screenshotHelper = new ScreenshotHelper(this);
        this.ocrHelper = new OcrHelper(ctx);
        this.sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        this.dao = new KeepRuleDAO(sharedPref);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayData.imageIcon != null) {
            overlayData.windowManager.removeView(overlayData.imageIcon);
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
        overlayData.imageIcon.setImageResource(R.mipmap.green_circle);

        TappingThread.Delegate d = new TappingThread.Delegate() {

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

        TappingThread tappingThread = new TappingThread(d);
        tappingThread.start();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // Forward event to gesture detector
        return gestureDetector.onTouchEvent(motionEvent);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        // Remember initial location of overlay icon
        overlayData.initialX = overlayData.params.x;
        overlayData.initialY = overlayData.params.y;
        overlayData.initialTouchX = motionEvent.getRawX();
        overlayData.initialTouchY = motionEvent.getRawY();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // Move overlay icon
        int deltaX = (int) (e2.getRawX() - overlayData.initialTouchX); // delta to initial position
        int deltaY = (int) (e2.getRawY() - overlayData.initialTouchY); // delta to initial position

        if (Math.abs(deltaX) > overlayData.mTouchSlop || Math.abs(deltaY) > overlayData.mTouchSlop) {
            overlayData.params.x = overlayData.initialX + deltaX;
            overlayData.params.y = overlayData.initialY + deltaY;
            overlayData.windowManager.updateViewLayout(overlayData.imageIcon, overlayData.params);
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
            overlayData.imageIcon.setImageResource(R.mipmap.red_circle);
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

    /**
     * Encapsulates all data to make the overlay icon work and provides an initialization routine to keep that fuss out of onCreate.
     */
    private static class OverlayData {
        private WindowManager windowManager;
        private WindowManager.LayoutParams params;
        private ImageView imageIcon;

        private int mTouchSlop;
        private int initialX;
        private int initialY;
        private float initialTouchX;
        private float initialTouchY;

        private void init(OverlayIconService s) {
            imageIcon = new ImageView(s);
            imageIcon.setImageResource(R.mipmap.red_circle);
            imageIcon.setOnTouchListener(s);

            int LAYOUT_FLAG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
            }

            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    LAYOUT_FLAG,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 100;
            params.y = 100;

            windowManager = (WindowManager) s.getSystemService(WINDOW_SERVICE);
            windowManager.addView(imageIcon, params);

            ViewConfiguration vc = ViewConfiguration.get(s);
            mTouchSlop = vc.getScaledTouchSlop();
        }
    }
}
