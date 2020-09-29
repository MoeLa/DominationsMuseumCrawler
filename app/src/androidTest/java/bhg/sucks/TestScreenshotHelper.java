package bhg.sucks;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import bhg.sucks.helper.ScreenshotHelper;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class TestScreenshotHelper {

    private Context appContext;
    private ScreenshotHelper screenshotHelper;

    @Before
    public void setup() {
        this.appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        this.screenshotHelper = new ScreenshotHelper(appContext);
    }

    @Test
    public void testBenchmarkTakeScreenshot() {
        Bitmap b1 = screenshotHelper.takeScreenshot3();
        assertNotNull("No bitmap received", b1);
        Bitmap b = screenshotHelper.takeScreenshot();
        assertNotNull("No bitmap received", b);

    }

}
