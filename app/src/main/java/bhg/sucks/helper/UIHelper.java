package bhg.sucks.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bhg.sucks.model.Category;
import bhg.sucks.model.Skill;

public class UIHelper {

    /**
     * Creates a map filled with an entry for each {@link Category} with an empty list.
     */
    public static Map<Category, List<Skill>> createEmptySkillsMap() {
        Map<Category, List<Skill>> result = new HashMap<>();
        for (Category c : Category.values()) {
            result.put(c, new ArrayList<>());
        }

        return result;
    }
}
