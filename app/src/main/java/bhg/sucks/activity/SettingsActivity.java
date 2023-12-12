package bhg.sucks.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Locale;

import bhg.sucks.R;
import bhg.sucks.helper.DebugHelper;
import bhg.sucks.model.KeepThreeStarOption;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.settingsTopAppBar);
        setSupportActionBar(toolbar);

        this.sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

        // Init language
        Locale l = getResources().getConfiguration().getLocales().get(0);
        if (Locale.GERMANY.equals(l)) {
            RadioButton rbGerman = findViewById(R.id.settingsLanguageGerman);
            rbGerman.setChecked(true);
        } else {
            RadioButton rbEnglish = findViewById(R.id.settingsLanguageEnglish);
            rbEnglish.setChecked(true);
        }

        // Set language change listener
        final RadioGroup languageGroup = findViewById(R.id.settingsLanguageGroup);
        languageGroup.setOnCheckedChangeListener((group, checkedId) -> {
            LocaleListCompat newLocale = AppCompatDelegate.getApplicationLocales();
            if (checkedId == R.id.settingsLanguageEnglish) {
                newLocale = LocaleListCompat.create(Locale.US);
            } else if (checkedId == R.id.settingsLanguageGerman) {
                newLocale = LocaleListCompat.create(Locale.GERMANY);
            }
            AppCompatDelegate.setApplicationLocales(newLocale);
        });

        // Init debug mode switch
        final SwitchCompat switchDebugMode = findViewById(R.id.settingsDebugMode);
        switchDebugMode.setChecked(sharedPref.getBoolean(DebugHelper.DEBUG_MODE_KEY, false));
        switchDebugMode.setOnCheckedChangeListener((btnView, isChecked) -> sharedPref.edit()
                .putBoolean(DebugHelper.DEBUG_MODE_KEY, isChecked)
                .apply());

        // Init 'keep three star artifact' options
        String savedString = sharedPref.getString(KeepThreeStarOption.SHARED_PREF_KEY, KeepThreeStarOption.No.name());
        KeepThreeStarOption savedOption = KeepThreeStarOption.valueOf(savedString);
        switch (savedOption) {
            case No:
                final RadioButton rbNo = findViewById(R.id.settingsKeep3No);
                rbNo.setChecked(true);
                break;
            case OnlyFoodGold:
                final RadioButton rbFoodGold = findViewById(R.id.settingsKeep3FoodGold);
                rbFoodGold.setChecked(true);
                break;
            case Yes:
                final RadioButton rbYes = findViewById(R.id.settingsKeep3Yes);
                rbYes.setChecked(true);
                break;
        }

        // Set change listener for 'keep three star artifact' options
        final RadioGroup keepThreeOptionGroup = findViewById(R.id.settingsKeep3Group);
        keepThreeOptionGroup.setOnCheckedChangeListener((group, checkedId) -> {
            KeepThreeStarOption option;
            if (checkedId == R.id.settingsKeep3No) {
                option = KeepThreeStarOption.No;
            } else if (checkedId == R.id.settingsKeep3FoodGold) {
                option = KeepThreeStarOption.OnlyFoodGold;
            } else {
                option = KeepThreeStarOption.Yes;
            }
            sharedPref.edit()
                    .putString(KeepThreeStarOption.SHARED_PREF_KEY, option.name())
                    .apply();
        });
    }
}