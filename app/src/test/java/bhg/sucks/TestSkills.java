package bhg.sucks;

import com.google.common.collect.Sets;

import org.junit.Test;

import java.util.Set;

import bhg.sucks.model.Category;
import bhg.sucks.model.Skill;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class TestSkills {

    @Test
    public void noDoubles_mainHall() {
        Category[] main = new Category[]{Category.Weapon, Category.Armor, Category.Jewelry, Category.Pottery};

        Set<Integer> resIds = Sets.newHashSet();
        int count = 0;
        for (Category cat : main) {
            for (Skill s : Skill.values()) {
                if (s.getCategory() == cat) {
                    resIds.add(s.getResId());
                    count++;
                }
            }
        }

        assertEquals("Counted more main hall skills than unique 'resIds'.", count, resIds.size());
    }

    @Test
    public void noDoubles_warHall() {
        Category[] war = new Category[]{Category.WarWeapon, Category.WarArmor, Category.WarEquipment};

        Set<Integer> resIds = Sets.newHashSet();
        int count = 0;
        for (Category cat : war) {
            for (Skill s : Skill.values()) {
                if (s.getCategory() == cat) {
                    resIds.add(s.getResId());
                    count++;
                }
            }
        }

        assertEquals("Counted more war hall skills than unique 'resIds'.", count, resIds.size());
    }

}