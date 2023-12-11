package bhg.sucks.thread;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import bhg.sucks.helper.OcrHelper;
import bhg.sucks.model.KeepRule;
import bhg.sucks.model.Skill;

/**
 * This class gathers test cases that intensively test the algorithm to decide whether to keep or drop an artefact.
 */
public class TestKeepWarEquipmentArtefact {

    private TappingThreadHelper tappingThreadHelper;
    private KeepArtefactTestHelper helper;

    @Before
    public void setup() {
        this.tappingThreadHelper = new TappingThreadHelper();
        this.helper = new KeepArtefactTestHelper();
    }

    @Test
    public void testWarEquipmentDDHHX() {
        OcrHelper.Data data = OcrHelper.Data.builder()
                .skills(Lists.newArrayList(
                        Skill.WarAllEnemyDefensiveTowersDamage,
                        Skill.WarAllEnemyDefensiveTowersDamage,
                        Skill.WarAllEnemyDefensiveTowersHitpoints,
                        Skill.WarAllEnemyDefensiveTowersHitpoints,
                        Skill.WarRedoubtDamage))
                .build();

        List<KeepRule> keepRules = helper.warHallMortarRules();

        Optional<KeepRule> actual = tappingThreadHelper.keepingBecauseOfRule(data, keepRules);
        assertThat(actual, isPresent());
    }

    @Test
    public void testWarEquipmentHHSSX() {
        OcrHelper.Data data = OcrHelper.Data.builder()
                .skills(Lists.newArrayList(
                        Skill.WarAllEnemyDefensiveTowersHitpoints,
                        Skill.WarAllEnemyDefensiveTowersHitpoints,
                        Skill.WarEnemyDefenderSpawnTime,
                        Skill.WarEnemyDefenderSpawnTime,
                        Skill.WarRedoubtDamage))
                .build();

        List<KeepRule> keepRules = helper.warHallMortarRules();

        Optional<KeepRule> actual = tappingThreadHelper.keepingBecauseOfRule(data, keepRules);
        assertThat(actual, isPresent());
    }

    @Test
    public void testWarEquipmentSSXDD() {
        OcrHelper.Data data = OcrHelper.Data.builder()
                .skills(Lists.newArrayList(
                        Skill.WarEnemyDefenderSpawnTime,
                        Skill.WarEnemyDefenderSpawnTime,
                        Skill.WarRedoubtDamage,
                        Skill.WarAllEnemyDefensiveTowersDamage,
                        Skill.WarAllEnemyDefensiveTowersDamage))
                .build();

        List<KeepRule> keepRules = helper.warHallMortarRules();

        Optional<KeepRule> actual = tappingThreadHelper.keepingBecauseOfRule(data, keepRules);
        assertThat(actual, isPresent());
    }

    @Test
    public void testWarEquipmentSXXDD() {
        OcrHelper.Data data = OcrHelper.Data.builder()
                .skills(Lists.newArrayList(
                        Skill.WarEnemyDefenderSpawnTime,
                        Skill.WarRedoubtDamage,
                        Skill.WarRedoubtDamage,
                        Skill.WarAllEnemyDefensiveTowersDamage,
                        Skill.WarAllEnemyDefensiveTowersDamage))
                .build();

        List<KeepRule> keepRules = helper.warHallMortarRules();

        Optional<KeepRule> actual = tappingThreadHelper.keepingBecauseOfRule(data, keepRules);
        assertThat(actual, isEmpty());
    }

}
