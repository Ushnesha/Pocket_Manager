package com.example.pocketmanager;

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pocketmanager.ExpensesContract.Expenses;
import com.example.pocketmanager.ExpensesContract.ExpensesWithCategories;
import java.util.Date;
import java.util.List;



public class TransactionFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int SUM_LOADER_ID = 0;
    private static final int LIST_LOADER_ID = 1;

    private ListView mExpensesView;
    private ExpenseViewAdapter mAdapter;
    private TextView mTotalExpSumTextView;
    public TransactionFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_transaction, container, false);
        mExpensesView = (ListView) rootView.findViewById(R.id.trans_list_view);
        mTotalExpSumTextView = (TextView) rootView.findViewById(R.id.total_rs);

        mExpensesView.setEmptyView(rootView.findViewById(R.id.expenses_empty_list_view));
        mExpensesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                prepareExpenseToEdit(id);
            }
        });

//        rootView.findViewById(R.id.add_expense_button_if_empty_list).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                prepareExpenseToCreate();
//            }
//        });
        mTotalExpSumTextView.setText(String.valueOf(0.0f));

        registerForContextMenu(mExpensesView);

        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareExpenseToCreate();
            }
        });
        return rootView;

    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new ExpenseViewAdapter(getActivity());
        mExpensesView.setAdapter(mAdapter);

        getLoaderManager().initLoader(SUM_LOADER_ID, null, this);
        getLoaderManager().initLoader(LIST_LOADER_ID, null, this);
    }


    private void prepareExpenseToEdit(long id) {
        Intent intent = new Intent(getActivity(), ExpenseDetailActivity.class);
        intent.putExtra(ExpenseDetailFragment.EXTRA_EDIT_EXPENSE, id);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadExpenseData();
    }

    private void prepareExpenseToCreate() {
        startActivity(new Intent(getActivity(), ExpenseDetailActivity.class));
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        Uri uri = null;
        switch (i) {
            case SUM_LOADER_ID:
                uri = Expenses.EXPENSES_SUM_URI;
                break;
            case LIST_LOADER_ID:
                uri = ExpensesWithCategories.CONTENT_URI;
                break;
        }


        String today = Utils.getDateString(new Date());
        String[] selectionArgs = { today };

        return new CursorLoader(getActivity(),
                uri,
                null,
                null,
                null,
                Expenses.DATE+" DESC"
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()){
            case SUM_LOADER_ID:
                int valueSumIndex=0;
                if(cursor!=null){
                    valueSumIndex = cursor.getColumnIndex(Expenses.VALUES_SUM);
                cursor.moveToFirst();
                float valueSum = cursor.getFloat(valueSumIndex);
                mTotalExpSumTextView.setText(String.valueOf(valueSum));
                }else{
                    mTotalExpSumTextView.setText(String.valueOf(0.0f));
                }
                break;

            case LIST_LOADER_ID:
                // Hide the progress bar

                mAdapter.swapCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        switch (loader.getId()) {
            case SUM_LOADER_ID:
                mTotalExpSumTextView.setText(String.valueOf(0.0f));
                break;
            case LIST_LOADER_ID:
                mAdapter.swapCursor(null);
                break;
        }

    }

    private void reloadExpenseData() {

        getLoaderManager().restartLoader(LIST_LOADER_ID, null, this);
        getLoaderManager().restartLoader(SUM_LOADER_ID, null, this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.expense_delete_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete_expense_menu_item:
                deleteExpense(info.id);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private int deleteSingleExpense(long expenseId) {
        Uri uri = ContentUris.withAppendedId(Expenses.CONTENT_URI, expenseId);
        int rowsDeleted;
        rowsDeleted = getActivity().getContentResolver().delete(
                uri,        // the URI of the row to delete
                null,       // where clause
                null        // where args
        );

        showStatusMessage(getResources().getString(R.string.expense_deleted));
        reloadExpenseData();

        return rowsDeleted;
    }

    private void deleteExpense(final long expenseId) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_exp_dialog_msg)
                .setNeutralButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.delete_string, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteSingleExpense(expenseId);
                    }
                })
                .show();
    }
    private void showStatusMessage(CharSequence text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }
}
