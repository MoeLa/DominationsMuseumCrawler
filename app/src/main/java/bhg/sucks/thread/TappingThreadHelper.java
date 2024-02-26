package bhg.sucks.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import bhg.sucks.helper.OcrHelper;
import bhg.sucks.model.KeepRule;
import bhg.sucks.model.KeepThreeStarOption;
import bhg.sucks.model.Skill;
import bhg.sucks.model.UpgradeResource;

/**
 * Encapsulates functionality that decides, if an item shall be kept.
 * <p>
 * Note: The {@link TappingThread} needs to decide whether to keep a scanned item or to sell it directly.
 */
public class TappingThreadHelper {

    /**
     * In the setting, the user can decide to
     * <ol>
     *     <li>not keep an artefact because of its level at all</li>
     *     <li>keep all 3* artefacts, or</li>
     *     <li>only keep 3* artefacts needing gold/food to upgrade.</li>
     * </ol>
     * Depending on that setting, an artefact is evaluated, if it should be kept because of its level.
     *
     * @return <i>true</i>, if an artefact should be kept because of its level
     */
    boolean keepingBecauseOfLevel(OcrHelper.Data data, KeepThreeStarOption keepThreeStarOption) {
        if (keepThreeStarOption == KeepThreeStarOption.No) {
            return false;
        }

        if (keepThreeStarOption == KeepThreeStarOption.Yes) {
            return data.getLevel() >= 3;
        }

        // else: keepThreeStarOption == KeepThreeStarOptions.OnlyFoodGold
        return data.getLevel() >= 3 && data.getCategory().getUpgradeResource() != UpgradeResource.Oil;
    }

    /**
     * Matches an artefact with all defined <i>keepRules</i>.
     *
     * @return <i>keepRule</i>, if data.skills matches that keep rule or<br/>
     * an empty Optional, if no keep rule matched the artefact.
     */
    Optional<KeepRule> keepingBecauseOfRule(OcrHelper.Data data, List<KeepRule> keepRules) {
        keepRuleIteration:
        for (KeepRule keepRule : keepRules) {
            int matches = 0;
            List<Skill> skillsOfItem = new ArrayList<>(data.getSkills());

            // Step 1: Check for each mandatory skill being listed
            for (Skill s : keepRule.getMandatorySkillsOfCategory()) {
                if (skillsOfItem.contains(s)) {
                    skillsOfItem.remove(s);
                    matches++;
                } else {
                    // Didn't meet a mandatory skill => continue with next keep rule
                    continue keepRuleIteration;
                }
            }

            // Step 2: Check, if remaining skills match an optional skill
            List<Skill> optionalSkills = keepRule.getOptionalSkillsOfCategory();
            for (Skill s : skillsOfItem) {
                if (optionalSkills.contains(s)) {
                    matches++;
                }
            }

            // Step 3: Check, whether we have enough matches
            if (matches >= keepRule.getAmountMatches().ordinal() + 1) {
                return Optional.of(keepRule);
            }

        }

        return Optional.empty();
    }

}
