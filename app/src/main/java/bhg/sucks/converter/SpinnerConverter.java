package bhg.sucks.converter;

import androidx.databinding.InverseMethod;

import bhg.sucks.model.AmountMatches;
import bhg.sucks.model.Category;

/**
 * Encapsulates methods to use enums or locales in spinners
 */
public class SpinnerConverter {

    @InverseMethod("toCategory")
    public static int toInt(Category category) {
        return category.ordinal();
    }

    public static Category toCategory(int ordinal) {
        return Category.values()[ordinal];
    }

    @InverseMethod("toAmountMatches")
    public static int toInt(AmountMatches amountMatches) {
        return amountMatches.ordinal();
    }

    public static AmountMatches toAmountMatches(int ordinal) {
        return AmountMatches.values()[ordinal];
    }

}
