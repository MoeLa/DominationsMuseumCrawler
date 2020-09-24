package bhg.sucks.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.media.ImageReader;
import android.media.projection.MediaProjectionManager;
import android.util.Log;

import com.google.common.base.Stopwatch;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;

public class ScreenshotHelper {

    private static final String TAG = "ScreenshotHelper";
    private final Context context;

    /**
     * @param context {@link Context} to determine the cache dir
     */
    public ScreenshotHelper(Context context) {
        this.context = context;
    }

    /**
     * Takes a screenshot of the complete screen.
     *
     * @return the taken screenshot or <i>null</i>, if not possible
     */
    public Bitmap takeScreenshot() {
        // TODO: Probably better/faster approach with MediaProjectionManager
        // MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        final File screenshotFile = new File(context.getCacheDir(), "img.png");

        Stopwatch swTakingScreenshot = Stopwatch.createStarted();
        ExecuteAsRootBase x = new ExecuteAsRootBase() {

            @Override
            protected List<String> getCommandsToExecute() {
                List<String> result = new ArrayList<>();
                result.add("/system/bin/screencap -p " + screenshotFile.getAbsolutePath());
                return result;
            }

        };
        boolean success = x.execute(); // Takes ~600ms on OnePlus 5T
        if (!success) {
            return null;
        }
        swTakingScreenshot.stop();
        Log.d(TAG, "Taking screenshot in " + swTakingScreenshot);

        Stopwatch swDecodingFile = Stopwatch.createStarted();
        Bitmap ret = BitmapFactory.decodeFile(screenshotFile.getAbsolutePath()); // Takes 50ms
        swDecodingFile.stop();
        Log.d(TAG, "Decoding file in " + swDecodingFile);

        return ret;
    }

    public Bitmap takeScreenshot3() {
        Stopwatch swTakingScreenshot = Stopwatch.createStarted();

        Process suProcess;
        try {
            suProcess = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            Log.d(TAG, "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
            return null;
        }

        try (DataOutputStream os = new DataOutputStream(suProcess.getOutputStream())) {
            os.writeBytes("/system/bin/screencap -p\n");
            os.flush();

            Bitmap screen = BitmapFactory.decodeStream(suProcess.getInputStream());

            os.writeBytes("exit\n");
            os.flush();

            return screen;
        } catch (IOException e) {
            Log.d("ROOT", "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
            return null;
        } finally {
            swTakingScreenshot.stop();
            Log.d(TAG, "Taking screenshot3 in " + swTakingScreenshot);
        }
    }

    // https://omerjerk.in/index.php/2016/03/03/take-screenshot-without-root-in-android/
    public Bitmap takeScreenshot2() {
        ImageReader mImageReader = ImageReader.newInstance(1, 1, ImageFormat.RGB_565, 2);

        MediaProjectionManager mpm = (MediaProjectionManager) context.getSystemService(MEDIA_PROJECTION_SERVICE);
//        mpm.getMediaProjection()

        return null;
    }

}
