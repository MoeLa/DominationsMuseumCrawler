package bhg.sucks.helper;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import bhg.sucks.thread.TappingThread;

/**
 * Encapsulates all tap actions.
 */
public class TapHelper {

    private final static String hurryAnimationCommand = "input tap 50 50";
    private final static String tapCommand = "input tap %s %s";
    private static final String TAG = "TapHelper";

    private final TappingThread.Delegate delegate;
    private final ScreenshotHelper screenshotHelper;
    private final OcrHelper ocrHelper;

    private ExecuteAsRootBase fiveArtifactsExecutor;
    private ExecuteAsRootBase sellExecutor;
    private ExecuteAsRootBase confirmExecutor;
    private ExecuteAsRootBase continueExecutor;
    private ExecuteAsRootBase nopExecutor;

    public TapHelper(TappingThread.Delegate delegate) {
        this.delegate = delegate;
        this.screenshotHelper = delegate.getScreenshotHelper();
        this.ocrHelper = delegate.getOcrHelper();
    }

    /**
     * Taps the button "5 Artifacts" and four more times in the corner to hurry the following animation.
     */
    public void tapFiveArtifacts() {
        if (!delegate.isRunning()) {
            return;
        }

        if (fiveArtifactsExecutor == null) {
            Log.d(TAG, "tapFiveArtifacts > Screenshot for calculating bounds");
            Bitmap b = screenshotHelper.takeScreenshot3();
            ocrHelper.isFiveArtifactsAvailable(b, this::setAndTapFiveArtifacts);
        } else {
            fiveArtifactsExecutor.execute();
        }
    }

    private void setAndTapFiveArtifacts(Point p) {
        if (p == null) {
            Log.d(TAG, "tapFiveArtifacts > Did not find '5 Artifacts' text");
            return;
        }

        // Note: Concat command here, because the point might be collected by GC
        final String fiveArtifactsCommand = String.format(tapCommand, p.x, p.y);

        this.fiveArtifactsExecutor = new ExecuteAsRootBase() {

            @Override
            protected List<String> getCommandsToExecute() {
                List<String> result = new ArrayList<>();
                result.add(fiveArtifactsCommand);
                result.add(hurryAnimationCommand);
                result.add(hurryAnimationCommand);
                result.add(hurryAnimationCommand);
                result.add(hurryAnimationCommand);
                return result;
            }

        };

        fiveArtifactsExecutor.execute();
    }

    /**
     * Taps the button "Sell".
     */
    public void tapSell() {
        if (!delegate.isRunning()) {
            return;
        }

        if (sellExecutor == null) {
            Log.d(TAG, "tapSell > Screenshot for calculating bounds");
            Bitmap b = screenshotHelper.takeScreenshot3();
            ocrHelper.isSellAvailable(b, this::setAndTapsSell);
        } else {
            sellExecutor.execute();
        }
    }

    private void setAndTapsSell(Point p) {
        if (p == null) {
            Log.d(TAG, "tapSell > Did not find 'Sell for' text");
            return;
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

        sellExecutor.execute();
    }

    /**
     * Taps the button "Confirm" and hurries the following animation by tapping three times in the corner.
     */
    public void tapConfirm() {
        if (!delegate.isRunning()) {
            return;
        }

        if (confirmExecutor == null) {
            Log.d(TAG, "tapConfirm > Screenshot for calculating bounds");
            Bitmap b = screenshotHelper.takeScreenshot3();
            ocrHelper.isConfirmAvailable(b, this::setAndTapConfirm);
        } else {
            confirmExecutor.execute();
        }
    }

    private void setAndTapConfirm(Point p) {
        if (p == null) {
            Log.d(TAG, "tapSell > Did not find 'Confirm' text");
            return;
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

        confirmExecutor.execute();
    }

    /**
     * Taps the button "Continue" and hurries the following animation by tapping three times in the corner.
     */
    public void tapContinue() {
        if (!delegate.isRunning()) {
            return;
        }

        if (continueExecutor == null) {
            Log.d(TAG, "tapContinue > Screenshot for calculating bounds");
            Bitmap b = screenshotHelper.takeScreenshot3();
            ocrHelper.isContinueAvailable(b, this::setAndTapContinue);
        } else {
            continueExecutor.execute();
        }
    }

    private void setAndTapContinue(Point p) {
        if (p == null) {
            Log.d(TAG, "tapSell > Did not find 'Continue' text");
            return;
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

        continueExecutor.execute();
    }

    /**
     * Hurries the animation by tapping three times in the corner.
     */
    public boolean tapHurryAnimation() {
        if (!delegate.isRunning()) {
            return false;
        }

        if (nopExecutor == null) {
            this.nopExecutor = new ExecuteAsRootBase() {

                @Override
                protected List<String> getCommandsToExecute() {
                    List<String> result = new ArrayList<>();
                    result.add(hurryAnimationCommand);
                    result.add(hurryAnimationCommand);
                    result.add(hurryAnimationCommand);
                    return result;
                }

            };
        }

        return nopExecutor.execute();
    }
}
