package bhg.sucks.thread;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.graphics.Point;

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
import bhg.sucks.model.Category;
import bhg.sucks.model.Skill;
import bhg.sucks.util.AndroidTestUtil;

@RunWith(AndroidJUnit4.class)
public class TestTappingThreadEn {

    private Context appContext;
    private OcrHelper ocrHelper;
    private AndroidTestUtil util;

    @Before
    public void setup() {
        this.appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        this.ocrHelper = new OcrHelper(appContext, new DebugHelper(appContext), false);
        this.util = new AndroidTestUtil(appContext, ocrHelper);
    }

    @Test
    public void testReceiveEnglishStringResource() {
        String s = appContext.getResources().getString(R.string.AirDefenseDamage);
        assertEquals("Not running in English => This test suite won't find nothing!", "Air Defense Damage", s);
    }

    @Test
    public void testDetermineScreenDestroyArtifactDialog() throws InterruptedException {
        // Prepare
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.en_confirm_sell_dialog);

        // Execute
        OcrHelper.AnalysisResult ar = ocrHelper.toAnalyseResult(textBlocks);

        // Check
        assertThat(ar.getScreen(), is(OcrHelper.Screen.ARTIFACT_DESTROY_DIALOG));
    }

    @Test
    public void testIsConfirmAvailable() throws InterruptedException {
        // Prepare
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.en_confirm_sell_dialog);
        Point p = ocrHelper.isConfirmAvailable(textBlocks);

        // Check
        assertThat(p, is(notNullValue()));
    }

    @Test
    public void testConvertEnArtifactPottery() throws InterruptedException {
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.en_artifact_pottery);
        OcrHelper.Data data = ocrHelper.convertItemScreenshot(textBlocks);

        OcrHelper.Data expected = OcrHelper.Data.builder()
                .category(Category.Pottery)
                .skills(Lists.newArrayList(Skill.LibraryTechFoodCost, Skill.MissileSiloHitpoints, Skill.MissileSiloDamage, Skill.AirDefenseDamage, Skill.RedoubtHitpoints))
                .level(1)
                .build();

        assertThat(data, equalTo(expected));
    }

}
