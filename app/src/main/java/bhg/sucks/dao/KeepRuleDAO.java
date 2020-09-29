package bhg.sucks.dao;

import android.content.SharedPreferences;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import bhg.sucks.model.KeepRule;

/**
 * DAO for persisting an fetching {@link KeepRule}s.
 * <p>
 * Currently uses the shared prefs. Not beautiful, but works.
 * </p>
 */
public class KeepRuleDAO {

    private static final String KEY = "myKeepRules";
    private static final Gson gson = new Gson();

    private final SharedPreferences sharedPrefs;

    public KeepRuleDAO(SharedPreferences sharedPreferences) {
        this.sharedPrefs = sharedPreferences;
    }

    public List<KeepRule> getAll() {
        String s = sharedPrefs.getString(KEY, "[]");
        KeepRule[] rulesArray = gson.fromJson(s, KeepRule[].class);
        return Lists.newArrayList(rulesArray);
    }

    public KeepRule get(String id) {
        return getAll().stream()
                .filter(item -> item.getId().equals(id))
                .findAny()
                .orElse(null);
    }

    public KeepRule create(KeepRule keepRule) {
        List<KeepRule> keepRules = getAll();

        keepRule.setId(UUID.randomUUID().toString());
        keepRule.setPosition(keepRules.size()); // position is 0-based
        keepRules.add(keepRule);

        sharedPrefs.edit()
                .putString(KEY, gson.toJson(keepRules))
                .apply();

        return keepRule;
    }

    public KeepRule update(KeepRule keepRule) {
        List<KeepRule> keepRules = getAll();
        Optional<KeepRule> toUpdate = keepRules.stream()
                .filter(item -> item.getId().equals(keepRule.getId()))
                .findAny();

        if (!toUpdate.isPresent()) {
            return null;
        }

        KeepRule item = toUpdate.get();
        item.setName(keepRule.getName());
        item.setCategory(keepRule.getCategory());
        item.setAmountMatches(keepRule.getAmountMatches());
        item.setSkills(keepRule.getSkills());

        sharedPrefs.edit()
                .putString(KEY, gson.toJson(keepRules))
                .apply();

        return item;
    }

    public KeepRule delete(String id) {
        List<KeepRule> keepRules = getAll();
        KeepRule ret = keepRules.stream()
                .filter(item -> item.getId().equals(id))
                .findAny()
                .orElse(null);

        if (ret == null) {
            return null;
        }

        keepRules.remove(ret.getPosition());

        // After deleting a rule, we need to adjust positions of the remaining ones
        for (int i = 0; i < keepRules.size(); i++) {
            keepRules.get(i).setPosition(i);
        }

        sharedPrefs.edit()
                .putString(KEY, gson.toJson(keepRules))
                .apply();

        return ret;
    }

}
