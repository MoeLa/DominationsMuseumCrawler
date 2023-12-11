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
public class TestKeepWarWeaponArtefact {

    private TappingThreadHelper tappingThreadHelper;
    private KeepArtefactTestHelper helper;

    @Before
    public void setup() {
        this.tappingThreadHelper = new TappingThreadHelper();
        this.helper = new KeepArtefactTestHelper();
    }

    @Test
    public void testWarWeaponMMAPX() {
        OcrHelper.Data data = OcrHelper.Data.builder()
                .skills(Lists.newArrayList(
                        Skill.WarMortarTroopDamage,
                        Skill.WarMortarTroopDamage,
                        Skill.WarRangedSiegeDamage,
                        Skill.WarParatroopersFromTransportsDamage,
                        Skill.WarHeavyInfantryDamage))
                .build();

        List<KeepRule> keepRules = helper.warHallMortarRules();

        Optional<KeepRule> actual = tappingThreadHelper.keepingBecauseOfRule(data, keepRules);
        assertThat(actual, isPresent());
    }

    @Test
    public void testWarWeaponMAAPX() {
        OcrHelper.Data data = OcrHelper.Data.builder()
                .skills(Lists.newArrayList(
                        Skill.WarMortarTroopDamage,
                        Skill.WarRangedSiegeDamage,
                        Skill.WarRangedSiegeDamage,
                        Skill.WarParatroopersFromTransportsDamage,
                        Skill.WarHeavyInfantryDamage))
                .build();

        List<KeepRule> keepRules = helper.warHallMortarRules();

        Optional<KeepRule> actual = tappingThreadHelper.keepingBecauseOfRule(data, keepRules);
        assertThat(actual, isPresent());
    }

    @Test
    public void testWarWeaponMAPPX() {
        OcrHelper.Data data = OcrHelper.Data.builder()
                .skills(Lists.newArrayList(
                        Skill.WarMortarTroopDamage,
                        Skill.WarRangedSiegeDamage,
                        Skill.WarParatroopersFromTransportsDamage,
                        Skill.WarParatroopersFromTransportsDamage,
                        Skill.WarHeavyInfantryDamage))
                .build();

        List<KeepRule> keepRules = helper.warHallMortarRules();

        Optional<KeepRule> actual = tappingThreadHelper.keepingBecauseOfRule(data, keepRules);
        assertThat(actual, isPresent());
    }

    @Test
    public void testWarWeaponMAPXX() {
        OcrHelper.Data data = OcrHelper.Data.builder()
                .skills(Lists.newArrayList(
                        Skill.WarMortarTroopDamage,
                        Skill.WarRangedSiegeDamage,
                        Skill.WarParatroopersFromTransportsDamage,
                        Skill.WarHeavyInfantryDamage,
                        Skill.WarHeavyInfantryDamage))
                .build();

        List<KeepRule> keepRules = helper.warHallMortarRules();

        Optional<KeepRule> actual = tappingThreadHelper.keepingBecauseOfRule(data, keepRules);
        assertThat(actual, isEmpty());
    }

}
