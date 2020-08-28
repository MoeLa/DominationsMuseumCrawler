package bhg.sucks.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import bhg.sucks.dao.KeepRuleDAO;
import bhg.sucks.so.we.need.a.dominationsmuseumcrawler.R;

public class KeepRuleContract extends ActivityResultContract<String, String> {

    private KeepRuleDAO dao;

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, String keepRuleId) {
        this.dao = new KeepRuleDAO(context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE));

        Intent intent = new Intent(context, CreateKeepRuleActivity.class);
        intent.putExtra("id", keepRuleId);
        return intent;
    }

    @Override
    public String parseResult(int resultCode, @Nullable Intent result) {
        if (resultCode != Activity.RESULT_OK || result == null) {
            return null;
        }

        String id = result.getStringExtra("id");
        return id;
    }

}
