package bhg.sucks.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import bhg.sucks.model.KeepRule;
import bhg.sucks.model.Skill;
import bhg.sucks.so.we.need.a.dominationsmuseumcrawler.R;

/**
 * Adapter for {@link CreateKeepRuleActivity}'s recycler view.
 */
public class SelectSkillsAdapter extends RecyclerView.Adapter<SelectSkillsAdapter.ViewHolder> {

    private final List<Skill> skills;
    private final KeepRule keepRule;

    public SelectSkillsAdapter(List<Skill> skills, KeepRule keepRule) {
        this.skills = skills;
        this.keepRule = keepRule;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View skillsView = inflater.inflate(R.layout.item_select_skill, parent, false);

        return new ViewHolder(skillsView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Skill skill = skills.get(position);

        CheckBox cbSkill = holder.cbSkill;
        cbSkill.setText(skill.getResId());
        cbSkill.setTag(skill.ordinal());
        cbSkill.setChecked(keepRule.getSkills().contains(skill));
    }

    @Override
    public int getItemCount() {
        return skills.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CheckBox cbSkill;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.cbSkill = itemView.findViewById(R.id.cbSkill);
        }
    }

}
