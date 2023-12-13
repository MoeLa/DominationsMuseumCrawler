package bhg.sucks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.common.collect.Lists;
import com.google.mlkit.vision.text.Text;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import bhg.sucks.helper.DebugHelper;
import bhg.sucks.helper.OcrHelper;
import bhg.sucks.model.Category;
import bhg.sucks.model.Skill;
import bhg.sucks.util.AndroidTestUtil;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TestOcrHelper {

    private static final String TAG = TestOcrHelper.class.getName();

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
    public void testReceiveGermanStringResource() {
        String s = appContext.getResources().getString(R.string.AirDefenseDamage);
        assertEquals("Not running in German => This test suite won't find nothing!", "Luftabwehr-Schaden", s);
    }

    @Test
    public void testConvertScreenshot() throws InterruptedException {
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.screenshot);
        OcrHelper.Data data = ocrHelper.convertItemScreenshot(textBlocks);

        OcrHelper.Data expected = OcrHelper.Data.builder()
                .category(Category.Jewelry)
                .skills(Lists.newArrayList(Skill.OilLooted, Skill.EnemyAntiTankGunDamage, Skill.OilLooted, Skill.LootedResourceRefund, Skill.AllResourceLooted))
                .level(1)
                .build();

        assertThat(data, equalTo(expected));
    }

    @Test
    public void testConvertScreenshot2() throws InterruptedException {
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.screenshot2);
        OcrHelper.Data data = ocrHelper.convertItemScreenshot(textBlocks);

        OcrHelper.Data expected = OcrHelper.Data.builder()
                .category(Category.WarArmor)
                .skills(Lists.newArrayList(Skill.WarInvadingBazookaDamage, Skill.WarHeavyTankHitpoints, Skill.WarInvadingGeneralsDamage, Skill.WarHeavyCavalryHitpoints, Skill.WarMortarTroopHitpoints))
                .level(1)
                .build();

        assertThat(data, equalTo(expected));
    }

    @Test
    public void testConvertScreenshot3() throws InterruptedException {
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.screenshot3);
        OcrHelper.Data data = ocrHelper.convertItemScreenshot(textBlocks);

        OcrHelper.Data expected = OcrHelper.Data.builder()
                .category(Category.WarArmor)
                .skills(Lists.newArrayList(Skill.WarRangedInfantryHitpoints, Skill.WarInvadingHeavyInfantryDamage, Skill.WarInvadingBazookaDamage, Skill.WarInvadingArmoredCarDamage, Skill.WarAttackHelicoptersHitpoints))
                .level(1)
                .build();

        assertThat(data, equalTo(expected));
    }

    @Test
    public void testConvertScreenshot4() throws InterruptedException {
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.screenshot4);
        OcrHelper.Data data = ocrHelper.convertItemScreenshot(textBlocks);

        OcrHelper.Data expected = OcrHelper.Data.builder()
                .category(Category.WarWeapon)
                .skills(Lists.newArrayList(Skill.WarAttackHelicoptersDamage, Skill.WarInvadingTacticalHelicopterHitpoints, Skill.WarInvadingAttackHelicoptersHitpoints, Skill.WarAPCDamage, Skill.WarInvadingTacticalHelicopterHitpoints))
                .level(3)
                .build();

        assertThat(data, equalTo(expected));
    }

    @Test
    public void testConvertScreenshot5() throws InterruptedException {
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.screenshot5);
        OcrHelper.Data data = ocrHelper.convertItemScreenshot(textBlocks);

        OcrHelper.Data expected = OcrHelper.Data.builder()
                .category(Category.WarEquipment)
                .skills(Lists.newArrayList(Skill.WarAllEnemyDefensiveTowersHitpoints, Skill.WarEnemyMissileSiloDamage, Skill.WarAllDefensiveTowersHitpoints, Skill.WarEnemyRedoubtDamage, Skill.WarAllEnemyDefensiveTowersHitpoints))
                .level(2)
                .build();

        assertThat(data, equalTo(expected));
    }

    @Test
    public void testAnalyseScreenshot_CraftAnimation() throws InterruptedException {
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.screenshot_no_buttons);
        OcrHelper.AnalysisResult ar = ocrHelper.toAnalyseResult(textBlocks);

        assertThat(ar.getScreen(), equalTo(OcrHelper.Screen.ARTIFACT_CRAFT_ANIMATION));
    }

    @Test
    public void testAnalyseScreenshot_ConfirmButton() throws InterruptedException {
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.screenshot_confirm_button);
        OcrHelper.AnalysisResult ar = ocrHelper.toAnalyseResult(textBlocks);

        assertThat(ar.getScreen(), equalTo(OcrHelper.Screen.ARTIFACT_DESTROY_DIALOG));
    }

    @Test
    public void testAnalyseScreenshot_FullyLoaded() throws InterruptedException {
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.screenshot_fully_loaded);
        OcrHelper.AnalysisResult ar = ocrHelper.toAnalyseResult(textBlocks);

        assertThat(ar.getScreen(), equalTo(OcrHelper.Screen.ARTIFACT_FULLY_LOADED));
    }

    @Test
    public void testIsSellAvailable() throws InterruptedException {
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.screenshot_fully_loaded);
        Point actual = ocrHelper.isSellAvailable(textBlocks);

        Point expected = new Point(851, 960);
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void testIsContinueAvailable() throws InterruptedException {
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.screenshot_fully_loaded);
        Point actual = ocrHelper.isContinueAvailable(textBlocks);

        Point expected = new Point(1297, 958);
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void testIsFiveArtifactsAvailable() throws InterruptedException {
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.de_crafting_home);
        Point actual = ocrHelper.isFiveArtifactsAvailable(textBlocks);

        Point expected = new Point(1529, 732);
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void testIsConfirmAvailable() throws InterruptedException {
        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.screenshot_confirm_button);
        Point actual = ocrHelper.isConfirmAvailable(textBlocks);

        Point expected = new Point(1306, 711);
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void testBlubb() throws InterruptedException {
//        List<Text.TextBlock> textBlocks = util.resourceToTextBlocks(R.drawable.test2);
//
//        OcrHelper.Data d = ocrHelper.convertItemScreenshot(textBlocks);
        List<String> names = Lists.newArrayList();
        names.add("before: " + Thread.currentThread().getId());

        Bitmap b = util.bitmapFrom(R.drawable.screenshot_fully_loaded);
        ocrHelper.analyseScreenshot(b, text -> {
            synchronized (names) {
                names.add("success: " + Thread.currentThread().getId());
            }
        }, e -> {
            synchronized (names) {
                names.add("exception: " + Thread.currentThread().getId());
            }
        });

        synchronized (names) {
            names.add("after: " + Thread.currentThread().getId());
        }

        while (names.size() < 3) {
            Thread.sleep(500);
        }

        Log.i("names", names.toString());
    }

}