package bhg.sucks;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
import java.util.Set;

import bhg.sucks.helper.OcrHelper;
import bhg.sucks.helper.ScreenshotHelper;
import bhg.sucks.model.AmountMatches;
import bhg.sucks.model.KeepRule;
import bhg.sucks.thread.TappingThread;

@RunWith(AndroidJUnit4.class)
public class TestTappingThread {

    private Context appContext;
    private OcrHelper ocrHelper;

    @Before
    public void setup() {
        this.appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        this.ocrHelper = new OcrHelper(appContext);
    }

    @Test
    public void testKeepThreeStarArtifactSwitchOff() {
        // Prepare - Get screenshot
        Bitmap b = bitmapFrom(R.drawable.screenshot4);
        assertNotNull("Bitmap shall not be null", b);

        // Prepare - Analyze screenshot
        OcrHelper.AnalysisResult ar = ocrHelper.analyseScreenshot(b);

        // Prepare - Create TappingThread with 'don't keep 3* artifacts'
        TappingThread t = new TappingThread(createDelegate(false));

        // Execute
        boolean keep = t.keepArtifact(ar.getTextBlocks());

        // Check
        assertThat(keep, is(false));
    }

    @Test
    public void testKeepThreeStarArtifactSwitchOn() {
        // Prepare - Get screenshot
        Bitmap b = bitmapFrom(R.drawable.screenshot4);
        assertNotNull("Bitmap shall not be null", b);

        // Prepare - Analyze screenshot
        OcrHelper.AnalysisResult ar = ocrHelper.analyseScreenshot(b);

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
                return List.of(KeepRule.builder()
                        .skills(Set.of())
                        .amountMatches(AmountMatches.FIVE_OF_FIVE)
                        .build());
            }
        };
    }

    private Bitmap bitmapFrom(int redId) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;

        return BitmapFactory.decodeResource(appContext.getResources(), redId, opts);
    }
}
