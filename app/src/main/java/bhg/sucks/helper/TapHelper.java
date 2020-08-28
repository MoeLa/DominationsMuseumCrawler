package bhg.sucks.helper;

import android.graphics.Bitmap;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

import bhg.sucks.thread.MyThread;

/**
 * Encapsulates all tap actions.
 */
public class TapHelper {

    private final static String TAG = "TapHelper";
    private final static String hurryAnimationCommand = "input tap 1 1"; // "input tap 1900 200"
    private final static String tapCommand = "input tap %s %s";
    // private final static String fiveArtifactsCommand = "input tap 1550 745";
    // private final static String sellCommand = "input tap 870 960";
    // private final static String confirmCommand = "input tap 1300 715";

    private final MyThread.Delegate delegate;
    private final ScreenshotHelper screenshotHelper;
    private final OcrHelper ocrHelper;
    private final ExecuteAsRootBase fiveArtifactsExecutor;

    private ExecuteAsRootBase sellExecutor;
    private ExecuteAsRootBase confirmExecutor;
    private ExecuteAsRootBase continueExecutor;

    public TapHelper(MyThread.Delegate delegate) {
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

        boolean ret = fiveArtifactsExecutor.execute();
        return ret;
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
            Bitmap b = screenshotHelper.takeScreenshot();
            Point p = ocrHelper.isSellAvailable(b);
            if (p == null) {
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

        boolean ret = sellExecutor.execute();
        return ret;
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
            Bitmap b = screenshotHelper.takeScreenshot();
            Point p = ocrHelper.isConfirmAvailable(b);
            if (p == null) {
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
            Bitmap b = screenshotHelper.takeScreenshot();
            Point p = ocrHelper.isContinueAvailable(b);
            if (p == null) {
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

}
