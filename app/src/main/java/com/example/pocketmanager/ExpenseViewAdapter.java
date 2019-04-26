package com.example.pocketmanager;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class ExpenseViewAdapter extends CursorAdapter {

    private String mCurrency;

    public ExpenseViewAdapter(Context context) {
        super(context, null, 0);
    }

    public void setCurrency(String curr){
        this.mCurrency= curr;
        notifyDataSetChanged();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        return LayoutInflater.from(context).inflate(R.layout.trans_list_item, viewGroup, false);

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView tv_amount=(TextView) view.findViewById(R.id.tv_total);
        TextView tv_cat=(TextView) view.findViewById(R.id.tv_category);
        TextView tv_date=(TextView) view.findViewById(R.id.tv_date);

        float expValue = cursor.getFloat(cursor.getColumnIndexOrThrow(ExpensesContract.Expenses.VALUE));
        String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(ExpensesContract.Categories.NAME));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(ExpensesContract.Expenses.DATE));

        tv_amount.setText(String.valueOf(expValue));
        tv_cat.setText(categoryName);
        tv_date.setText(date);


    }
}
