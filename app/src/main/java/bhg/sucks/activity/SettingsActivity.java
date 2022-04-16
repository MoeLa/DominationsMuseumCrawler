package bhg.sucks.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity;

import java.util.Locale;

import bhg.sucks.R;
import bhg.sucks.model.KeepThreeStarOption;

public class SettingsActivity extends LocaleAwareCompatActivity {

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.settingsTopAppBar);
        setSupportActionBar(toolbar);

        this.sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

        // Init language
        Locale l = Locale.getDefault();
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
            if (checkedId == R.id.settingsLanguageEnglish) {
                updateLocale(Locale.US);
            } else if (checkedId == R.id.settingsLanguageGerman) {
                updateLocale(Locale.GERMANY);
            }
            recreate();
        });

        // Init debug mode switch
        final SwitchCompat switchDebugMode = findViewById(R.id.settingsDebugMode);
        switchDebugMode.setChecked(sharedPref.getBoolean(getString(R.string.debug_mode), false));
        switchDebugMode.setOnCheckedChangeListener((btnView, isChecked) -> sharedPref.edit()
                .putBoolean(getString(R.string.debug_mode), isChecked)
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