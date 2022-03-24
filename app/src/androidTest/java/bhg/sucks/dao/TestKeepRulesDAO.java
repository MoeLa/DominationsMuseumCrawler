package bhg.sucks.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import bhg.sucks.helper.UIHelper;
import bhg.sucks.matcher.KeepRuleMatcher;
import bhg.sucks.model.AmountMatches;
import bhg.sucks.model.Category;
import bhg.sucks.model.KeepRule;
import bhg.sucks.model.Skill;

@RunWith(AndroidJUnit4.class)
public class TestKeepRulesDAO {

    private KeepRuleDAO dao;
    private KeepRule kr0;
    private KeepRule kr1;
    private KeepRule kr2;

    @Before
    public void setup() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        SharedPreferences sharedPrefs = ctx.getSharedPreferences("test", Context.MODE_PRIVATE);
        sharedPrefs.edit().clear().commit();

        this.dao = new KeepRuleDAO(sharedPrefs);

        // Create three dummy keep rules to set up some data
        this.kr0 = dao.create(KeepRule.builder().mandatorySkills(Collections.emptyMap()).build());
        this.kr1 = dao.create(KeepRule.builder()
                .category(Category.WarEquipment)
                .mandatorySkills(UIHelper.createEmptySkillsMap())
                .optionalSkills(UIHelper.createEmptySkillsMap())
                .build());
        this.kr2 = dao.create(KeepRule.builder()
                .category(Category.Armor)
                .mandatorySkills(UIHelper.createEmptySkillsMap())
                .optionalSkills(UIHelper.createEmptySkillsMap())
                .build());
    }

    @Test
    public void testCreate_Success() {
        // Prepare
        KeepRule toCreate = KeepRule.builder()
                .name("a Name")
                .category(Category.Weapon)
                .mandatorySkills(ImmutableMap.of(Category.Weapon, ImmutableList.of(Skill.APCDamage)))
                .optionalSkills(ImmutableMap.of(Category.Weapon, ImmutableList.of(Skill.APCHitpoints)))
                .amountMatches(AmountMatches.ONE_OF_FIVE)
                .build();

        // Execute
        KeepRule created = dao.create(toCreate);

        // Check
        KeepRule expected = KeepRule.builder()
                .name("a Name")
                .category(Category.Weapon)
                .amountMatches(AmountMatches.ONE_OF_FIVE)
                .mandatorySkills(ImmutableMap.of(Category.Weapon, ImmutableList.of(Skill.APCDamage)))
                .optionalSkills(ImmutableMap.of(Category.Weapon, ImmutableList.of(Skill.APCHitpoints)))
                .position(3)
                .build();

        assertThat(created.getId(), is(not(nullValue())));
        assertThat(created, is(KeepRuleMatcher.same(expected)));
    }

    @Test
    public void testUpdate_Success() {
        // Prepare
        KeepRule toUpdate = KeepRule.builder()
                .id(kr1.getId())
                .name("updated")
                .category(Category.Armor)
                .amountMatches(AmountMatches.TWO_OF_FIVE)
                .mandatorySkills(ImmutableMap.of(Category.Armor, ImmutableList.of(Skill.AirDefenseHitpoints, Skill.BazookaHitpoints)))
                .optionalSkills(ImmutableMap.of(Category.Armor, ImmutableList.of(Skill.AirDefenseDamage, Skill.BazookaDamage)))
                .build();

        // Execute
        KeepRule updated = dao.update(toUpdate);
        KeepRule updatedFromStorage = dao.get(kr1.getId());

        // Check
        KeepRule expected = KeepRule.builder()
                .id(kr1.getId())
                .name("updated")
                .category(Category.Armor)
                .amountMatches(AmountMatches.TWO_OF_FIVE)
                .mandatorySkills(ImmutableMap.of(Category.Armor, ImmutableList.of(Skill.AirDefenseHitpoints, Skill.BazookaHitpoints)))
                .optionalSkills(ImmutableMap.of(Category.Armor, ImmutableList.of(Skill.AirDefenseDamage, Skill.BazookaDamage)))
                .position(kr1.getPosition())
                .build();

        assertThat(updated, is(KeepRuleMatcher.sameWithId(expected)));
        assertThat(updatedFromStorage, is(KeepRuleMatcher.sameWithId(expected)));
    }

    @Test
    public void testUpdate_idNotFound() {
        // Prepare
        KeepRule toUpdate = KeepRule.builder()
                .id("unknown")
                .build();

        // Execute
        KeepRule updated = dao.update(toUpdate);

        // Check
        assertThat(updated, is(nullValue()));
    }

    @Test
    public void testDelete_Success() {
        // Execute
        KeepRule deleted = dao.delete(kr0.getId());

        // Check
        List<KeepRule> keepRulesAfterDeletion = dao.getAll();

        KeepRule expected = KeepRule.builder()
                .id(kr1.getId())
                .category(Category.WarEquipment)
                .mandatorySkills(Maps.newHashMap())
                .optionalSkills(Maps.newHashMap())
                .position(0)
                .build();
        KeepRule expected1 = KeepRule.builder()
                .id(kr2.getId())
                .category(Category.Armor)
                .mandatorySkills(Maps.newHashMap())
                .optionalSkills(Maps.newHashMap())
                .position(1)
                .build();

        assertThat(deleted.getId(), is(kr0.getId()));
        assertThat(keepRulesAfterDeletion, contains(KeepRuleMatcher.sameWithId(expected), KeepRuleMatcher.sameWithId(expected1)));
    }

    @Test
    public void testDelete_idNotFound() {
        // Execute
        KeepRule updated = dao.delete("unknown");

        // Check
        assertThat(updated, is(nullValue()));
    }

    @Test
    public void testGetAll_Success() {
        // Execute
        List<KeepRule> actual = dao.getAll();

        // Check
        assertThat(actual, hasSize(3));
    }

    @Test
    public void testGet_Success() {
        // Execute
        KeepRule actual = dao.get(kr2.getId());

        // Check
        KeepRule expected = KeepRule.builder()
                .id(kr2.getId())
                .category(Category.Armor)
                .mandatorySkills(Maps.newHashMap())
                .optionalSkills(Maps.newHashMap())
                .position(2)
                .build();
        assertThat(actual, is(KeepRuleMatcher.sameWithId(expected)));
    }

}
