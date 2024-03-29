package bhg.sucks.thread;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.common.collect.Lists;
import com.google.mlkit.vision.text.Text;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import bhg.sucks.R;
import bhg.sucks.helper.DebugHelper;
import bhg.sucks.helper.OcrHelper;
import bhg.sucks.helper.ScreenshotHelper;
import bhg.sucks.helper.UIHelper;
import bhg.sucks.model.AmountMatches;
import bhg.sucks.model.KeepRule;
import bhg.sucks.model.KeepThreeStarOption;
import bhg.sucks.util.AndroidTestUtil;

@RunWith(AndroidJUnit4.class)
public class TestTappingThreadDe {

    private Context appContext;
    private OcrHelper ocrHelper;
    private DebugHelper debugHelper;
    private AndroidTestUtil util;

    @Before
    public void setup() {
        this.appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        this.debugHelper = new DebugHelper(appContext);
        this.ocrHelper = new OcrHelper(appContext, debugHelper, false);
        this.util = new AndroidTestUtil(appContext, ocrHelper);
    }

    @Test
    public void testReceiveGermanStringResource() {
        String s = appContext.getResources().getString(R.string.AirDefenseDamage);
        assertEquals("Not running in German => This test suite won't find nothing!", "Luftabwehr-Schaden", s);
    }

    @Test
    public void testCraftingHome() throws InterruptedException {
        // Prepare
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.de_crafting_home);

        // Execute
        OcrHelper.AnalysisResult ar = ocrHelper.toAnalyseResult(textBlocks);

        // Check
        assertThat(ar.getScreen(), is(OcrHelper.Screen.ARTIFACT_CRAFTING_HOME));
    }

    @Test
    public void testKeepThreeStarArtifactOptionNo() throws InterruptedException {
        // Prepare
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.screenshot4);
        OcrHelper.AnalysisResult ar = ocrHelper.toAnalyseResult(textBlocks);

        // Prepare - Create TappingThread with 'don't keep 3* artifacts'
        TappingThread t = new TappingThread(createDelegate(KeepThreeStarOption.No));

        // Execute
        boolean keep = t.keepArtifact(ar.getTextBlocks());

        // Check
        assertThat(keep, is(false));
    }

    @Test
    public void testKeepThreeStarArtifactOptionFoodGold() throws InterruptedException {
        // Prepare
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.screenshot4);
        OcrHelper.AnalysisResult ar = ocrHelper.toAnalyseResult(textBlocks);

        // Prepare - Create TappingThread with 'don't keep 3* artifacts'
        TappingThread t = new TappingThread(createDelegate(KeepThreeStarOption.OnlyFoodGold));

        // Execute
        boolean keep = t.keepArtifact(ar.getTextBlocks());

        // Check
        assertThat(keep, is(true));
    }

    @Test
    public void testKeepThreeStarArtifactOptionYes() throws InterruptedException {
        // Prepare
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.screenshot4);
        OcrHelper.AnalysisResult ar = ocrHelper.toAnalyseResult(textBlocks);

        // Prepare - Create TappingThread with 'don't keep 3* artifacts'
        TappingThread t = new TappingThread(createDelegate(KeepThreeStarOption.Yes));

        // Execute
        boolean keep = t.keepArtifact(ar.getTextBlocks());

        // Check
        assertThat(keep, is(true));
    }

    private TappingThread.Delegate createDelegate(KeepThreeStarOption keepThreeStarOption) {
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
            public DebugHelper getDebugHelper() {
                return debugHelper;
            }

            @Override
            public boolean isRunning() {
                return true;
            }

            @Override
            public void setRunning(boolean running) {

            }

            @Override
            public KeepThreeStarOption keepThreeStarOption() {
                return keepThreeStarOption;
            }

            @Override
            public List<KeepRule> getKeepRules() {
                return Lists.newArrayList(KeepRule.builder()
                        .mandatorySkills(UIHelper.createEmptySkillsMap())
                        .optionalSkills(UIHelper.createEmptySkillsMap())
                        .amountMatches(AmountMatches.FIVE_OF_FIVE)
                        .build());
            }

            @Override
            public boolean isDebugMode() {
                return false;
            }
        };
    }

}
