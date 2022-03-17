package bhg.sucks.thread;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import bhg.sucks.helper.OcrHelper;
import bhg.sucks.model.AmountMatches;
import bhg.sucks.model.Category;
import bhg.sucks.model.KeepRule;
import bhg.sucks.model.Skill;

public class TestTappingThreadHelper {

    private TappingThreadHelper tappingThreadHelper;

    @Before
    public void setup() {
        this.tappingThreadHelper = new TappingThreadHelper();
    }

    @Test
    public void testKeepingBecauseOfLevel_Success() {
        OcrHelper.Data data = OcrHelper.Data.builder()
                .level(3)
                .build();

        boolean actual = tappingThreadHelper.keepingBecauseOfLevel(data);
        assertThat(actual, is(true));
    }

    @Test
    public void testKeepingBecauseOfLevel_levelTooLow() {
        OcrHelper.Data data = OcrHelper.Data.builder()
                .level(2)
                .build();

        boolean actual = tappingThreadHelper.keepingBecauseOfLevel(data);
        assertThat(actual, is(false));
    }

    @Test
    public void testKeepingBecauseOfRule_Success() {
        OcrHelper.Data data = OcrHelper.Data.builder()
                .skills(Lists.newArrayList(
                        Skill.GuerrillaHitpoints,
                        Skill.GuerrillaHitpoints,
                        Skill.FighterDamage,
                        Skill.FighterDamage,
                        Skill.APCDamage))
                .build();
        List<KeepRule> keepRules = Collections.singletonList(KeepRule.builder()
                .category(Category.Weapon)
                .amountMatches(AmountMatches.FOUR_OF_FIVE)
                .mandatorySkills(Maps.newHashMap())
                .optionalSkills(ImmutableMap.of(Category.Weapon, Lists.newArrayList(Skill.GuerrillaHitpoints, Skill.FighterDamage)))
                .build());

        boolean actual = tappingThreadHelper.keepingBecauseOfRule(data, keepRules);
        assertThat(actual, is(true));
    }

    @Test
    public void testKeepingBecauseOfRule_tooFewMatches() {
        OcrHelper.Data data = OcrHelper.Data.builder()
                .skills(Lists.newArrayList(
                        Skill.GuerrillaHitpoints,
                        Skill.GuerrillaHitpoints,
                        Skill.BazookaDamage,
                        Skill.FighterDamage,
                        Skill.APCDamage))
                .build();
        List<KeepRule> keepRules = Collections.singletonList(KeepRule.builder()
                .category(Category.Weapon)
                .amountMatches(AmountMatches.FOUR_OF_FIVE)
                .mandatorySkills(Maps.newHashMap())
                .optionalSkills(ImmutableMap.of(Category.Weapon, Lists.newArrayList(Skill.GuerrillaHitpoints, Skill.FighterDamage)))
                .build());

        boolean actual = tappingThreadHelper.keepingBecauseOfRule(data, keepRules);
        assertThat(actual, is(false));
    }

}
