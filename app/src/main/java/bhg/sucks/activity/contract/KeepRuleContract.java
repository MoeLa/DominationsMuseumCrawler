package bhg.sucks.activity.contract;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import bhg.sucks.activity.CreateKeepRuleActivity;

/**
 * This contract ensures type safety, when a keep rule shall be created or edited.
 */
public class KeepRuleContract extends ActivityResultContract<String, String> {

    public static final String KEY = "id";

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, String keepRuleId) {
        Intent intent = new Intent(context, CreateKeepRuleActivity.class);
        intent.putExtra(KEY, keepRuleId);
        return intent;
    }

    @Override
    public String parseResult(int resultCode, @Nullable Intent result) {
        if (resultCode != Activity.RESULT_OK || result == null) {
            return null;
        }

        return result.getStringExtra(KEY);
    }

}
