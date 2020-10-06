package bhg.sucks.model;

import android.content.Context;

import bhg.sucks.R;

public enum Category {

    Weapon,
    Armor,
    Jewelry,
    Pottery,
    WarWeapon,
    WarArmor,
    WarEquipment;

    public String getText(Context c) {
        String[] items = c.getResources().getStringArray(R.array.array_categories);
        return items[this.ordinal()];
    }

}
