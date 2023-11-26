package bhg.sucks.service.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

/**
 * Taken from <a href="https://lokalise.com/blog/android-app-localization/#Change_Application_Locale_Programmatically">...</a>
 */
public class ContextUtils extends ContextWrapper {

    public ContextUtils(Context base) {
        super(base);
    }

    public static ContextUtils updateLocale(Context context, Locale localeToSwitchTo) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration(); // 1

        LocaleList localeList = new LocaleList(localeToSwitchTo); // 2
        LocaleList.setDefault(localeList); // 3
        configuration.setLocales(localeList); // 4

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            context = context.createConfigurationContext(configuration); // 6
        } else {
            resources.updateConfiguration(configuration, resources.getDisplayMetrics()); // 7
        }

        return new ContextUtils(context); // 8
    }

}
