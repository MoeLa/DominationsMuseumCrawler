package bhg.sucks.thread;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.google.mlkit.vision.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import bhg.sucks.helper.DebugHelper;
import bhg.sucks.helper.OcrHelper;
import bhg.sucks.helper.ScreenshotHelper;
import bhg.sucks.helper.TapHelper;
import bhg.sucks.model.KeepRule;
import bhg.sucks.model.KeepThreeStarOption;

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

    private final DebugHelper debugHelper;

    private final AtomicInteger counter;

    public TappingThread(Delegate d) {
        this.delegate = d;
        this.tapHelper = new TapHelper(d);
        this.tappingThreadHelper = new TappingThreadHelper();
        this.debugHelper = d.getDebugHelper();

        this.counter = new AtomicInteger(0);
    }

    @Override
    public void run() {
        runInternal();
    }

    private void runInternal() {
        if (delegate.isRunning()) {
            Log.d(TAG, "runInternal > Take screenshot");
            Bitmap bitmap = delegate.getScreenshotHelper().takeScreenshot3();
            delegate.getOcrHelper().analyseScreenshot(bitmap, this::onRecognizeSuccess, this::onRecognizeError);
        } else {
            Log.d(TAG, "runInternal > Thread not running anymore");
        }
    }

    private void onRecognizeSuccess(Text text) {
        Log.d(TAG, "onRecognizeSuccess > Analyse texts");
        OcrHelper.AnalysisResult ar = delegate.getOcrHelper().toAnalyseResult(text.getTextBlocks());
        analyzeResult(ar);
    }

    private void onRecognizeError(Exception e) {
        Log.d(TAG, "onRecognizeError > An error occurred");
        e.printStackTrace();
    }

    /**
     * React on a screen that could be captured/read/parsed as wished and the corresponding action
     * has been performed. Meaning: Continue by capturing the next screen.
     */
    private void done() {
        counter.set(0);
        runInternal();
    }

    /**
     * React on a screen that somehow needs to be dismissed and recaptured: Increment the counter
     * and try again. If the counter gets too high, try something else or abort the thread.
     */
    private void repeat(OcrHelper.Screen s) {
        counter.incrementAndGet();

        switch (s) {
            case ARTIFACT_CRAFT_ANIMATION:
                if (counter.get() < 3) {
                    Log.d(TAG, "repeat > ARTIFACT_CRAFT_ANIMATION > Hurry animation");
                    tapHelper.tapHurryAnimation();
                } else if (counter.get() < 6) {
                    Log.d(TAG, "repeat > ARTIFACT_CRAFT_ANIMATION > Tap '5 Artifacts'");
                    tapHelper.tapFiveArtifacts();
                } else {
                    Log.d(TAG, "repeat > ARTIFACT_CRAFT_ANIMATION > Abort thread!");
                    delegate.setRunning(false);
                    return;
                }
                break;
            case ARTIFACT_FULLY_LOADED:
                if (counter.get() < 3) {
                    Log.d(TAG, "repeat > ARTIFACT_FULLY_LOADED > Try again! Counter: " + counter);
                } else {
                    Log.d(TAG, "repeat > ARTIFACT_FULLY_LOADED > Keep artifact! Counter: " + counter);
                    tapHelper.tapContinue();

                    done();
                    return;
                }
                break;
            case COULD_NOT_DETERMINE:
                if (counter.get() < 3) {
                    Log.d(TAG, "repeat > COULD_NOT_DETERMINE > Try again! Counter: " + counter);
                } else {
                    Log.d(TAG, "repeat > COULD_NOT_DETERMINE > Abort thread! Counter: " + counter);
                    delegate.setRunning(false);
                    return;
                }
            default:
                // Nothing
                break;
        }

        runInternal();
    }

    /**
     * Decide on whether the screen which tap(s) to be made.
     *
     * @param ar {@link bhg.sucks.helper.OcrHelper.AnalysisResult} providing the current screen and its texts.
     */
    @VisibleForTesting
    void analyzeResult(OcrHelper.AnalysisResult ar) {
        switch (ar.getScreen()) {
            case ARTIFACT_CRAFTING_HOME:
                Log.d(TAG, "analyzeResult > ARTIFACT_CRAFTING_HOME > Tap '5 Artifacts'");
                tapHelper.tapFiveArtifacts();

                done();
                break;
            case ARTIFACT_CRAFT_ANIMATION:
                Log.d(TAG, "analyzeResult > ARTIFACT_CRAFT_ANIMATION > Counter: " + counter);

                repeat(OcrHelper.Screen.ARTIFACT_CRAFT_ANIMATION);
                break;
            case ARTIFACT_FULLY_LOADED:
                Boolean keepArtifact = keepArtifact(ar.getTextBlocks());
                if (keepArtifact == null) {
                    Log.d(TAG, "analyzeResult > ARTIFACT_FULLY_LOADED > Repeat! Counter: " + counter);

                    repeat(OcrHelper.Screen.ARTIFACT_FULLY_LOADED);
                } else if (keepArtifact) {
                    Log.d(TAG, "analyzeResult > ARTIFACT_FULLY_LOADED > Tap 'Continue'");
                    tapHelper.tapContinue();

                    done();
                } else {
                    Log.d(TAG, "analyzeResult > ARTIFACT_FULLY_LOADED > Sell artifact");
                    tapHelper.tapSell();
                    tapHelper.tapConfirm();

                    done();
                }
                break;
            case ARTIFACT_DESTROY_DIALOG:
                Log.d(TAG, "analyzeResult > ARTIFACT_DESTROY_DIALOG > Tap 'Confirm'");
                tapHelper.tapConfirm();

                done();
                break;
            case COULD_NOT_DETERMINE: // Intentionally jump into default case
            default:
                if (counter.get() < 3) {
                    Log.d(TAG, "analyzeResult > Screen could not be determined. Repeat! Counter: " + counter);
                    repeat(OcrHelper.Screen.COULD_NOT_DETERMINE);
                } else {
                    Log.d(TAG, "analyzeResult > Screen could not be determined. Stop thread! Counter: " + counter);
                    delegate.setRunning(false);
                }
                break;
        }
    }

    /**
     * @return <i>true</i>, if the artifact should be kept meaning the 'continue' button should be tapped<br/>
     * <i>false</i>, if the artifact should be dismissed meaning the 'sell' button should be tapped<br/>
     * <i>null</i>, if the artifact's data was read completely meaning it should be read again
     */
    @VisibleForTesting
    public Boolean keepArtifact(List<Text.TextBlock> textBlocks) {
        if (!delegate.isRunning()) {
            // Quick exit, when user stopped crawling
            return null;
        }

        List<KeepRule> keepRules = delegate.getKeepRules();
        if (delegate.keepThreeStarOption() == KeepThreeStarOption.No && keepRules.isEmpty()) {
            // Quick exit, when no criteria to keep the artifact exists
            Log.d(TAG, "keepArtifact > Quick Exit");
            return false;
        }

        OcrHelper.Data data = delegate.getOcrHelper().convertItemScreenshot(textBlocks);
        if (data.isComplete()) {
            if (tappingThreadHelper.keepingBecauseOfLevel(data, delegate.keepThreeStarOption())) {
                return true;
            }

            Optional<KeepRule> reasonToKeep = tappingThreadHelper.keepingBecauseOfRule(data, keepRules);
            if (reasonToKeep.isPresent()) {
                if (delegate.isDebugMode()) {
                    debugHelper.logKeepArtefact(data, reasonToKeep.get());
                }
                return true;
            }

            return false;
        }

        Log.d(TAG, "keepArtifact > Data incomplete, try again => " + data);
        return null;
    }

    /**
     * Delegate providing functionality to {@link TappingThread}, that is delivered from the 'outside'.
     */
    public interface Delegate {

        ScreenshotHelper getScreenshotHelper();

        OcrHelper getOcrHelper();

        DebugHelper getDebugHelper();

        boolean isRunning();

        void setRunning(boolean running);

        KeepThreeStarOption keepThreeStarOption();

        List<KeepRule> getKeepRules();

        boolean isDebugMode();
    }

}
