package bhg.sucks.model;

import android.content.Context;

import bhg.sucks.R;

public enum Category {

    Weapon(UpgradeResource.Food),
    Armor(UpgradeResource.Gold),
    Jewelry(UpgradeResource.Food),
    Pottery(UpgradeResource.Gold),
    WarWeapon(UpgradeResource.Food),
    WarArmor(UpgradeResource.Gold),
    WarEquipment(UpgradeResource.Oil);

    private final UpgradeResource upgradeResource;

    Category(UpgradeResource upgradeResource) {
        this.upgradeResource = upgradeResource;
    }

    public String getText(Context c) {
        String[] items = c.getResources().getStringArray(R.array.array_categories);
        return items[this.ordinal()];
    }

    public UpgradeResource getUpgradeResource() {
        return upgradeResource;
    }
}
