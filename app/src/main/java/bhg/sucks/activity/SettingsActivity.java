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

public class SettingsActivity extends LocaleAwareCompatActivity {

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.settingsTopAppBar);
        setSupportActionBar(toolbar);

        this.sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

        if (Locale.GERMANY.equals(Locale.getDefault())) {
            RadioButton rbGerman = findViewById(R.id.settingsLanguageGerman);
            rbGerman.setChecked(true);
        } else {
            RadioButton rbGerman = findViewById(R.id.settingsLanguageEnglish);
            rbGerman.setChecked(true);
        }

        final RadioGroup languageGroup = findViewById(R.id.settingsLanguageGroup);
        languageGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.settingsLanguageEnglish) {
                updateLocale(Locale.US);
            } else if (checkedId == R.id.settingsLanguageGerman) {
                updateLocale(Locale.GERMANY);
            }
        });

        final SwitchCompat switchDebugMode = findViewById(R.id.settingsDebugMode);
        switchDebugMode.setChecked(sharedPref.getBoolean(getString(R.string.debug_mode), false));
        switchDebugMode.setOnCheckedChangeListener((btnView, isChecked) -> sharedPref.edit()
                .putBoolean(getString(R.string.debug_mode), isChecked)
                .apply());

        final SwitchCompat switchKeepThreeStarArtifacts = findViewById(R.id.settingsKeep3StarArtifacts);
        switchKeepThreeStarArtifacts.setChecked(sharedPref.getBoolean(getString(R.string.keep_3_artifacts), false));
        switchKeepThreeStarArtifacts.setOnCheckedChangeListener((btnView, isChecked) -> sharedPref.edit()
                .putBoolean(getString(R.string.keep_3_artifacts), isChecked)
                .apply());
    }
}