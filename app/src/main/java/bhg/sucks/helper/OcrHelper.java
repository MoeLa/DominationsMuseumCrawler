package bhg.sucks.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import androidx.core.util.Pair;

import com.google.android.gms.tasks.Task;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import bhg.sucks.R;
import bhg.sucks.model.Category;
import bhg.sucks.model.Skill;

/**
 * Helper for reading the skills and level from an artifact's screenshot.
 */
public class OcrHelper {

    public static final int LEVEL_COULD_NOT_BE_DETERMINED = -1;

    private static final String TAG = "OcrHelper";
    private static final int LEV_OFFSET = 5;

    private final String fiveArtifactsButtonText; // = "475";
    private final String artifactBenefits; // = "Artefakt-Boni";
    private final String sellButtonPrefix; // = "Verkaufen für";
    private final String confirmButtonText; // = "Ja";
    private final String continueButtonText; // = "Fortfahren";

    private final Context context;
    private final TextRecognizer textRecognizer;
    private final DiffMatchPatch diffMatchPatch;
    private final DebugHelper debugHelper;
    private final boolean debugMode;
    private final Map<String, Category> categoryLookup;
    private final Multimap<Category, Skill> skillLookup;

    private final Pattern newLinePattern = Pattern.compile("\\R");

    public OcrHelper(Context context, DebugHelper debugHelper, boolean debugMode) {
        this.context = context;
        this.textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        this.diffMatchPatch = new DiffMatchPatch();
        this.debugHelper = debugHelper;
        this.debugMode = debugMode;
        this.categoryLookup = Arrays.stream(Category.values())
                .collect(Collectors.toMap(c -> c.getText(context), Function.identity()));
        this.skillLookup = HashMultimap.create();
        for (Skill s : Skill.values()) {
            skillLookup.put(s.getCategory(), s);
        }

        this.artifactBenefits = context.getString(R.string.artifact_benefits);
        this.fiveArtifactsButtonText = context.getString(R.string.five_artifacts_button_text); // oder 450 oder 425
        this.sellButtonPrefix = context.getString(R.string.sell_button_prefix);
        this.confirmButtonText = context.getString(R.string.confirm_button_text);
        this.continueButtonText = context.getString(R.string.continue_button_text);
    }

    /**
     * Tests, if the '5 Artifacts' button is visible.
     *
     * @param bitmap      Screenshot to scan
     * @param handlePoint A {@link Point} to click the button or <i>null</i>, if not found.
     */
    public void isFiveArtifactsAvailable(Bitmap bitmap, Consumer<Point> handlePoint) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        Task<Text> result = textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    List<Text.TextBlock> textBlocks = visionText.getTextBlocks();

                    Point p = isFiveArtifactsAvailable(textBlocks);
                    handlePoint.accept(p);
                });
    }

    /**
     * Tests, if the '5 Artifacts' button is visible.
     *
     * @param textBlocks Detected text blocks
     * @return A {@link Point} to click the button or <i>null</i>, if not found.
     */
    public Point isFiveArtifactsAvailable(List<Text.TextBlock> textBlocks) {
        for (Text.TextBlock textBlock : textBlocks) {
            if (textBlock.getText().equals(fiveArtifactsButtonText)) {
                return getCenterOf(textBlock.getCornerPoints());
            }
        }

        return null;
    }

    /**
     * Tests, if the 'sell' button is visible.
     *
     * @param bitmap      Screenshot to scan
     * @param handlePoint A {@link Point} to click 'sell' or <i>null</i>, if not found.
     */
    public void isSellAvailable(Bitmap bitmap, Consumer<Point> handlePoint) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        Task<Text> result = textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    List<Text.TextBlock> textBlocks = visionText.getTextBlocks();

                    Point p = isSellAvailable(textBlocks);
                    handlePoint.accept(p);
                });
    }

    /**
     * Tests, if the 'sell' button is visible.
     *
     * @param textBlocks Detected text blocks
     * @return A {@link Point} to click 'sell' or <i>null</i>, if not found.
     */
    public Point isSellAvailable(List<Text.TextBlock> textBlocks) {
        for (Text.TextBlock textBlock : textBlocks) {
            if (textBlock.getText().startsWith(sellButtonPrefix)) {
                return getCenterOf(textBlock.getCornerPoints());
            }
        }

        return null;
    }

    /**
     * Tests, if the 'Confirm' button is visible.
     *
     * @param bitmap      Screenshot to scan
     * @param handlePoint A {@link Point} to click the button or <i>null</i>, if not found.
     */
    public void isConfirmAvailable(Bitmap bitmap, Consumer<Point> handlePoint) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        Task<Text> result = textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    List<Text.TextBlock> textBlocks = visionText.getTextBlocks();

                    Point p = isConfirmAvailable(textBlocks);

                    if (p != null) {
                        // Fix to not hit 'buy single artefact', but 'buy 5 artefacts'
                        p.set(p.x + 50, p.y);
                    }

                    handlePoint.accept(p);
                });
    }

    /**
     * Tests, if the 'Confirm' button is visible.
     *
     * @param textBlocks Detected text blocks
     * @return A {@link Point} to click the button or <i>null</i>, if not found.
     */
    public Point isConfirmAvailable(List<Text.TextBlock> textBlocks) {
        for (Text.TextBlock textBlock : textBlocks) {
            if (textBlock.getText().equals(confirmButtonText)) {
                return getCenterOf(textBlock.getCornerPoints());
            }
        }

        return null;
    }

    /**
     * Tests, if the 'Continue' button is visible.
     *
     * @param bitmap      Screenshot to scan
     * @param handlePoint A {@link Point} to click the button or <i>null</i>, if not found.
     */
    public void isContinueAvailable(Bitmap bitmap, Consumer<Point> handlePoint) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        Task<Text> result = textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    List<Text.TextBlock> textBlocks = visionText.getTextBlocks();

                    Point p = isContinueAvailable(textBlocks);
                    handlePoint.accept(p);
                });
    }

    /**
     * Tests, if the 'Continue' button is visible.
     *
     * @param textBlocks Detected text blocks
     * @return A {@link Point} to click the button or <i>null</i>, if not found.
     */
    public Point isContinueAvailable(List<Text.TextBlock> textBlocks) {
        for (Text.TextBlock textBlock : textBlocks) {
            if (textBlock.getText().equals(continueButtonText)) {
                return getCenterOf(textBlock.getCornerPoints());
            }
        }

        return null;
    }

    private Point getCenterOf(Point[] cornerPoints) {
        Point res = new Point();
        res.x = (cornerPoints[0].x + cornerPoints[1].x) / 2;
        res.y = (cornerPoints[1].y + cornerPoints[2].y) / 2;
        return res;
    }

//    /**
//     * @param bitmap     Screenshot of a museum item with its skills
//     * @param handleData Data-Object with details about the item or <i>null</i>, if details couldn't be read
//     */
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    public void convertItemScreenshot(Bitmap bitmap, Consumer<Data> handleData) {
//        Stopwatch swDetectImage = Stopwatch.createStarted();
//        InputImage image = InputImage.fromBitmap(bitmap, 0);
//        Task<Text> result = textRecognizer.process(image)
//                .addOnSuccessListener(visionText -> {
//                    List<Text.TextBlock> textBlocks = visionText.getTextBlocks();
//
//                    Data d = convertItemScreenshot(textBlocks);
//
//                    swDetectImage.stop();
//                    // textRecognizer.detect(frame); // Duration about 1.4s (emulator) and 220ms (OnePlus 5T)
//                    Log.d(TAG, "Detecting/processing image in " + swDetectImage);
//
//                    handleData.accept(d);
//                });
//
//    }

    public Data convertItemScreenshot(List<Text.TextBlock> textBlocks) {
        final List<String> texts = Lists.newArrayList();
        for (Text.TextBlock textBlock : textBlocks) {
            texts.add(textBlock.getText());
        }

        Stopwatch swParsingTextBlocks = Stopwatch.createStarted();

        Category category = evaluateCategory(texts).second;
        List<Skill> skills = evaluateSkills(texts, category);
        int level = evaluateLevel(texts);
        Data ret = Data.builder()
                .category(category)
                .skills(skills)
                .level(level)
                .build();

        swParsingTextBlocks.stop();
        Log.d(TAG, "Parsing textBlocks in " + swParsingTextBlocks);

        return ret;
    }

    /**
     * Writes the skill to shared prefs.
     * <p>
     * Should be called, when no (perfect) match was achieved during recognizing.
     * </p>
     */
    private void persistSkill(Category cat, String skill) {
        if (!debugMode) {
            return;
        }

        if (skill.length() < 10 || skill.equals(artifactBenefits) || skill.startsWith(sellButtonPrefix)) {
            Log.d(TAG, "persistSkill: Neglecting '" + skill + "'");
            return;
        }

        debugHelper.persistSkill(cat, skill);
    }

    private Pair<Integer, Category> evaluateCategory(List<String> texts) {
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

                    return Pair.create(0, e.getValue());
                }

                if (bestGuess.first > lev) {
                    bestGuess = Pair.create(lev, e.getValue());
                }
            }
        }

        swEvaluateCategory.stop();
        Log.d(TAG, "Evaluating category (lev=" + bestGuess.first + ", " + bestGuess.second.getText(context) + ") in " + swEvaluateCategory);
        return bestGuess;
    }

    /**
     * The 'Levenshtein Distance' is the minimal amount of insert/delete/replace operations to transfer
     * a first string into a second. The fewer operations, the more equal the texts are. Thus being a
     * good indicator, if two texts are equal besides some typos that occur during OCR.
     * <p>
     * Note: That number is always positive.
     *
     * @return the levenshtein distance between <i>text1</i> and <i>text2</i>
     */
    private int applyDiffAndLev(String text1, String text2) {
        return diffMatchPatch.diffLevenshtein(diffMatchPatch.diffMain(text1, text2));
    }

    private List<Skill> evaluateSkills(List<String> texts, Category category) {
        int idx = Integer.MIN_VALUE;
        Stopwatch swFindIntro = Stopwatch.createStarted();
        for (int i = 0; i < texts.size(); i++) {
            if (artifactBenefits.equals(texts.get(i))) {
                idx = i + 1; // idx shall point to next text after 'artifactBonus'
                break;
            }
        }

        swFindIntro.stop();
        if (idx == Integer.MIN_VALUE) {
            Log.d(TAG, "Could not find '" + artifactBenefits + "' in detected texts in " + swFindIntro);
            return null;
        } else {
            Log.d(TAG, "Found intro to skills in " + swFindIntro);
        }

        Collection<Skill> availableSkills = skillLookup.get(category);

        List<Skill> ret = Lists.newArrayList();
        while (idx < texts.size() && ret.size() < 5) {
            Stopwatch swFindSkill = Stopwatch.createStarted();

            // Get the first/next text, assuming it is a skill
            String text = texts.get(idx);

            // Check for the text containing more than one skill
            Matcher matcher = newLinePattern.matcher(text);
            if (matcher.find()) {
                // text contains a line break
                String[] token = text.split(newLinePattern.pattern());

                // Continue with first token (instead of whole text)..
                text = token[0];
                // ... and enqueue the rest after the current one
                for (int j = token.length; j > 1; j--) {
                    texts.add(idx + 1, token[j - 1]);
                }
            }

            // Cut off the skill's percentage
            int idxOfLastLetter = lastLetterIn(text);
            if (idxOfLastLetter > 0) {
                text = text.substring(0, idxOfLastLetter + 1); // Cut off chars beginning at last space
            }

            // Determine (or at least guess) the skill
            Pair<Integer, Skill> bestGuess = Pair.create(Integer.MAX_VALUE, null);
            for (Skill s : availableSkills) {
                String skillText = context.getString(s.getResId());
                int lev = applyDiffAndLev(skillText, text);
                if (lev == 0) {
                    // Perfect match -> No need to search any further
                    bestGuess = Pair.create(lev, s);
                    break;
                }

                if (bestGuess.first > lev) {
                    // Best match so far, but continue trying the other skills
                    bestGuess = Pair.create(lev, s);
                }
            }

            swFindSkill.stop();
            if (bestGuess.first == 0) {
                // Perfect match
                ret.add(bestGuess.second);
                // No log entry
            } else if (bestGuess.first < LEV_OFFSET) {
                // With lev < LEV_OFFSET, we assume that only some typos have appeared during OCR
                ret.add(bestGuess.second);
                Log.d(TAG, String.format("Evaluating skill '%s' (lev = %s, '%s') in %s",
                        text,
                        bestGuess.first,
                        context.getString(bestGuess.second.getResId()),
                        swFindSkill
                ));
                persistSkill(category, text); // On debug mode: Write text to a file
            } else {
                // lev too bad => Probably no match => No adding to ret
                Log.d(TAG, String.format("Evaluating skills. No Match for '%s' => lev = %s, bestGuess = '%s'. Duration %s",
                        text,
                        bestGuess.first,
                        bestGuess.second == null ? null : context.getString(bestGuess.second.getResId()),
                        swFindSkill
                ));
                persistSkill(category, text); // On debug mode: Write text to a file
            }

            idx++;
        }

        return ret;
    }

    /**
     * Traverses <i>text</i> (backwards) to find the last letter, starting at text.length - 4.
     * <p>
     * Why <i>- 4</i>? - The texts end with something like '+1%' (three characters to skip) and
     * one more to avoid IndexOutOfBounds exception.
     *
     * @return index of the last letter in <i>text</i>
     */
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

        if (sellForText.contains("300") || sellForText.contains("315") || sellForText.contains("330") || sellForText.contains("360")) {
            return 3;
        }

        if (sellForText.contains("90") || sellForText.contains("94") || sellForText.contains("99") || sellForText.contains("108")) {
            return 2;
        }

        if (sellForText.contains("25") || sellForText.contains("26") || sellForText.contains("27") || sellForText.contains("30")) {
            return 1;
        }

        return LEVEL_COULD_NOT_BE_DETERMINED;
    }

    public void analyseScreenshot(Bitmap bitmap, Consumer<Text> handleSuccess, Consumer<Exception> handleFailure) {
        Log.d(TAG, "analyseScreenshot > Send image through textRecognizer");
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        textRecognizer.process(image)
                .addOnSuccessListener(handleSuccess::accept)
                .addOnFailureListener(handleFailure::accept);
    }

    public AnalysisResult toAnalyseResult(List<Text.TextBlock> textBlocks) {
        Log.d(TAG, "toAnalyseResult > Create 'AnalysisResult'");

        Screen s;
        if (isConfirmAvailable(textBlocks) != null) {
            // That dialog to confirm selling an artifact is open
            s = Screen.ARTIFACT_DESTROY_DIALOG;
        } else if (isContinueAvailable(textBlocks) != null) {
            // The continue button is visible => Next step would be to sell/continue
            s = Screen.ARTIFACT_FULLY_LOADED;
        } else if (isFiveArtifactsAvailable(textBlocks) != null) {
            // '5 Artifacts' button is visible => Assume that we're at crafting home
            s = Screen.ARTIFACT_CRAFTING_HOME;
        } else {
            final List<String> texts = Lists.newArrayList();
            for (Text.TextBlock textBlock : textBlocks) {
                texts.add(textBlock.getText());
            }

            Pair<Integer, Category> pair = evaluateCategory(texts);
            if (pair.first < 4) {
                // lev is good enough to assume, we're during a crafting animation
                s = Screen.ARTIFACT_CRAFT_ANIMATION;
            } else {
                // lev is too bad to assume, that we're during a crafting animation (where the category is already visible)
                s = Screen.COULD_NOT_DETERMINE;
            }
        }

        return AnalysisResult.builder()
                .screen(s)
                .textBlocks(textBlocks)
                .build();
    }

    public enum Screen {

        ARTIFACT_CRAFTING_HOME,
        ARTIFACT_CRAFT_ANIMATION,
        ARTIFACT_FULLY_LOADED,
        ARTIFACT_DESTROY_DIALOG,
        COULD_NOT_DETERMINE

    }

    /**
     * Information about the current screen and its texts.
     */
    public static class AnalysisResult {

        private final Screen screen;
        private final List<Text.TextBlock> textBlocks;

        private AnalysisResult(Builder builder) {
            screen = builder.screen;
            textBlocks = builder.textBlocks;
        }

        public static Builder builder() {
            return new Builder();
        }

        public Screen getScreen() {
            return screen;
        }

        public List<Text.TextBlock> getTextBlocks() {
            return textBlocks;
        }

        public static final class Builder {
            private Screen screen;
            private List<Text.TextBlock> textBlocks;

            private Builder() {
            }

            public Builder screen(Screen val) {
                screen = val;
                return this;
            }

            public Builder textBlocks(List<Text.TextBlock> val) {
                textBlocks = val;
                return this;
            }

            public AnalysisResult build() {
                return new AnalysisResult(this);
            }
        }
    }


    public static class Data {

        private Category category;
        private List<Skill> skills;
        private int level;

        private Data(Builder builder) {
            setCategory(builder.category);
            setSkills(builder.skills);
            setLevel(builder.level);
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * A {@link Data} is considered complete, if
         * <ul>
         *     <li>a category was determined</li>
         *     <li>at least some skills were determined and</li>
         *     <li>the level could be determined</li>
         * </ul>
         *
         * @return <i>true</i>, if this data has been filled completely
         */
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Data data = (Data) o;

            if (level != data.level) return false;
            if (category != data.category) return false;
            return Objects.equals(skills, data.skills);
        }

        @Override
        public int hashCode() {
            int result = category != null ? category.hashCode() : 0;
            result = 31 * result + (skills != null ? skills.hashCode() : 0);
            result = 31 * result + level;
            return result;
        }

        public Category getCategory() {
            return category;
        }

        public void setCategory(Category category) {
            this.category = category;
        }

        public List<Skill> getSkills() {
            return skills;
        }

        public void setSkills(List<Skill> skills) {
            this.skills = skills;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public static final class Builder {
            private Category category;
            private List<Skill> skills;
            private int level;

            private Builder() {
            }

            public Builder category(Category val) {
                category = val;
                return this;
            }

            public Builder skills(List<Skill> val) {
                skills = val;
                return this;
            }

            public Builder level(int val) {
                level = val;
                return this;
            }

            public Data build() {
                return new Data(this);
            }
        }
    }

}
