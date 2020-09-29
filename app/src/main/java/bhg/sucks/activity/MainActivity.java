package bhg.sucks.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity;

import java.util.List;
import java.util.Locale;

import bhg.sucks.converter.SpinnerConverter;
import bhg.sucks.dao.KeepRuleDAO;
import bhg.sucks.model.KeepRule;
import bhg.sucks.service.OverlayIconService;
import bhg.sucks.so.we.need.a.dominationsmuseumcrawler.R;

public class MainActivity extends LocaleAwareCompatActivity implements AdapterView.OnItemSelectedListener {

    private KeepRuleDAO dao;
    private SharedPreferences sharedPref;
    private List<KeepRule> keepRules;
    private KeepRulesAdapter rvAdapter;
    private ActivityResultLauncher<String> startCreateKeepRuleActivity = registerForActivityResult(new KeepRuleContract(), new ActivityResultCallback<String>() {

        @Override
        public void onActivityResult(String keepRuleId) {
            KeepRule keepRule = dao.get(keepRuleId);
            if (keepRule.getPosition() == keepRules.size()) {
                // New rule created/added
                keepRules.add(keepRule);
                rvAdapter.notifyItemInserted(keepRule.getPosition());
            } else {
                // Rule updated
                keepRules.set(keepRule.getPosition(), keepRule);
                rvAdapter.notifyItemChanged(keepRule.getPosition());
            }
        }

    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        this.dao = new KeepRuleDAO(sharedPref);

        final SwitchCompat switchKeepThreeStarArtifacts = findViewById(R.id.switchKeep3StarArtifacts);
        switchKeepThreeStarArtifacts.setChecked(sharedPref.getBoolean(getString(R.string.keep_3_artifacts), false));
        switchKeepThreeStarArtifacts.setOnCheckedChangeListener((btnView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.keep_3_artifacts), isChecked);
            editor.apply();
        });

        Spinner langSpinner = findViewById(R.id.inpLang);
        langSpinner.setSelection(SpinnerConverter.toInt(Locale.getDefault()), false);
        langSpinner.setOnItemSelectedListener(this);

        final RecyclerView rvKeepRules = findViewById(R.id.rvKeepRules);
        this.keepRules = dao.getAll();
        this.rvAdapter = new KeepRulesAdapter(keepRules);
        rvKeepRules.setAdapter(rvAdapter);
        rvKeepRules.setLayoutManager(new LinearLayoutManager(this));
    }

    public void addRule(View view) {
        startCreateKeepRuleActivity.launch(null);
    }

    public void editRule(View view) {
        String keepRuleId = (String) view.getTag();
        startCreateKeepRuleActivity.launch(keepRuleId);
    }

    public void deleteRule(View view) {
        String id = (String) view.getTag();
        KeepRule deletedKeepRule = dao.delete(id);
        keepRules.remove(deletedKeepRule.getPosition());
        rvAdapter.notifyItemRemoved(deletedKeepRule.getPosition());
    }

    public void startService(View view) {
        Intent intent = new Intent(this, OverlayIconService.class);
        startService(intent);

        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        Locale lang = SpinnerConverter.toLocale(pos);
        updateLocale(lang);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // empty by design
    }
}