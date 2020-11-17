package bhg.sucks.helper;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import bhg.sucks.thread.TappingThread;

/**
 * Encapsulates all tap actions.
 */
public class TapHelper {

    private final static String hurryAnimationCommand = "input tap 1 1";
    private final static String tapCommand = "input tap %s %s";
    private static final String TAG = "TapHelper";

    private final TappingThread.Delegate delegate;
    private final ScreenshotHelper screenshotHelper;
    private final OcrHelper ocrHelper;
    private final ExecuteAsRootBase fiveArtifactsExecutor;

    private ExecuteAsRootBase sellExecutor;
    private ExecuteAsRootBase confirmExecutor;
    private ExecuteAsRootBase continueExecutor;

    public TapHelper(TappingThread.Delegate delegate) {
        this.delegate = delegate;
        this.screenshotHelper = delegate.getScreenshotHelper();
        this.ocrHelper = delegate.getOcrHelper();

        // Note: Concat command here, because the point might be collected by GC
        Point p = delegate.getPoint();
        final String fiveArtifactsCommand = String.format(tapCommand, p.x, p.y);

        this.fiveArtifactsExecutor = new ExecuteAsRootBase() {

            @Override
            protected List<String> getCommandsToExecute() {
                List<String> result = new ArrayList<>();
                result.add(fiveArtifactsCommand);
                result.add(hurryAnimationCommand);
                result.add(hurryAnimationCommand);
                result.add(hurryAnimationCommand);
                return result;
            }

        };
    }

    /**
     * Taps the button "5 Artifacts" and three more times to hurry the following animation.
     *
     * @return <i>true</i>, if the operation could be executed
     */
    public boolean tapFiveArtifacts() {
        if (!delegate.isRunning()) {
            return false;
        }

        Bitmap b = screenshotHelper.takeScreenshot3();
        Point p = ocrHelper.isFiveArtifactsAvailable(b);
        if (p == null) {
            Log.d(TAG, "tapFiveArtifacts > Did not find '475' text");
            return false;
        }

        return fiveArtifactsExecutor.execute();
    }

    /**
     * Taps the button "Sell".
     *
     * @return <i>true</i>, if the operation could be executed
     */
    public boolean tapSell() {
        if (!delegate.isRunning()) {
            return false;
        }

        if (sellExecutor == null) {
            Log.d(TAG, "tapSell > Screenshot for calculating bounds");
            Bitmap b = screenshotHelper.takeScreenshot3();
            Point p = ocrHelper.isSellAvailable(b);
            if (p == null) {
                Log.d(TAG, "tapSell > Did not find 'Sell for' text");
                return false;
            }

            // Note: Concat command here, because p might be collected by GC
            final String sellCommand = String.format(tapCommand, p.x, p.y);

            this.sellExecutor = new ExecuteAsRootBase() {

                @Override
                protected List<String> getCommandsToExecute() {
                    List<String> result = new ArrayList<>();
                    result.add(sellCommand);
                    return result;
                }

            };
        }

        return sellExecutor.execute();
    }

    /**
     * Taps the button "Confirm" and hurries the following animation.
     *
     * @return <i>true</i>, if the operation could be executed
     */
    public boolean tapConfirm() {
        if (!delegate.isRunning()) {
            return false;
        }

        if (confirmExecutor == null) {
            Log.d(TAG, "tapConfirm > Screenshot for calculating bounds");
            Bitmap b = screenshotHelper.takeScreenshot3();
            Point p = ocrHelper.isConfirmAvailable(b);
            if (p == null) {
                Log.d(TAG, "tapSell > Did not find 'Confirm' text");
                return false;
            }

            // Note: Concat command here, because p might be collected by GC
            final String confirmCommand = String.format(tapCommand, p.x, p.y);

            this.confirmExecutor = new ExecuteAsRootBase() {

                @Override
                protected List<String> getCommandsToExecute() {
                    List<String> result = new ArrayList<>();
                    result.add(confirmCommand);
                    result.add(hurryAnimationCommand);
                    result.add(hurryAnimationCommand);
                    result.add(hurryAnimationCommand);
                    return result;
                }

            };
        }

        return confirmExecutor.execute();
    }

    /**
     * Taps the button "Continue" and hurries the following animation.
     *
     * @return <i>true</i>, if the operation could be executed
     */
    public boolean tapContinue() {
        if (!delegate.isRunning()) {
            return false;
        }

        if (continueExecutor == null) {
            Log.d(TAG, "tapContinue > Screenshot for calculating bounds");
            Bitmap b = screenshotHelper.takeScreenshot3();
            Point p = ocrHelper.isContinueAvailable(b);
            if (p == null) {
                Log.d(TAG, "tapSell > Did not find 'Continue' text");
                return false;
            }

            // Note: Concat command here, because p might be collected by GC
            final String continueCommand = String.format(tapCommand, p.x, p.y);

            this.continueExecutor = new ExecuteAsRootBase() {

                @Override
                protected List<String> getCommandsToExecute() {
                    List<String> result = new ArrayList<>();
                    result.add(continueCommand);
                    result.add(hurryAnimationCommand);
                    result.add(hurryAnimationCommand);
                    result.add(hurryAnimationCommand);
                    return result;
                }

            };
        }

        return continueExecutor.execute();
    }

    /**
     * @return <i>true</i>, if we could return to the 'five artifacts' screen
     */
    public boolean rescueProcess(BooleanSupplier keepArtifact) {
        for (int i = 0; i < 5; i++) {
            Bitmap b = screenshotHelper.takeScreenshot3();
            if (i != 0 && ocrHelper.isFiveArtifactsAvailable(b) != null) {
                Log.d(TAG, "rescueProcess > Could rescue process with " + i + " actions.");
                return true;
            }

            if (ocrHelper.isConfirmAvailable(b) != null) {
                tapConfirm();
            } else if (ocrHelper.isContinueAvailable(b) != null) {
                if (keepArtifact.getAsBoolean()) {
                    tapContinue();
                } else {
                    tapSell();
                    tapConfirm();
                }
            }
        }

        Log.i(TAG, "rescueProcess > Could not rescue process");
        return false;
    }

}
