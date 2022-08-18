package bhg.sucks.model;


public enum KeepThreeStarOption {
    No,
    OnlyFoodGold,
    Yes;

    public static String SHARED_PREF_KEY = KeepThreeStarOption.class.getName();

}
