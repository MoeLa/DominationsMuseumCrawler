package bhg.sucks.thread;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import java.util.List;

import bhg.sucks.helper.OcrHelper;
import bhg.sucks.helper.ScreenshotHelper;
import bhg.sucks.helper.TapHelper;
import bhg.sucks.model.KeepRule;

/**
 * Thread, that performs the tapping steps in an infinite loop.
 * <p>
 * Note: The thread stops, when <i>delegate#isRunning</i> = false;
 */
public class TappingThread extends Thread {

    private static final String TAG = TappingThread.class.getName();
    private final Delegate delegate;
    private final TapHelper tapHelper;
    private final TappingThreadHelper tappingThreadHelper;

    public TappingThread(Delegate d) {
        this.delegate = d;
        this.tapHelper = new TapHelper(d);
        this.tappingThreadHelper = new TappingThreadHelper();
    }

    @Override
    public void run() {
        while (delegate.isRunning()) {
            tapHelper.tapFiveArtifacts();

            for (int i = 0; i <= 4; i++) {
                if (keepArtifact()) {
                    tapHelper.tapContinue();
                } else {
                    tapHelper.tapSell();
                    tapHelper.tapConfirm();
                }
            }
        }
    }

    /**
     * @return <i>true</i>, if the artifact should be kept meaning the 'continue' button should be tapped
     */
    private boolean keepArtifact() {
        if (!delegate.isRunning()) {
            // Quick exit, when user stopped crawling
            return true;
        }

        List<KeepRule> keepRules = delegate.getKeepRules();
        if (!delegate.isKeepThreeStarArtifacts() && keepRules.isEmpty()) {
            // Quick exit, when no criteria to keep the artifact exists
            Log.d(TAG, "keepArtifact > Quick Exit");
            return false;
        }

        for (int i = 0; i < 2; i++) {
            Log.d(TAG, "keepArtifact > Screenshot for converting item");
            Bitmap bitmap = delegate.getScreenshotHelper().takeScreenshot3();
            OcrHelper.Data data = delegate.getOcrHelper().convertItemScreenshot(bitmap);

            if (data.isComplete()) {
                return tappingThreadHelper.keepingBecauseOfLevel(data) || tappingThreadHelper.keepingBecauseOfRule(data, keepRules);
            }
            // else: Loop again
            Log.d(TAG, "keepArtifact: Second try, neglecting " + data);
        }

        Log.d(TAG, "keepArtifact > Exiting after failing converting the screenshot twice");
        return false;
    }

    /**
     * Delegate providing functionality to {@link TappingThread}, that is delivered from the 'outside'.
     */
    public interface Delegate {

        ScreenshotHelper getScreenshotHelper();

        OcrHelper getOcrHelper();

        /**
         * @return A {@link Point} of/on "5 Artifacts" button
         */
        Point getPoint();

        boolean isRunning();

        boolean isKeepThreeStarArtifacts();

        List<KeepRule> getKeepRules();

    }

}
