package bhg.sucks;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.common.collect.Lists;
import com.google.mlkit.vision.text.Text;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import bhg.sucks.helper.OcrHelper;
import bhg.sucks.helper.ScreenshotHelper;
import bhg.sucks.helper.UIHelper;
import bhg.sucks.model.AmountMatches;
import bhg.sucks.model.KeepRule;
import bhg.sucks.thread.TappingThread;
import bhg.sucks.util.AndroidTestUtil;

@RunWith(AndroidJUnit4.class)
public class TestTappingThread {

    private Context appContext;
    private OcrHelper ocrHelper;
    private AndroidTestUtil util;

    @Before
    public void setup() {
        this.appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        this.ocrHelper = new OcrHelper(appContext);
        this.util = new AndroidTestUtil(appContext, ocrHelper);
    }

    @Test
    public void testKeepThreeStarArtifactSwitchOff() throws InterruptedException {
        // Prepare
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.screenshot4);
        OcrHelper.AnalysisResult ar = ocrHelper.toAnalyseResult(textBlocks);

        // Prepare - Create TappingThread with 'don't keep 3* artifacts'
        TappingThread t = new TappingThread(createDelegate(false));

        // Execute
        boolean keep = t.keepArtifact(ar.getTextBlocks());

        // Check
        assertThat(keep, is(false));
    }

    @Test
    public void testKeepThreeStarArtifactSwitchOn() throws InterruptedException {
        // Prepare
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.screenshot4);
        OcrHelper.AnalysisResult ar = ocrHelper.toAnalyseResult(textBlocks);

        // Prepare - Create TappingThread with 'don't keep 3* artifacts'
        TappingThread t = new TappingThread(createDelegate(true));

        // Execute
        boolean keep = t.keepArtifact(ar.getTextBlocks());

        // Check
        assertThat(keep, is(true));
    }

    private TappingThread.Delegate createDelegate(boolean keepThreeStarArtifacts) {
        return new TappingThread.Delegate() {
            @Override
            public ScreenshotHelper getScreenshotHelper() {
                return new ScreenshotHelper(appContext);
            }

            @Override
            public OcrHelper getOcrHelper() {
                return ocrHelper;
            }

            @Override
            public boolean isRunning() {
                return true;
            }

            @Override
            public void setRunning(boolean running) {

            }

            @Override
            public boolean isKeepThreeStarArtifacts() {
                return keepThreeStarArtifacts;
            }

            @Override
            public List<KeepRule> getKeepRules() {
                return Lists.newArrayList(KeepRule.builder()
                        .mandatorySkills(UIHelper.createEmptySkillsMap())
                        .optionalSkills(UIHelper.createEmptySkillsMap())
                        .amountMatches(AmountMatches.FIVE_OF_FIVE)
                        .build());
            }
        };
    }

}
