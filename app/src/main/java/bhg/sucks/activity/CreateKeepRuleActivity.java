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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import bhg.sucks.converter.SpinnerConverter;
import bhg.sucks.dao.KeepRuleDAO;
import bhg.sucks.model.AmountMatches;
import bhg.sucks.model.Category;
import bhg.sucks.model.KeepRule;
import bhg.sucks.model.Skill;
import bhg.sucks.so.we.need.a.dominationsmuseumcrawler.R;
import bhg.sucks.so.we.need.a.dominationsmuseumcrawler.databinding.ActivityCreateKeepRuleBinding;

/**
 * Activity to define a new {@link KeepRule} or update an existing one.
 */
public class CreateKeepRuleActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "CreateKeepRuleActivity";

    private Map<Category, List<Skill>> skillsLookup;
    private List<Skill> skills;
    private KeepRuleDAO dao;
    private KeepRule keepRule;
    private SelectSkillsAdapter rvAdapter;

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
            Collections.sort(skills, Comparator.comparing(s -> getString(s.getResId())));
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
        Category category = SpinnerConverter.toCategory(position);
        keepRule.setCategory(category);

        skills.clear();
        skills.addAll(getSkillsFromLookupFor(category));
        rvAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // empty by design
    }

}