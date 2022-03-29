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
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity;

import java.util.List;

import bhg.sucks.R;
import bhg.sucks.activity.adapter.KeepRulesAdapter;
import bhg.sucks.activity.contract.KeepRuleContract;
import bhg.sucks.dao.KeepRuleDAO;
import bhg.sucks.helper.ExecuteAsRootBase;
import bhg.sucks.model.KeepRule;
import bhg.sucks.service.OverlayIconService;

public class MainActivity extends LocaleAwareCompatActivity {

    private static final int APP_PERMISSIONS = 1337;

    private KeepRuleDAO dao;
    private SharedPreferences sharedPref;
    private List<KeepRule> keepRules;
    private KeepRulesAdapter rvAdapter;

    private final ActivityResultLauncher<String> startCreateKeepRuleActivity = registerForActivityResult(new KeepRuleContract(), new ActivityResultCallback<String>() {

        @Override
        public void onActivityResult(String keepRuleId) {
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

    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.topAppBarMain);
        setSupportActionBar(toolbar);

        this.sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        this.dao = new KeepRuleDAO(sharedPref);

        final RecyclerView rvKeepRules = findViewById(R.id.rvKeepRules);
        this.keepRules = dao.getAll();
        this.rvAdapter = new KeepRulesAdapter(keepRules);
        rvKeepRules.setAdapter(rvAdapter);
        rvKeepRules.setLayoutManager(new LinearLayoutManager(this));

        boolean rootEnabled = ExecuteAsRootBase.canRunRootCommands();
        if (!rootEnabled) {
            Toast.makeText(this, "No root permissions", Toast.LENGTH_LONG).show();
        }
    }

    public void addRule(MenuItem mi) {
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

    public void startService(MenuItem mi) {
        if (Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(this, OverlayIconService.class);
            startService(intent);

            finish();
        } else {
            Toast.makeText(this, "No permission to draw overlays", Toast.LENGTH_LONG).show();

            // Open android settings to request overlay permissions
            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            myIntent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(myIntent, APP_PERMISSIONS);
        }
    }

    public void settings(MenuItem mi) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_PERMISSIONS) {
            // Response to 'request for overlay permissions'
            if (Settings.canDrawOverlays(this)) {
                startService((MenuItem) null);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_app_bar_main, menu);
        return true;
    }

}