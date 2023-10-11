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
     * @return <i>true</i>, if data.level is &gt;=3
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
     * @return <i>keepRule</i>, if data.skills matches any keep rule
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
