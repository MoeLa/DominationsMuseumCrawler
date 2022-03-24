package bhg.sucks.matcher;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import bhg.sucks.model.AmountMatches;
import bhg.sucks.model.Category;
import bhg.sucks.model.KeepRule;
import bhg.sucks.model.Skill;

/**
 * Provides Hamcrest matcher for checking a {@link KeepRule}.
 */
public class KeepRuleMatcher {

    /**
     * Tests the attributes: id, name, category, skills, amountMatches and position
     */
    public static Matcher<KeepRule> sameWithId(KeepRule keepRule) {
        return allOf(
                id(equalTo(keepRule.getId())),
                same(keepRule)
        );
    }

    /**
     * Tests the business attributes: name, category, skills, amountMatches and position
     */
    public static Matcher<KeepRule> same(KeepRule keepRule) {
        return allOf(
                name(equalTo(keepRule.getName())),
                category(equalTo(keepRule.getCategory())),
                mandatorySkills(containsInAnyOrder(keepRule.getMandatorySkillsOfCategory().toArray(new Skill[0]))),
                optionalSkills(containsInAnyOrder(keepRule.getOptionalSkillsOfCategory().toArray(new Skill[0]))),
                amountMatches(equalTo(keepRule.getAmountMatches())),
                position(equalTo(keepRule.getPosition()))
        );
    }

    public static Matcher<KeepRule> id(Matcher<String> matcher) {
        return new FeatureMatcher<KeepRule, String>(matcher, "KeepRule with id =", "id") {
            @Override
            protected String featureValueOf(KeepRule actual) {
                return actual.getId();
            }
        };
    }

    public static Matcher<KeepRule> name(Matcher<String> matcher) {
        return new FeatureMatcher<KeepRule, String>(matcher, "KeepRule with name =", "name") {
            @Override
            protected String featureValueOf(KeepRule actual) {
                return actual.getName();
            }
        };
    }

    public static Matcher<KeepRule> category(Matcher<Category> matcher) {
        return new FeatureMatcher<KeepRule, Category>(matcher, "KeepRule with category =", "category") {
            @Override
            protected Category featureValueOf(KeepRule actual) {
                return actual.getCategory();
            }
        };
    }

    public static Matcher<KeepRule> mandatorySkills(Matcher<Iterable<? extends Skill>> matcher) {
        return new FeatureMatcher<KeepRule, Iterable<? extends Skill>>(matcher, "KeepRule with mandatorySkills =", "mandatorySkills") {
            @Override
            protected Iterable<? extends Skill> featureValueOf(KeepRule actual) {
                return actual.getMandatorySkillsOfCategory();
            }
        };
    }

    public static Matcher<KeepRule> optionalSkills(Matcher<Iterable<? extends Skill>> matcher) {
        return new FeatureMatcher<KeepRule, Iterable<? extends Skill>>(matcher, "KeepRule with optionalSkills =", "optionalSkills") {
            @Override
            protected Iterable<? extends Skill> featureValueOf(KeepRule actual) {
                return actual.getOptionalSkillsOfCategory();
            }
        };
    }

    public static Matcher<KeepRule> amountMatches(Matcher<AmountMatches> matcher) {
        return new FeatureMatcher<KeepRule, AmountMatches>(matcher, "KeepRule with amountMatches =", "amountMatches") {
            @Override
            protected AmountMatches featureValueOf(KeepRule actual) {
                return actual.getAmountMatches();
            }
        };
    }

    public static Matcher<KeepRule> position(Matcher<Integer> matcher) {
        return new FeatureMatcher<KeepRule, Integer>(matcher, "KeepRule with position =", "position") {
            @Override
            protected Integer featureValueOf(KeepRule actual) {
                return actual.getPosition();
            }
        };
    }

}
