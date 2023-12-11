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
public class TestKeepWarArmorArtefact {

    private TappingThreadHelper tappingThreadHelper;
    private KeepArtefactTestHelper helper;

    @Before
    public void setup() {
        this.tappingThreadHelper = new TappingThreadHelper();
        this.helper = new KeepArtefactTestHelper();
    }

    @Test
    public void testWarArmorMMAPX() {
        OcrHelper.Data data = OcrHelper.Data.builder()
                .skills(Lists.newArrayList(
                        Skill.WarMortarTroopHitpoints,
                        Skill.WarMortarTroopHitpoints,
                        Skill.WarRangedSiegeHitpoints,
                        Skill.WarParatroopersFromTransportsHitpoints,
                        Skill.WarHeavyInfantryHitpoints))
                .build();

        List<KeepRule> keepRules = helper.warHallMortarRules();

        Optional<KeepRule> actual = tappingThreadHelper.keepingBecauseOfRule(data, keepRules);
        assertThat(actual, isPresent());
    }

    @Test
    public void testWarArmorMAAPX() {
        OcrHelper.Data data = OcrHelper.Data.builder()
                .skills(Lists.newArrayList(
                        Skill.WarMortarTroopHitpoints,
                        Skill.WarRangedSiegeHitpoints,
                        Skill.WarRangedSiegeHitpoints,
                        Skill.WarParatroopersFromTransportsHitpoints,
                        Skill.WarHeavyInfantryHitpoints))
                .build();

        List<KeepRule> keepRules = helper.warHallMortarRules();

        Optional<KeepRule> actual = tappingThreadHelper.keepingBecauseOfRule(data, keepRules);
        assertThat(actual, isPresent());
    }

    @Test
    public void testWarArmorMAPPX() {
        OcrHelper.Data data = OcrHelper.Data.builder()
                .skills(Lists.newArrayList(
                        Skill.WarMortarTroopHitpoints,
                        Skill.WarRangedSiegeHitpoints,
                        Skill.WarParatroopersFromTransportsHitpoints,
                        Skill.WarParatroopersFromTransportsHitpoints,
                        Skill.WarHeavyInfantryHitpoints))
                .build();

        List<KeepRule> keepRules = helper.warHallMortarRules();

        Optional<KeepRule> actual = tappingThreadHelper.keepingBecauseOfRule(data, keepRules);
        assertThat(actual, isPresent());
    }

    @Test
    public void testWarArmorXAPPX() {
        OcrHelper.Data data = OcrHelper.Data.builder()
                .skills(Lists.newArrayList(
                        Skill.WarHeavyInfantryHitpoints,
                        Skill.WarRangedSiegeHitpoints,
                        Skill.WarParatroopersFromTransportsHitpoints,
                        Skill.WarParatroopersFromTransportsHitpoints,
                        Skill.WarHeavyInfantryHitpoints))
                .build();

        List<KeepRule> keepRules = helper.warHallMortarRules();

        Optional<KeepRule> actual = tappingThreadHelper.keepingBecauseOfRule(data, keepRules);
        assertThat(actual, isEmpty());
    }

}
