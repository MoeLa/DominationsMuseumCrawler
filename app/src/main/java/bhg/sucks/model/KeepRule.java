package bhg.sucks.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Model for RecyclerView.
 */
public class KeepRule {

    private String id;
    private String name;
    private Category category;
    private Map<Category, List<Skill>> mandatorySkills;
    private Map<Category, List<Skill>> optionalSkills;
    private AmountMatches amountMatches;
    private int position;

    private KeepRule(Builder builder) {
        setId(builder.id);
        setName(builder.name);
        setCategory(builder.category);
        setMandatorySkills(builder.mandatorySkills);
        setOptionalSkills(builder.optionalSkills);
        setAmountMatches(builder.amountMatches);
        setPosition(builder.position);
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<Skill> getMandatorySkillsOfCategory() {
        return Optional.ofNullable(mandatorySkills.get(category)).orElse(new ArrayList<>());
    }

    public List<Skill> getOptionalSkillsOfCategory() {
        return Optional.ofNullable(optionalSkills.get(category)).orElse(new ArrayList<>());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Map<Category, List<Skill>> getMandatorySkills() {
        return mandatorySkills;
    }

    public void setMandatorySkills(Map<Category, List<Skill>> mandatorySkills) {
        this.mandatorySkills = mandatorySkills;
    }

    public Map<Category, List<Skill>> getOptionalSkills() {
        return optionalSkills;
    }

    public void setOptionalSkills(Map<Category, List<Skill>> optionalSkills) {
        this.optionalSkills = optionalSkills;
    }

    public AmountMatches getAmountMatches() {
        return amountMatches;
    }

    public void setAmountMatches(AmountMatches amountMatches) {
        this.amountMatches = amountMatches;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public static final class Builder {
        private String id;
        private String name;
        private Category category;
        private Map<Category, List<Skill>> mandatorySkills;
        private Map<Category, List<Skill>> optionalSkills;
        private AmountMatches amountMatches;
        private int position;

        private Builder() {
        }

        public Builder id(String val) {
            id = val;
            return this;
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder category(Category val) {
            category = val;
            return this;
        }

        public Builder mandatorySkills(Map<Category, List<Skill>> val) {
            mandatorySkills = val;
            return this;
        }

        public Builder optionalSkills(Map<Category, List<Skill>> val) {
            optionalSkills = val;
            return this;
        }

        public Builder amountMatches(AmountMatches val) {
            amountMatches = val;
            return this;
        }

        public Builder position(int val) {
            position = val;
            return this;
        }

        public KeepRule build() {
            return new KeepRule(this);
        }
    }
}
