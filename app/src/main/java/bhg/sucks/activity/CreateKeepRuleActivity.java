package bhg.sucks.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import bhg.sucks.R;
import bhg.sucks.converter.SpinnerConverter;
import bhg.sucks.dao.KeepRuleDAO;
import bhg.sucks.databinding.ActivityCreateKeepRuleBinding;
import bhg.sucks.model.AmountMatches;
import bhg.sucks.model.Category;
import bhg.sucks.model.KeepRule;
import bhg.sucks.model.Skill;

/**
 * Activity to define a new {@link KeepRule} or update an existing one.
 */
public class CreateKeepRuleActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "CreateKeepRuleActivity";
    private final Comparator<Skill> SKILL_COMPARATOR = Comparator.comparing(s -> getString(s.getResId()));

    private Map<Category, List<Skill>> skillsLookup;
    /**
     * Skills displayed in rvSelectedSkills (old)
     */
    private List<Skill> skills;
    private KeepRuleDAO dao;
    private KeepRule keepRule;
    private SelectSkillsAdapter rvAdapter;
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
                        .name("NewName")
                        .category(Category.Weapon)
                        .amountMatches(AmountMatches.FOUR_OF_FIVE)
                        .skills(Sets.newHashSet())
                        .mandatorySkills(createEmptySkillsMap())
                        .optionalSkills(createEmptySkillsMap())
                        .build());

        ActivityCreateKeepRuleBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_create_keep_rule);
        binding.setKeepRule(keepRule);

        Spinner categorySpinner = findViewById(R.id.inpCategory);
        categorySpinner.setSelection(SpinnerConverter.toInt(keepRule.getCategory()));
        categorySpinner.setOnItemSelectedListener(this);

        // Initializing the recycler view must happen after initializing data binding
        final RecyclerView rvSelectedSkills = findViewById(R.id.rvSelectedSkills);
        this.skills = getSkillsFromLookupFor(keepRule.getCategory());
        this.rvAdapter = new SelectSkillsAdapter(skills, keepRule);
        rvSelectedSkills.setAdapter(rvAdapter);
        rvSelectedSkills.setLayoutManager(new LinearLayoutManager(this));

        final RecyclerView rvMandatorySkills = findViewById(R.id.rvMandatorySkills);
        rvMandatorySkills.setLayoutManager(new LinearLayoutManager(this));
        this.rvMandatorySkillsAdapter = new SkillsAdapter(keepRule, SkillsAdapter.SkillsAdapterKey.MANDATORY);
        rvMandatorySkills.setAdapter(rvMandatorySkillsAdapter);

        final RecyclerView rvOptionalSkills = findViewById(R.id.rvOptionalSkills);
        rvOptionalSkills.setLayoutManager(new LinearLayoutManager(this));
        this.rvOptionalSkillsAdapter = new SkillsAdapter(keepRule, SkillsAdapter.SkillsAdapterKey.OPTIONAL);
        rvOptionalSkills.setAdapter(rvOptionalSkillsAdapter);
    }

    private Map<Category, List<Skill>> createEmptySkillsMap() {
        Map<Category, List<Skill>> result = new HashMap<>();
        for (Category c : Category.values()) {
            result.put(c, new ArrayList<>());
        }

        return result;
    }

    public void onCheckboxClicked(View view) {
        CheckBox cb = (CheckBox) view;
        Skill s = Skill.values()[(int) cb.getTag()];
        if (cb.isChecked()) {
            keepRule.getSkills().add(s);
        } else {
            keepRule.getSkills().remove(s);
        }
    }

    public void onAddMandatorySkillClicked(View view) {
        List<Skill> mandatorySkills = keepRule.getMandatorySkills().get(keepRule.getCategory());

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
        List<Skill> optionalSkills = keepRule.getOptionalSkills().get(keepRule.getCategory());

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
            case MANDATORY:
                Optional.ofNullable(
                        keepRule.getMandatorySkills().get(keepRule.getCategory()))
                        .ifPresent(skills -> {
                            int idx = skills.indexOf(skill);
                            if (idx >= 0) {
                                skills.remove(idx);
                                rvMandatorySkillsAdapter.notifyItemRemoved(idx);
                            }
                        });
                break;
            case OPTIONAL:
                Optional.ofNullable(
                        keepRule.getOptionalSkills().get(keepRule.getCategory()))
                        .ifPresent(skills -> {
                            int idx = skills.indexOf(skill);
                            if (idx >= 0) {
                                skills.remove(idx);
                                rvOptionalSkillsAdapter.notifyItemRemoved(idx);
                            }
                        });
                break;
            default:
        }
    }

    public void save(View view) {
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Category oldCategory = keepRule.getCategory();
        Category newCategory = SpinnerConverter.toCategory(position);

        keepRule.setCategory(newCategory);

        // Reset skills shown to skills of picked category
        int amountSkills = skills.size();
        skills.clear();
        rvAdapter.notifyItemRangeRemoved(0, amountSkills);
        skills.addAll(getSkillsFromLookupFor(newCategory));
        rvAdapter.notifyItemRangeInserted(0, skills.size());

        // Reset mandatory skills
        int amountOldMandatorySkills = keepRule.getMandatorySkills().get(oldCategory).size();
        rvMandatorySkillsAdapter.notifyItemRangeRemoved(0, amountOldMandatorySkills);
        int amountNewMandatorySkills = keepRule.getMandatorySkills().get(newCategory).size();
        rvMandatorySkillsAdapter.notifyItemRangeInserted(0, amountNewMandatorySkills);

        // Reset optional skills
        int amountOldOptionalSkills = keepRule.getOptionalSkills().get(oldCategory).size();
        rvOptionalSkillsAdapter.notifyItemRangeRemoved(0, amountOldOptionalSkills);
        int amountNewOptionalSkills = keepRule.getOptionalSkills().get(newCategory).size();
        rvOptionalSkillsAdapter.notifyItemRangeInserted(0, amountNewOptionalSkills);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // empty by design
    }

}