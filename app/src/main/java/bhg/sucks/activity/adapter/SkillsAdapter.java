package bhg.sucks.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import bhg.sucks.R;
import bhg.sucks.model.Category;
import bhg.sucks.model.KeepRule;
import bhg.sucks.model.Skill;

/**
 * Adapter for mandatory and optional skill's recycler view.
 */
public class SkillsAdapter extends RecyclerView.Adapter<SkillsAdapter.ViewHolder> {

    private final KeepRule keepRule;
    private final SkillsAdapterKey key;
    private final Map<Category, List<Skill>> skillsMap;

    public SkillsAdapter(KeepRule keepRule, SkillsAdapterKey key) {
        this.keepRule = keepRule;
        this.key = key;

        switch (key) {
            case MANDATORY:
                this.skillsMap = keepRule.getMandatorySkills();
                break;
            case OPTIONAL:
                this.skillsMap = keepRule.getOptionalSkills();
                break;
            default:
                this.skillsMap = new HashMap<>();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Optional.ofNullable(skillsMap.get(keepRule.getCategory()))
                .ifPresent(skills -> {
                    Skill skill = skills.get(position);
                    holder.skill.setText(skill.getResId());
                    holder.deleteButton.setTag(R.id.TAG_SKILL_DELETE_BUTTON_KEY, key);
                    holder.deleteButton.setTag(R.id.TAG_SKILL_DELETE_BUTTON_SKILL, skill);
                });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View skillsView = inflater.inflate(R.layout.item_skill, parent, false);

        return new ViewHolder(skillsView);
    }

    @Override
    public int getItemCount() {
        List<Skill> skills = skillsMap.get(keepRule.getCategory());
        if (skills == null) {
            return 0;
        }

        return skills.size();
    }

    public enum SkillsAdapterKey {
        MANDATORY, OPTIONAL
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView skill;
        public MaterialIconView deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.skill = itemView.findViewById(R.id.skill);
            this.deleteButton = itemView.findViewById(R.id.skillDeleteButton);
        }
    }
}
