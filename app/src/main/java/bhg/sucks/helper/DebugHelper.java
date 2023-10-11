package bhg.sucks.helper;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import bhg.sucks.R;
import bhg.sucks.model.Category;
import bhg.sucks.model.KeepRule;

public class DebugHelper {

    public static String DEBUG_MODE_KEY = "debug_mode";

    private static final String TAG = "DebugHelper";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Context context;

    public DebugHelper(Context context) {
        this.context = context;
    }

    /**
     * Logs the artefact to keep (<i>data</i>) and the matching rule (<i>keepRule</i>).
     */
    public void logKeepArtefact(OcrHelper.Data data, KeepRule keepRule) {
        Stopwatch swUpdatingSharedPrefs = Stopwatch.createStarted();
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

        swUpdatingSharedPrefs.stop();
        Log.d(TAG, "Updating SharedPrefs > rulesThatHit (adding " + keepRule.getName() + ") in " + swUpdatingSharedPrefs);
    }

    /**
     * Writes the skill to shared prefs.
     * <p>
     * Should be called, when no (perfect) match was achieved during recognizing.
     * </p>
     */
    public void persistSkill(Category cat, String skill) {
//        if (cat.ordinal() <= 3) {
        // Persist skill in shared prefs
        Stopwatch swUpdatingSharedPrefs = Stopwatch.createStarted();
        final String key = Optional.ofNullable(cat.getText(context))
                .orElse(cat.toString());

        SharedPreferences myPrefs = context.getSharedPreferences("skillTexts", Context.MODE_PRIVATE);
        Set<String> skills = Sets.newTreeSet(Arrays.asList(gson.fromJson(myPrefs.getString(key, "[]"), String[].class)));
        skills.add(skill);

        String persistString = gson.toJson(skills);
        myPrefs.edit()
                .putString(key, persistString)
                .apply();

        swUpdatingSharedPrefs.stop();
        Log.d(TAG, "Updating SharedPrefs > skillTexts (adding " + key + "/" + skill + ") in " + swUpdatingSharedPrefs);
//        } else {
        // War hall artifact => We're here because of a missing enum
        // Assumption: There is a main hall enum with the same text (or wrong category in a war hall enum). Go, find it and write everything out.
//            AtomicInteger countWrites = new AtomicInteger(0);
//            skillLookup.entries().stream()
//                    .filter(e -> e.getKey().ordinal() <= 3) // Only keep main hall categories
//                    .map(e -> Pair.create(e, applyDiffAndLev(context.getString(e.getValue().getResId()), skill)))
//                    .min(Comparator.comparing(p -> p.second))
//                    .ifPresent(p -> {
//                        String potSkillName = "War" + p.first.getValue().toString();
//                        try {
//                            Skill skill1 = Skill.valueOf(potSkillName);
//                            Log.i(TAG, "Skill '" + potSkillName + "' already exists.");
//                            return;
//                        } catch (IllegalArgumentException e) {
//                            // Skill doesn't exist yet => Write it to newSkillsEnums
//                            File file = new File(context.getCacheDir(), "newSkillEnums.txt");
//                            CharSink charSink = Files.asCharSink(file, Charset.defaultCharset(), FileWriteMode.APPEND);
//
//                            try {
//                                String x = String.format("// No match for '%s' in %s. Probably add...%n%s%n",
//                                        skill,
//                                        cat,
//                                        String.format("%s(Category.%s, R.string.%s),",
//                                                potSkillName,
//                                                cat,
//                                                p.first.getValue())
//                                );
//                                charSink.write(x);
//                                Log.w(TAG, x);
//                                countWrites.incrementAndGet();
//                            } catch (IOException ex) {
//                                Log.e("Exception", "File write failed: " + p.first.toString());
//                            }
//                        }
//                    });
//            if (countWrites.get() == 0) {
//                Log.e(TAG, String.format("No writes for %s/%s", cat, skill));
//            }
//        }

    }
}
