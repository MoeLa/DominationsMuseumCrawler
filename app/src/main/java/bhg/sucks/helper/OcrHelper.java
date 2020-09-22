package bhg.sucks.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.RequiresApi;
import androidx.core.util.Pair;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import com.google.gson.Gson;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import bhg.sucks.model.Category;
import bhg.sucks.model.Skill;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Helper for reading the skills and level from an artifact's screenshot.
 */
public class OcrHelper {

    public final static int LEVEL_COULD_NOT_BE_DETERMINED = -1;
    private static final String TAG = "OcrHelper";

    private final static String artifactBonus = "Artefakt-Boni";
    private final static String fiveArtifactsButtonText = "475";
    private final static String sellButtonPrefix = "Verkaufen für";
    private final static String confirmButtonText = "Ja";
    private final static String continueButtonText = "Fortfahren";

    private final Context context;
    private final TextRecognizer textRecognizer;
    private final DiffMatchPatch diffMatchPatch;
    private final Gson gson;
    private final Map<String, Category> categoryLookup;
    private Multimap<Category, Skill> skillLookup;

    public OcrHelper(Context context) {
        this.context = context;
        this.textRecognizer = new TextRecognizer.Builder(context).build();
        this.diffMatchPatch = new DiffMatchPatch();
        this.gson = new Gson();
        this.categoryLookup = Arrays.stream(Category.values())
                .collect(Collectors.toMap(c -> c.getText(context), Function.identity()));
        this.skillLookup = HashMultimap.create();
        for (Skill s : Skill.values()) {
            skillLookup.put(s.getCategory(), s);
        }
    }

    /**
     * Tests, if the '5 Artifacts' button is visible.
     *
     * @param bitmap Screenshot to scan
     * @return A {@link Point} to click the button or <i>null</i>, if not found.
     */
    public Point isFiveArtifactsAvailable(Bitmap bitmap) {
        if (!textRecognizer.isOperational()) {
            return null;
        }

        Frame frame = new Frame.Builder()
                .setBitmap(bitmap)
                .build();
        SparseArray<TextBlock> textBlocks = textRecognizer.detect(frame);

        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.valueAt(i);

            if (textBlock.getValue().equals(fiveArtifactsButtonText)) {
                return textBlock.getCornerPoints()[0]; // Top-left point of text should be fine here
            }
        }

        return null;
    }

    /**
     * Tests, if the 'sell' button is visible.
     *
     * @param bitmap Screenshot to scan
     * @return A {@link Point} to click 'sell' or <i>null</i>, if not found.
     */
    public Point isSellAvailable(Bitmap bitmap) {
        if (!textRecognizer.isOperational()) {
            return null;
        }

        Frame frame = new Frame.Builder()
                .setBitmap(bitmap)
                .build();
        SparseArray<TextBlock> textBlocks = textRecognizer.detect(frame);

        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.valueAt(i);

            if (textBlock.getValue().startsWith(sellButtonPrefix)) {
                return textBlock.getCornerPoints()[0]; // Top-left point of text should be fine here
            }
        }

        return null;
    }

    /**
     * Tests, if the 'Confirm' button is visible.
     *
     * @param bitmap Screenshot to scan
     * @return A {@link Point} to click the button or <i>null</i>, if not found.
     */
    public Point isConfirmAvailable(Bitmap bitmap) {
        if (!textRecognizer.isOperational()) {
            return null;
        }

        Frame frame = new Frame.Builder()
                .setBitmap(bitmap)
                .build();
        SparseArray<TextBlock> textBlocks = textRecognizer.detect(frame);

        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.valueAt(i);

            if (textBlock.getValue().equals(confirmButtonText)) {
                return textBlock.getCornerPoints()[0]; // Top-left point of text should be fine here
            }
        }

        return null;
    }

    /**
     * Tests, if the 'Continue' button is visible.
     *
     * @param bitmap Screenshot to scan
     * @return A {@link Point} to click the button or <i>null</i>, if not found.
     */
    public Point isContinueAvailable(Bitmap bitmap) {
        if (!textRecognizer.isOperational()) {
            return null;
        }

        Frame frame = new Frame.Builder()
                .setBitmap(bitmap)
                .build();
        SparseArray<TextBlock> textBlocks = textRecognizer.detect(frame);

        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.valueAt(i);

            if (textBlock.getValue().equals(continueButtonText)) {
                return textBlock.getCornerPoints()[0]; // Top-left point of text should be fine here
            }
        }

        return null;
    }

    /**
     * @param bitmap Screenshot of a museum item with its skills
     * @return Data-Object with details about the item or <i>null</i>, if details couldn't be read
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public Data convertItemScreenshot(Bitmap bitmap) {
        if (!textRecognizer.isOperational()) {
            Log.d(TAG, "TextRecognizer is not operational!");
            return null;
        }

        Stopwatch swDetectImage = Stopwatch.createStarted();
        Frame frame = new Frame.Builder()
                .setBitmap(bitmap)
                .build();
        SparseArray<TextBlock> textBlocks = textRecognizer.detect(frame); // Duration about 1.4s (emulator) and 220ms (OnePlus 5T)
        final List<String> texts = Lists.newArrayList();
        for (int i = 0; i < textBlocks.size(); i++) {
            texts.add(textBlocks.valueAt(i).getValue());
        }
        swDetectImage.stop();
        Log.d(TAG, "Detecting image in " + swDetectImage);

        Stopwatch swParsingTextBlocks = Stopwatch.createStarted();

        Category category = evaluateCategory(texts);
        List<Skill> skills = evaluateSkills(texts, category);
        int level = evaluateLevel(texts);
        Data ret = Data.builder()
                .category(category)
                .skills(skills)
                .level(level)
                .build();

        swParsingTextBlocks.stop();
        Log.d(TAG, "Parsing textBlocks in " + swParsingTextBlocks);

//        if (!ret.isComplete()) {
//            Log.i(TAG, String.format("Item's attributes could not be parsed (completely): %s", ret));
//            return null;
//        }

        return ret;
    }

    /**
     * Writes the skill to shared prefs.
     * <p>
     * Should be called, when no (perfect) match was achieved during recognizing.
     * </p>
     */
    private void persistSkill(Category cat, String skill) {
        if (skill.length() <= 10 || skill.equals(artifactBonus) || skill.startsWith(sellButtonPrefix)) {
            Log.d(TAG, "persistSkill: Neglecting '" + skill + "'");
            return;
        }

        if (cat.ordinal() <= 3) {
            // Main hall artifact => Persist skill in shared prefs
            Stopwatch swUpdatingSharedPrefs = Stopwatch.createStarted();
            final String key = Optional.ofNullable(cat.getText(context))
                    .orElse(cat.toString());

            SharedPreferences myPrefs = context.getSharedPreferences("mainHallSkillTexts", Context.MODE_PRIVATE);
            Set<String> skills = Sets.newTreeSet(Arrays.asList(gson.fromJson(myPrefs.getString(key, "[]"), String[].class)));
            skills.add(skill);

            String persistString = gson.toJson(skills);
            myPrefs.edit()
                    .putString(key, persistString)
                    .apply();

            swUpdatingSharedPrefs.stop();
            Log.d(TAG, "Updating SharedPrefs (adding " + key + "/" + skill + ") in " + swUpdatingSharedPrefs);
        } else {
            // War hall artifact => We're here because of a missing enum
            // Assumption: There is a main hall enum with the same text (or wrong category in a war hall enum). Go, find it and write everything out.
            AtomicInteger countWrites = new AtomicInteger(0);
            skillLookup.entries().stream()
                    .filter(e -> e.getKey().ordinal() <= 3) // Only keep main hall categories
                    .map(e -> Pair.create(e, applyDiffAndLev(context.getString(e.getValue().getResId()), skill)))
                    .min(Comparator.comparing(p -> p.second))
                    .ifPresent(p -> {
                        String potSkillName = "War" + p.first.getValue().toString();
                        try {
                            Skill skill1 = Skill.valueOf(potSkillName);
                            Log.i(TAG, "Skill '" + potSkillName + "' already exists.");
                            return;
                        } catch (IllegalArgumentException e) {
                            // Skill doesn't exist yet => Write it to newSkillsEnums
                            File file = new File(context.getCacheDir(), "newSkillEnums.txt");
                            CharSink charSink = Files.asCharSink(file, Charset.defaultCharset(), FileWriteMode.APPEND);

                            try {
                                String x = String.format("// No match for '%s' in %s. Probably add...%n%s%n",
                                        skill,
                                        cat,
                                        String.format("%s(Category.%s, R.string.%s),",
                                                potSkillName,
                                                cat,
                                                p.first.getValue())
                                );
                                charSink.write(x);
                                Log.w(TAG, x);
                                countWrites.incrementAndGet();
                            } catch (IOException ex) {
                                Log.e("Exception", "File write failed: " + p.first.toString());
                            }
                        }
                    });
            if (countWrites.get() == 0) {
                Log.e(TAG, String.format("No writes for %s/%s", cat, skill));
            }
        }

    }


    private Category evaluateCategory(List<String> texts) {
        // Possible scenarios: 'text' might be
        // 1. the name of the item + '\n' + the category
        // 2. the category alone
        // 3. things like 'Pottery ö', 'WeaponX', 'War Weapon &', 'Ristung', 'Ausristung (Krieg)', 'Ausrüstung (Krieg) &' => Handled by redoing the screenshot + ocr detection
        // or something completely unrelated, that must be ignored

        Stopwatch swEvaluateCategory = Stopwatch.createStarted();
        List<String> flattenedList = Lists.newArrayList();
        for (String text : texts) {
            if (text.contains("\n")) {
                flattenedList.addAll(Arrays.asList(text.split("\n")));
            } else {
                flattenedList.add(text);
            }
        }
        Log.d(TAG, "evaluateCategory: " + flattenedList);

        Pair<Integer, Category> bestGuess = Pair.create(Integer.MAX_VALUE, null);
        for (String text : flattenedList) {
            for (Map.Entry<String, Category> e : categoryLookup.entrySet()) {
                int lev = applyDiffAndLev(e.getKey(), text);
                if (lev == 0) {
                    swEvaluateCategory.stop();
                    Log.d(TAG, "Evaluating category (perfect match) in " + swEvaluateCategory);

                    return e.getValue();
                }

                if (bestGuess.first > lev) {
                    bestGuess = Pair.create(lev, e.getValue());
                }
            }
        }

        swEvaluateCategory.stop();
        Log.d(TAG, "Evaluating category (lev=" + bestGuess.first + ", " + bestGuess.second.getText(context) + ") in " + swEvaluateCategory);
        return bestGuess.second;
    }

    private int applyDiffAndLev(String text1, String text2) {
        return diffMatchPatch.diffLevenshtein(diffMatchPatch.diffMain(text1, text2));
    }

    private List<Skill> evaluateSkills(List<String> texts, Category category) {
        int idx = Integer.MIN_VALUE;
        Stopwatch swFindIntro = Stopwatch.createStarted();
        for (int i = 0; i < texts.size(); i++) {
            if (artifactBonus.equals(texts.get(i))) {
                idx = i + 1; // idx shall point to next text after 'artifactBonus'
                break;
            }
        }

        swFindIntro.stop();
        if (idx == Integer.MIN_VALUE) {
            Log.d(TAG, "Could not find '" + artifactBonus + "' in detected texts in " + swFindIntro);
            return null;
        } else {
            Log.d(TAG, "Found intro to skills in " + swFindIntro);
        }

        Collection<Skill> availableSkills = skillLookup.get(category);

        List<Skill> ret = Lists.newArrayList();
        while (idx < texts.size() && ret.size() < 5) {
            Stopwatch swFindSkill = Stopwatch.createStarted();

            String text = texts.get(idx); // Get text
            int idxOfLastLetter = lastLetterIn(text);
            //  int idxOfLastSpace = text.indexOf(' ', text.length() - 5); // Index of last space (with in the last five characters)
            if (idxOfLastLetter > 0) {
                text = text.substring(0, idxOfLastLetter + 1); // Cut off chars beginning at last space
            }

            Pair<Integer, Skill> bestGuess = Pair.create(Integer.MAX_VALUE, null);
            for (Skill s : availableSkills) {
                String skillText = context.getString(s.getResId());
                int lev = applyDiffAndLev(skillText, text);
                if (lev == 0) {
                    bestGuess = Pair.create(lev, s);
                    break;
                }

                if (bestGuess.first > lev) {
                    bestGuess = Pair.create(lev, s);
                }
            }

            swFindSkill.stop();
            if (bestGuess.first == 0) {
                ret.add(bestGuess.second);
                // No log entry
            } else if (bestGuess.first < 5) {
                ret.add(bestGuess.second);
                Log.d(TAG, String.format("Evaluating skill '%s' (lev = %s, '%s') in %s",
                        text,
                        bestGuess.first,
                        context.getString(bestGuess.second.getResId()),
                        swFindSkill
                ));
                persistSkill(category, text);
            } else {
                // lev too bad => Probably no match => No adding to ret
                Log.d(TAG, String.format("Evaluating skills. No Match for '%s' => lev = %s, bestGuess = '%s'. Duration %s",
                        text,
                        bestGuess.first,
                        bestGuess.second == null ? null : context.getString(bestGuess.second.getResId()),
                        swFindSkill
                ));
                persistSkill(category, text);
            }

            idx++;
        }

        return ret;
    }

    private int lastLetterIn(String text) {
        for (int i = text.length() - 1 - 3; i >= 0; i--) {
            if (Character.isLetter(text.charAt(i))) {
                return i;
            }
        }

        return -1;
    }

    private int evaluateLevel(List<String> texts) {
        for (String text : texts) {
            if (text.startsWith(sellButtonPrefix)) {
                return getLevelFrom(text);
            }
        }

        return LEVEL_COULD_NOT_BE_DETERMINED;
    }

    /**
     * Determines the level of the artifact from the "sell for"-button text.
     *
     * @return <i>1</i>, <i>2</i>, <i>3</i>, or {@link #LEVEL_COULD_NOT_BE_DETERMINED}
     */
    private int getLevelFrom(String sellForText) {
        if (sellForText == null || sellForText.isEmpty()) {
            return LEVEL_COULD_NOT_BE_DETERMINED;
        }

        if (sellForText.contains("25")) {
            return 1;
        }

        if (sellForText.contains("90")) {
            return 2;
        }

        if (sellForText.contains("300")) {
            return 3;
        }

        return LEVEL_COULD_NOT_BE_DETERMINED;
    }

    @Getter
    @Setter
    @Builder
    @EqualsAndHashCode
    public static class Data {

        private Category category;
        private List<Skill> skills;
        private int level;

        public boolean isComplete() {
            return category != null
                    && skills != null // && skills.size() == 5
                    && level != LEVEL_COULD_NOT_BE_DETERMINED;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "category=" + category +
                    ", skills=" + skills +
                    ", level=" + level +
                    '}';
        }
    }

}
