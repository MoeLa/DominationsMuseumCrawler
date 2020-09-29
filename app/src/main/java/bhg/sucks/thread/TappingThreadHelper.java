package bhg.sucks.thread;

import java.util.List;

import bhg.sucks.helper.OcrHelper;
import bhg.sucks.model.KeepRule;
import bhg.sucks.model.Skill;

/**
 * Encapsulates functionality that decides, if an item shall be kept.
 * <p>
 * Note: The {@link TappingThread} needs to decide whether to keep a scanned item or to sell it directly.
 */
public class TappingThreadHelper {

    /**
     * @return <i>true</i>, if data.level is &gt;=3
     */
    boolean keepingBecauseOfLevel(OcrHelper.Data data) {
        return data.getLevel() >= 3;
    }

    /**
     * @return <i>true</i>, if data.skills matches any keep rule
     */
    boolean keepingBecauseOfRule(OcrHelper.Data data, List<KeepRule> keepRules) {
        for (KeepRule keepRule : keepRules) {
            int matches = 0;
            for (Skill s : data.getSkills()) {
                if (keepRule.getSkills().contains(s)) {
                    matches++;
                }
            }

            if (matches >= keepRule.getAmountMatches().ordinal() + 1) {
                return true;
            }

        }

        return false;
    }

}
