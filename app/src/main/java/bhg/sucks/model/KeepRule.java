package bhg.sucks.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model for RecyclerView.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KeepRule {

    private String id;
    private String name;
    private Category category;
    private Map<Category, List<Skill>> mandatorySkills;
    private Map<Category, List<Skill>> optionalSkills;
    private AmountMatches amountMatches;
    private int position;

    public List<Skill> getMandatorySkillsOfCategory() {
        return Optional.ofNullable(mandatorySkills.get(category)).orElse(new ArrayList<>());
    }

    public List<Skill> getOptionalSkillsOfCategory() {
        return Optional.ofNullable(optionalSkills.get(category)).orElse(new ArrayList<>());
    }

}
