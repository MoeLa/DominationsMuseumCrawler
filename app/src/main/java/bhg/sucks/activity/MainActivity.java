package bhg.sucks.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import bhg.sucks.R;
import bhg.sucks.activity.adapter.KeepRulesAdapter;
import bhg.sucks.activity.contract.KeepRuleContract;
import bhg.sucks.dao.KeepRuleDAO;
import bhg.sucks.helper.ExecuteAsRootBase;
import bhg.sucks.model.KeepRule;
import bhg.sucks.service.OverlayIconService;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            activityResult -> startServiceInternal(findViewById(R.id.topAppBarMain))
    );
    private KeepRuleDAO dao;
    private List<KeepRule> keepRules;
    private KeepRulesAdapter rvAdapter;
    private final ActivityResultLauncher<String> startCreateKeepRuleActivity = registerForActivityResult(
            new KeepRuleContract(),
            this::createKeepRuleResponse
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.topAppBarMain);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        this.dao = new KeepRuleDAO(sharedPref);

        final RecyclerView rvKeepRules = findViewById(R.id.rvKeepRules);
        this.keepRules = dao.getAll();
        this.rvAdapter = new KeepRulesAdapter(keepRules);
        rvKeepRules.setAdapter(rvAdapter);
        rvKeepRules.setLayoutManager(new LinearLayoutManager(this));

        boolean rootEnabled = ExecuteAsRootBase.canRunRootCommands();
        if (!rootEnabled) {
            Snackbar.make(rvKeepRules, R.string.no_root_permission, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.dismiss, v -> {
                        // Intentionally left empty
                    })
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_app_bar_main, menu);
        return true;
    }

    public void addRule(MenuItem mi) {
        startCreateKeepRuleActivity.launch(null);
    }

    public void startService(MenuItem mi) {
        startServiceInternal(findViewById(R.id.rvKeepRules));
    }

    public void startServiceInternal(View view) {
        if (Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(this, OverlayIconService.class);
            startService(intent);

            finish();
        } else {
            Snackbar.make(view, R.string.no_overlay_permissions, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.open_settings, v -> {
                        Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        myIntent.setData(Uri.parse("package:" + getPackageName()));
                        requestPermissionLauncher.launch(myIntent);
                    })
                    .show();
        }
    }

    public void settings(MenuItem mi) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
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

    private void createKeepRuleResponse(String keepRuleId) {
        KeepRule keepRule = dao.get(keepRuleId);
        if (keepRule == null) {
            // Happens, if back button is used
            return;
        }

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
}
