package bhg.sucks.thread;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.text.TextBlock;

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
        int hurryAnimationCounter = 0;

        while (delegate.isRunning()) {
            Log.d(TAG, "run > Screenshot to analyse next step");
            Bitmap bitmap = delegate.getScreenshotHelper().takeScreenshot3();
            OcrHelper.AnalysisResult ar = delegate.getOcrHelper().analyseScreenshot(bitmap);

            switch (ar.getScreen()) {
                case ARTIFACT_CRAFTING_HOME:
                    Log.d(TAG, "run > Tap '5 Artifacts'");
                    tapHelper.tapFiveArtifacts();
                    break;
                case ARTIFACT_CRAFT_ANIMATION:
                    hurryAnimationCounter++;
                    Log.d(TAG, "run > Hurry animation. Counter: " + hurryAnimationCounter);
                    if (hurryAnimationCounter < 3) {
                        boolean b = tapHelper.tapHurryAnimation();
                        Log.d(TAG, "run > Hurry animation > result: " + b);
                    } else {
                        hurryAnimationCounter = 0;
                        Log.d(TAG, "run > Try alternative: Tap '5 Artifacts'");
                        tapHelper.tapFiveArtifacts();
                    }
                    break;
                case ARTIFACT_FULLY_LOADED:
                    if (keepArtifact(ar.getTextBlocks())) {
                        Log.d(TAG, "run > Tap 'Continue'");
                        tapHelper.tapContinue();
                    } else {
                        Log.d(TAG, "run > Sell artifact");
                        tapHelper.tapSell();
                        tapHelper.tapConfirm();
                    }
                    break;
                case ARTIFACT_DESTROY_DIALOG:
                    Log.d(TAG, "run > Tap 'Confirm'");
                    tapHelper.tapConfirm();
                    break;
                default:
                    Log.d(TAG, "run > Exiting loop, since screen could not be detected.");
                    delegate.setRunning(false);
                    break;
            }
        }
    }

    /**
     * @return <i>true</i>, if the artifact should be kept meaning the 'continue' button should be tapped
     */
    private boolean keepArtifact(SparseArray<TextBlock> textBlocks) {
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

        // First try with detected text blocks from parameter
        OcrHelper.Data data = delegate.getOcrHelper().convertItemScreenshot(textBlocks);
        if (data.isComplete()) {
            return tappingThreadHelper.keepingBecauseOfLevel(data) || tappingThreadHelper.keepingBecauseOfRule(data, keepRules);
        }

        // Some more tries with new screenshots, since animation might have blocked some text
        for (int i = 0; i < 3; i++) {
            Log.d(TAG, "keepArtifact > Screenshot for converting item");
            Bitmap bitmap = delegate.getScreenshotHelper().takeScreenshot3();
            data = delegate.getOcrHelper().convertItemScreenshot(bitmap);

            if (data.isComplete()) {
                return tappingThreadHelper.keepingBecauseOfLevel(data) || tappingThreadHelper.keepingBecauseOfRule(data, keepRules);
            }
            // else: Loop again
            Log.d(TAG, "keepArtifact: Next try, neglecting " + data);
        }

        Log.d(TAG, "keepArtifact > Exiting after failing converting the screenshot three times");
        return false;
    }

    /**
     * Delegate providing functionality to {@link TappingThread}, that is delivered from the 'outside'.
     */
    public interface Delegate {

        ScreenshotHelper getScreenshotHelper();

        OcrHelper getOcrHelper();

        boolean isRunning();

        void setRunning(boolean running);

        boolean isKeepThreeStarArtifacts();

        List<KeepRule> getKeepRules();

    }

}
