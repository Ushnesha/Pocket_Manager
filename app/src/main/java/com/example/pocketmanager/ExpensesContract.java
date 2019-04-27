package com.example.pocketmanager;

import android.net.Uri;
import android.provider.BaseColumns;

public class ExpensesContract {

    public static final String AUTHORITY = "com.example.pocketmanager.provider";

    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    private ExpensesContract(){}

    public static class Categories implements BaseColumns, CategoriesColumns {

        private Categories() {}

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "categories");

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.pocketmanager.provider.expense_category";

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.pocketmanager.provider.expense_category";

        public static final String DEFAULT_SORT_ORDER = _ID + " ASC";
    }

    public static class Expenses implements BaseColumns, ExpensesColumns {

        private Expenses() {}

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "expenses");

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.pocketmanager.provider.expense";

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.pocketmanager.provider.expense";

        public static final String DEFAULT_SORT_ORDER = DATE + " ASC";

        public static final String VALUES_SUM = "values_sum";

        public static final Uri EXPENSES_SUM_URI=Uri.withAppendedPath(CONTENT_URI,"sum");
    }

    public static class ExpensesWithCategories implements BaseColumns {

        private ExpensesWithCategories() {}

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "expensesWithCategories");

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.pocketmanager.provider.expense_with_category";

        public static final Uri DATE_CONTENT_URI = Uri.withAppendedPath(CONTENT_URI, "date");

        public static final Uri DATE_RANGE_CONTENT_URI = Uri.withAppendedPath(CONTENT_URI, "dateRange");

        public static final Uri SUM_DATE_CONTENT_URI = Uri.withAppendedPath(DATE_CONTENT_URI, "sum");

        public static final Uri SUM_DATE_RANGE_CONTENT_URI =
                Uri.withAppendedPath(DATE_RANGE_CONTENT_URI, "sum");
        public static final Uri SUM_CONTENT_URI = Uri.withAppendedPath(CONTENT_URI, "sum");

    }


    protected interface CategoriesColumns {
        String NAME = "name";
    }

    protected interface ExpensesColumns {
        String VALUE = "value";
        String DATE = "date";
        String CATEGORY_ID = "category_id";
    }

}
