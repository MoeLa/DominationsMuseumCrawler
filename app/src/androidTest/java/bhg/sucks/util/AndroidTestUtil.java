package bhg.sucks.util;

import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.common.base.Stopwatch;
import com.google.mlkit.vision.text.Text;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import bhg.sucks.TestOcrHelper;
import bhg.sucks.helper.OcrHelper;

public class AndroidTestUtil {

    private static final String TAG = TestOcrHelper.class.getName();

    private final Context appContext;
    private final OcrHelper ocrHelper;

    public AndroidTestUtil(Context appContext, OcrHelper ocrHelper) {
        this.appContext = appContext;
        this.ocrHelper = ocrHelper;
    }

    public List<Text.TextBlock> resourceToTextBlocks(int resId) throws InterruptedException {
        Bitmap b = bitmapFrom(resId);
        assertNotNull("Bitmap shall not be null", b);

        AtomicReference<List<Text.TextBlock>> textBlocksRef = new AtomicReference<>();

        Stopwatch swResourceToTextBlocks = Stopwatch.createStarted();
        ocrHelper.analyseScreenshot(b, text -> {
            swResourceToTextBlocks.stop();
            Log.d(TAG, "Analyzed screenshot in " + swResourceToTextBlocks);

            synchronized (textBlocksRef) {
                textBlocksRef.set(text.getTextBlocks());
                textBlocksRef.notify();
            }
        }, Throwable::printStackTrace);
        synchronized (textBlocksRef) {
            if (textBlocksRef.get() == null) {
                textBlocksRef.wait();
            }
        }

        return textBlocksRef.get();
    }

    public Bitmap bitmapFrom(int resId) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;

        return BitmapFactory.decodeResource(appContext.getResources(), resId, opts);
    }

}
