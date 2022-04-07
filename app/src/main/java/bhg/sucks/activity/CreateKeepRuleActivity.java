package bhg.sucks.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import bhg.sucks.R;
import bhg.sucks.activity.adapter.SkillsAdapter;
import bhg.sucks.activity.contract.KeepRuleContract;
import bhg.sucks.converter.SpinnerConverter;
import bhg.sucks.dao.KeepRuleDAO;
import bhg.sucks.databinding.ActivityCreateKeepRuleBinding;
import bhg.sucks.helper.UIHelper;
import bhg.sucks.model.AmountMatches;
import bhg.sucks.model.Category;
import bhg.sucks.model.KeepRule;
import bhg.sucks.model.Skill;

/**
 * Activity to define a new {@link KeepRule} or update an existing one.
 */
public class CreateKeepRuleActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "CreateKeepRuleActivity";
    private final Comparator<Skill> SKILL_COMPARATOR = Comparator.comparing(s -> getString(s.getResId()));

    private Map<Category, List<Skill>> skillsLookup;
    private KeepRuleDAO dao;
    private KeepRule keepRule;
    private SkillsAdapter rvMandatorySkillsAdapter;
    private SkillsAdapter rvOptionalSkillsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_keep_rule);

        this.skillsLookup = Maps.newHashMap();
        for (Skill s : Skill.values()) {
            List<Skill> skills = skillsLookup.computeIfAbsent(s.getCategory(), (c) -> Lists.newArrayList());
            skills.add(s);
        }

        for (List<Skill> skills : skillsLookup.values()) {
            skills.sort(SKILL_COMPARATOR);
        }

        this.dao = new KeepRuleDAO(getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE));

        Intent intent = getIntent();
        this.keepRule = Optional.ofNullable(intent.getStringExtra(KeepRuleContract.KEY))
                .map(id -> dao.get(id))
                .orElse(KeepRule.builder()
                        .name("")
                        .category(Category.Weapon)
                        .amountMatches(AmountMatches.FOUR_OF_FIVE)
                        .mandatorySkills(UIHelper.createEmptySkillsMap())
                        .optionalSkills(UIHelper.createEmptySkillsMap())
                        .build());

        ActivityCreateKeepRuleBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_create_keep_rule);
        binding.setKeepRule(keepRule);

        // Important: Do stuff like findViewById etc. AFTER initializing data binding!!!
        MaterialToolbar toolbar = findViewById(R.id.topAppBarCreateKeepRule);
        setSupportActionBar(toolbar);
        if (intent.getStringExtra(KeepRuleContract.KEY) != null) {
            toolbar.setTitle(R.string.edit_keep_rule);
        }

        String[] categories = getResources().getStringArray(R.array.array_categories);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, R.layout.autocomplete_list_item, categories);
        AutoCompleteTextView category = findViewById(R.id.categoryTextView);
        category.setAdapter(categoryAdapter);
        category.setText(categories[SpinnerConverter.toInt(keepRule.getCategory())], false);
        category.setOnItemClickListener(this);

        String[] amountMatchesArray = getResources().getStringArray(R.array.array_amount_matches);
        ArrayAdapter<String> amountMatchesAdapter = new ArrayAdapter<>(this, R.layout.autocomplete_list_item, amountMatchesArray);
        AutoCompleteTextView amountMatches = findViewById(R.id.amountMatchesTextView);
        amountMatches.setAdapter(amountMatchesAdapter);
        amountMatches.setText(amountMatchesArray[SpinnerConverter.toInt(keepRule.getAmountMatches())], false);
        amountMatches.setOnItemClickListener((parent, view, position, id) -> {
            AmountMatches newAmountMatches = SpinnerConverter.toAmountMatches(position);
            keepRule.setAmountMatches(newAmountMatches);
        });

        // Initializing the recycler views must happen after initializing data binding
        final RecyclerView rvMandatorySkills = findViewById(R.id.rvMandatorySkills);
        rvMandatorySkills.setLayoutManager(new LinearLayoutManager(this));
        this.rvMandatorySkillsAdapter = new SkillsAdapter(keepRule, SkillsAdapter.SkillsAdapterKey.MANDATORY);
        rvMandatorySkills.setAdapter(rvMandatorySkillsAdapter);

        final RecyclerView rvOptionalSkills = findViewById(R.id.rvOptionalSkills);
        rvOptionalSkills.setLayoutManager(new LinearLayoutManager(this));
        this.rvOptionalSkillsAdapter = new SkillsAdapter(keepRule, SkillsAdapter.SkillsAdapterKey.OPTIONAL);
        rvOptionalSkills.setAdapter(rvOptionalSkillsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_app_bar_create_keep_rule, menu);
        return true;
    }

    public void onAddMandatorySkillClicked(View view) {
        List<Skill> mandatorySkills = keepRule.getMandatorySkillsOfCategory();

        final Long maxCount = 2L; // How often may a skill be picked
        Set<Skill> doNotShowLookup = mandatorySkills.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()))
                .entrySet().stream()
                .filter(e -> maxCount.equals(e.getValue())) // Keep skills in set, that have been picked twice
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        List<Skill> skillsToShow = getSkillsFromLookupFor(keepRule.getCategory()).stream()
                .filter(skill -> !doNotShowLookup.contains(skill))
                .collect(Collectors.toList());

        String[] items = skillsToShow.stream()
                .map(skill -> getString(skill.getResId()))
                .toArray(String[]::new);

        new MaterialAlertDialogBuilder(view.getContext())
                .setTitle(String.format("%s > %s",
                        getString(R.string.mandatory_skills),
                        keepRule.getCategory().getText(view.getContext())))
                .setItems(items, (dialog, which) -> {
                    Skill picked = skillsToShow.get(which);

                    // Add picked skill to keep rule
                    mandatorySkills.add(picked);
                    mandatorySkills.sort(SKILL_COMPARATOR);

                    // Update mandatory skills recycler view
                    int posAfterSort = mandatorySkills.indexOf(picked);
                    rvMandatorySkillsAdapter.notifyItemInserted(posAfterSort);
                })
                .show();
    }

    public void onAddOptionalSkillClicked(View view) {
        List<Skill> optionalSkills = keepRule.getOptionalSkillsOfCategory();

        Set<Skill> doNotShowLookup = new HashSet<>(optionalSkills);

        List<Skill> skillsToShow = getSkillsFromLookupFor(keepRule.getCategory()).stream()
                .filter(skill -> !doNotShowLookup.contains(skill))
                .collect(Collectors.toList());

        String[] items = skillsToShow.stream()
                .map(skill -> getString(skill.getResId()))
                .toArray(String[]::new);

        new MaterialAlertDialogBuilder(view.getContext())
                .setTitle(String.format("%s > %s",
                        getString(R.string.optional_skills),
                        keepRule.getCategory().getText(view.getContext())))
                .setItems(items, (dialog, which) -> {
                    Skill picked = skillsToShow.get(which);

                    // Add picked skill to keep rule
                    optionalSkills.add(picked);
                    optionalSkills.sort(SKILL_COMPARATOR);

                    // Update optional skills recycler view
                    int posAfterSort = optionalSkills.indexOf(picked);
                    rvOptionalSkillsAdapter.notifyItemInserted(posAfterSort);
                })
                .show();
    }

    public void onDeleteSkillClicked(View view) {
        SkillsAdapter.SkillsAdapterKey key = (SkillsAdapter.SkillsAdapterKey) view.getTag(R.id.TAG_SKILL_DELETE_BUTTON_KEY);
        Skill skill = (Skill) view.getTag(R.id.TAG_SKILL_DELETE_BUTTON_SKILL);

        switch (key) {
            case MANDATORY: {
                List<Skill> skills = keepRule.getMandatorySkillsOfCategory();

                int idx = skills.indexOf(skill);
                if (idx >= 0) {
                    skills.remove(idx);
                    rvMandatorySkillsAdapter.notifyItemRemoved(idx);
                }
                break;
            }
            case OPTIONAL: {
                List<Skill> skills = keepRule.getOptionalSkillsOfCategory();

                int idx = skills.indexOf(skill);
                if (idx >= 0) {
                    skills.remove(idx);
                    rvOptionalSkillsAdapter.notifyItemRemoved(idx);
                }
                break;
            }
            default:
        }
    }

    public void save(MenuItem mi) {
        KeepRule ret;
        if (keepRule.getId() == null) {
            // Note: id and position are set in dao.create
            ret = dao.create(keepRule);
            Log.d(TAG, "New keep rule created -> Id: " + ret.getId());
        } else {
            ret = dao.update(keepRule);
            Log.d(TAG, "Keep rule updated -> Id: " + ret.getId());
        }

        Intent result = new Intent();
        result.putExtra("id", ret.getId());
        setResult(Activity.RESULT_OK, result);

        finish();
    }

    private List<Skill> getSkillsFromLookupFor(Category cat) {
        return Optional.ofNullable(cat)
                .map(c -> skillsLookup.get(c))
                .map(Lists::newArrayList)
                .orElse(Lists.newArrayList());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int amountOldMandatorySkills = keepRule.getMandatorySkillsOfCategory().size();
        int amountOldOptionalSkills = keepRule.getOptionalSkillsOfCategory().size();

        Category newCategory = SpinnerConverter.toCategory(position);
        keepRule.setCategory(newCategory); // Setting new category in keep rule

        // Reset mandatory skills
        rvMandatorySkillsAdapter.notifyItemRangeRemoved(0, amountOldMandatorySkills);
        int amountNewMandatorySkills = keepRule.getMandatorySkillsOfCategory().size();
        rvMandatorySkillsAdapter.notifyItemRangeInserted(0, amountNewMandatorySkills);

        // Reset optional skills
        rvOptionalSkillsAdapter.notifyItemRangeRemoved(0, amountOldOptionalSkills);
        int amountNewOptionalSkills = keepRule.getOptionalSkillsOfCategory().size();
        rvOptionalSkillsAdapter.notifyItemRangeInserted(0, amountNewOptionalSkills);
    }

}