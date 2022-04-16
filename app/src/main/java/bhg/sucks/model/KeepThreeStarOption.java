package bhg.sucks.model;

import lombok.Getter;

@Getter
public enum KeepThreeStarOption {
    No,
    OnlyFoodGold,
    Yes;

    public static String SHARED_PREF_KEY = KeepThreeStarOption.class.getName();

}
