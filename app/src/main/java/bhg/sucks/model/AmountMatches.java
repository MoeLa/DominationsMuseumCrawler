package bhg.sucks.model;

import android.content.Context;

import bhg.sucks.so.we.need.a.dominationsmuseumcrawler.R;

public enum AmountMatches {

    ONE_OF_FIVE,
    TWO_OF_FIVE,
    THREE_OF_FIVE,
    FOUR_OF_FIVE,
    FIVE_OF_FIVE;

    public String getText(Context c) {
        String[] items = c.getResources().getStringArray(R.array.array_amount_matches);
        return items[this.ordinal()];
    }

}
