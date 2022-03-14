package bhg.sucks.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Set<Skill> skills;
    private Map<Category, List<Skill>> mandatorySkills;
    private Map<Category, List<Skill>> optionalSkills;
    private AmountMatches amountMatches;
    private int position;

}
