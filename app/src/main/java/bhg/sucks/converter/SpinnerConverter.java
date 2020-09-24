package bhg.sucks.converter;

import androidx.databinding.InverseMethod;

import java.util.Locale;

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

    @InverseMethod("toLocale")
    public static int toInt(Locale lang) {
        if (Locale.GERMANY.equals(lang)) {
            return 1;
        }

        return 0;
    }

    public static Locale toLocale(int ordinal) {
        switch (ordinal) {
            case 0:
                return Locale.US;
            case 1:
                return Locale.GERMANY;
            default:
                throw new RuntimeException("Unsupported ordinal " + ordinal);
        }
    }

}
