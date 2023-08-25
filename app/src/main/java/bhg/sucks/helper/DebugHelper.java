package bhg.sucks.helper;


import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bhg.sucks.R;
import bhg.sucks.model.KeepRule;

public class DebugHelper {

    public static String DEBUG_MODE_KEY = "debug_mode";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Context context;

    public DebugHelper(Context context) {
        this.context = context;
    }

    /**
     * Logs the artefact to keep (<i>data</i>) and the matching rule (<i>keepRule</i>).
     */
    public void logKeepArtefact(OcrHelper.Data data, KeepRule keepRule) {
        SharedPreferences myPrefs = context.getSharedPreferences("ruleHits", Context.MODE_PRIVATE);

        List<OcrHelper.Data> hits = new ArrayList<>(Arrays.asList(
                gson.fromJson(myPrefs.getString(keepRule.getName(), "[]"), OcrHelper.Data[].class)
        ));
        hits.add(data);

        Set<KeepRule> rulesThatHit = new HashSet<>(Arrays.asList(
                gson.fromJson(myPrefs.getString(context.getString(R.string.rulesthathit), "[]"), KeepRule[].class)
        ));
        rulesThatHit.add(keepRule);

        myPrefs.edit()
                .putString(keepRule.getName(), gson.toJson(hits))
                .putString(context.getString(R.string.rulesthathit), gson.toJson(rulesThatHit))
                .apply();
    }
}
