package bhg.sucks.thread;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import java.util.List;

import bhg.sucks.helper.OcrHelper;
import bhg.sucks.helper.ScreenshotHelper;
import bhg.sucks.helper.TapHelper;
import bhg.sucks.model.KeepRule;
import bhg.sucks.model.Skill;

public class MyThread extends Thread {

    private static final String TAG = MyThread.class.getName();
    private final Delegate delegate;
    private final TapHelper tapHelper;

    public MyThread(Delegate d) {
        this.delegate = d;
        this.tapHelper = new TapHelper(d);
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
            return false;
        }

        for (int i = 0; i < 2; i++) {
            Bitmap bitmap = delegate.getScreenshotHelper().takeScreenshot3();
            OcrHelper.Data data = delegate.getOcrHelper().convertItemScreenshot(bitmap);

            if (data.isComplete()) {
                return keepingBecauseOfLevel(data) || keepingBecauseOfRule(data, keepRules);
            }
            // else: Loop again
            Log.d(TAG, "keepArtifact: Second try, neglecting " + data);
        }

        return false;
    }

    /**
     * @return <i>true</i>, if data.level is &gt;=3
     */
    private boolean keepingBecauseOfLevel(OcrHelper.Data data) {
        return data.getLevel() >= 3;
    }

    /**
     * @return <i>true</i>, if data.skills matches any keep rule
     */
    private boolean keepingBecauseOfRule(OcrHelper.Data data, List<KeepRule> keepRules) {
        for (KeepRule keepRule : keepRules) {
            int matches = 0;
            for (Skill s : data.getSkills()) {
                if (keepRule.getSkills().contains(s)) {
                    matches++;
                }
            }

            if (matches >= keepRule.getAmountMatches().ordinal() + 1) {
                return true;
            }

        }

        return false;
    }

    /**
     * Delegate providing functionality to {@link MyThread}, that is delivered from the 'outside'.
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
