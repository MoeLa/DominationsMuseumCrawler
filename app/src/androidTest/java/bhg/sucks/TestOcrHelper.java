package bhg.sucks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import bhg.sucks.helper.OcrHelper;
import bhg.sucks.model.Category;
import bhg.sucks.model.Skill;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TestOcrHelper {

    private Context appContext;
    private OcrHelper ocrHelper;

    @Before
    public void setup() {
        this.appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        this.ocrHelper = new OcrHelper(appContext);
    }

    @Test
    public void testReceiveGermanStringResource() {
        String s = appContext.getResources().getString(R.string.AirDefenseDamage);
        assertEquals("Luftabwehr-Schaden", s);
    }

    @Test
    public void testConvertScreenshot() {
        Bitmap b = bitmapFrom(R.drawable.screenshot);
        assertNotNull("Bitmap shall not be null", b);

        OcrHelper.Data data = ocrHelper.convertItemScreenshot(b);

        OcrHelper.Data expected = OcrHelper.Data.builder()
                .category(Category.Jewelry)
                .skills(List.of(Skill.OilLooted, Skill.EnemyAntiTankGunDamage, Skill.OilLooted, Skill.LootedResourceRefund, Skill.AllResourceLooted))
                .level(1)
                .build();

        assertThat(data, equalTo(expected));
    }

    @Test
    public void testConvertScreenshot2() {
        Bitmap b = bitmapFrom(R.drawable.screenshot2);
        assertNotNull("Bitmap shall not be null", b);

        OcrHelper.Data data = ocrHelper.convertItemScreenshot(b);

        OcrHelper.Data expected = OcrHelper.Data.builder()
                .category(Category.WarArmor)
                .skills(List.of(Skill.WarInvadingBazookaDamage, Skill.WarHeavyTankHitpoints, Skill.WarInvadingGeneralsDamage, Skill.WarHeavyCavalryHitpoints, Skill.WarMortarTroopHitpoints))
                .level(1)
                .build();

        assertThat(data, equalTo(expected));
    }

    @Test
    public void testConvertScreenshot3() {
        Bitmap b = bitmapFrom(R.drawable.screenshot3);
        assertNotNull("Bitmap shall not be null", b);

        OcrHelper.Data data = ocrHelper.convertItemScreenshot(b);

        OcrHelper.Data expected = OcrHelper.Data.builder()
                .category(Category.WarArmor)
                .skills(List.of(Skill.WarRangedInfantryHitpoints, Skill.WarInvadingHeavyInfantryDamage, Skill.WarInvadingBazookaDamage, Skill.WarInvadingArmoredCarDamage, Skill.WarAttackHelicoptersHitpoints))
                .level(1)
                .build();

        assertThat(data, equalTo(expected));
    }

    @Test
    public void testConvertScreenshot4() {
        Bitmap b = bitmapFrom(R.drawable.screenshot4);
        assertNotNull("Bitmap shall not be null", b);

        OcrHelper.Data data = ocrHelper.convertItemScreenshot(b);

        OcrHelper.Data expected = OcrHelper.Data.builder()
                .category(Category.WarWeapon)
                .skills(List.of(Skill.WarAttackHelicoptersDamage, Skill.WarInvadingTacticalHelicopterHitpoints, Skill.WarInvadingAttackHelicoptersHitpoints, Skill.WarAPCDamage, Skill.WarInvadingTacticalHelicopterHitpoints))
                .level(3)
                .build();

        assertThat(data, equalTo(expected));
    }

    @Test
    public void testConvertScreenshot5() {
        Bitmap b = bitmapFrom(R.drawable.screenshot5);
        assertNotNull("Bitmap shall not be null", b);

        OcrHelper.Data data = ocrHelper.convertItemScreenshot(b);

        OcrHelper.Data expected = OcrHelper.Data.builder()
                .category(Category.WarEquipment)
                .skills(List.of(Skill.WarAllEnemyDefensiveTowersHitpoints, Skill.WarEnemyMissileSiloDamage, Skill.WarAllDefensiveTowersHitpoints, Skill.WarEnemyRedoubtDamage, Skill.WarAllEnemyDefensiveTowersHitpoints))
                .level(2)
                .build();

        assertThat(data, equalTo(expected));
    }

    @Test
    public void testAnalyseScreenshot_CraftAnimation() {
        Bitmap b = bitmapFrom(R.drawable.screenshot_no_buttons);
        assertNotNull("Bitmap shall not be null", b);

        OcrHelper.AnalysisResult ar = ocrHelper.analyseScreenshot(b);
        assertThat(ar.getScreen(), equalTo(OcrHelper.Screen.ARTIFACT_CRAFT_ANIMATION));
    }

    @Test
    public void testAnalyseScreenshot_ConfirmButton() {
        Bitmap b = bitmapFrom(R.drawable.screenshot_confirm_button);
        assertNotNull("Bitmap shall not be null", b);

        OcrHelper.AnalysisResult ar = ocrHelper.analyseScreenshot(b);
        assertThat(ar.getScreen(), equalTo(OcrHelper.Screen.ARTIFACT_DESTROY_DIALOG));
    }

    @Test
    public void testAnalyseScreenshot_FullyLoaded() {
        Bitmap b = bitmapFrom(R.drawable.screenshot_fully_loaded);
        assertNotNull("Bitmap shall not be null", b);

        OcrHelper.AnalysisResult ar = ocrHelper.analyseScreenshot(b);
        assertThat(ar.getScreen(), equalTo(OcrHelper.Screen.ARTIFACT_FULLY_LOADED));
    }

    @Test
    public void testBlubb() {
        Bitmap b = bitmapFrom(R.drawable.test2);
        assertNotNull("Bitmap shall not be null", b);

        OcrHelper.Data d = ocrHelper.convertItemScreenshot(b);
        System.out.println("Blubb");

    }

    private Bitmap bitmapFrom(int redId) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;

        return BitmapFactory.decodeResource(appContext.getResources(), redId, opts);
    }

}