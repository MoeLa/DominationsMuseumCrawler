package bhg.sucks.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.List;

import bhg.sucks.R;
import bhg.sucks.model.KeepRule;

/**
 * Adapter for {@link MainActivity}'s recycler view.
 */
public class KeepRulesAdapter extends RecyclerView.Adapter<KeepRulesAdapter.ViewHolder> {

    private Context context;
    private List<KeepRule> keepRules;

    public KeepRulesAdapter(List<KeepRule> keepRules) {
        this.keepRules = keepRules;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View keepRulesView = inflater.inflate(R.layout.item_keep_rule, parent, false);

        return new ViewHolder(keepRulesView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        KeepRule keepRule = keepRules.get(position);

        TextView nameView = holder.nameView;
        nameView.setText(keepRule.getName());
        nameView.setHint(keepRule.getId());

        TextView amountMatchesView = holder.amountMatchesView;
        amountMatchesView.setText(keepRule.getAmountMatches().getText(context));

        TextView categoryView = holder.categoryView;
        categoryView.setText(keepRule.getCategory().getText(context));

        MaterialIconView editView = holder.editView;
        editView.setTag(keepRule.getId());

        MaterialIconView deleteView = holder.deleteView;
        deleteView.setTag(keepRule.getId());
    }

    @Override
    public int getItemCount() {
        return keepRules.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView nameView;
        public TextView amountMatchesView;
        public TextView categoryView;
        public MaterialIconView editView;
        public MaterialIconView deleteView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.nameView = itemView.findViewById(R.id.keepRuleName);
            this.amountMatchesView = itemView.findViewById(R.id.keepRuleAmountMatches);
            this.categoryView = itemView.findViewById(R.id.keepRuleCategory);
            this.editView = itemView.findViewById(R.id.btnEdit);
            this.deleteView = itemView.findViewById(R.id.btnDelete);
        }
    }

}
