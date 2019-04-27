package com.example.pocketmanager;


import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.pocketmanager.ExpensesContract.Categories;
import com.example.pocketmanager.ExpensesContract.Expenses;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.realm.internal.Util;

import static android.support.constraint.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class ExpenseDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String EXTRA_EDIT_EXPENSE ="edit_expense";
    private static final int EXPENSE_LOADER_ID = 1;
    private static final int CATEGORIES_LOADER_ID = 0;
    private EditText mValueEditText;
    private Spinner mCategorySpinner;
    private SimpleCursorAdapter mAdapter;
    private Button mDateButon;
    private Button mSaveButton;
    private Button mCancelButton;
    private long mExtraValue;
    private long mExpenseCategoryId = -1;
    private Date mSelectDate= new Date();


    public ExpenseDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_expense_detail, container, false);
        mValueEditText = (EditText) view.findViewById(R.id.et_total);
        mCategorySpinner = (Spinner) view.findViewById(R.id.sp_categories);
        mDateButon=(Button) view.findViewById(R.id.btn_date);
        mSaveButton=(Button) view.findViewById(R.id.btn_save);
        mCancelButton=(Button) view.findViewById(R.id.btn_cancel);

        setEditTextDefaultValue();
        updateDate();

        // Set listener on Done (submit) button on keyboard clicked
        mValueEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    checkValueFieldForIncorrectInput();
                    return true;
                }
                return false;
            }
        });

        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mExpenseCategoryId = id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mDateButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDialog();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkForIncorrectInput()) {
                    // Create a new expense
                    if (mExtraValue < 1) {
                        insertNewExpense();

                        // Edit existing expense
                    } else {
                        updateExpense(mExtraValue);
                    }
                    Toast.makeText(getActivity(), "Changes saved", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getActivity(), "Changes not saved", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_item,
                null,
                new String[] { Categories.NAME },
                new int[] { android.R.id.text1 },
                0);
        // Specify the layout to use when the list of choices appears
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mCategorySpinner.setAdapter(mAdapter);

        mExtraValue = getActivity().getIntent().getLongExtra(EXTRA_EDIT_EXPENSE, -1);
        // Create a new expense
        if (mExtraValue < 1) {
            getActivity().setTitle(R.string.add_expense);
            loadCategories();

            // Edit existing expense
        } else {
            getActivity().setTitle(R.string.edit_expense);
            loadExpenseData();
        }
    }

    private boolean checkValueFieldForIncorrectInput() {
        String etValue = mValueEditText.getText().toString();
        try {
            if (etValue.length() == 0) {
                mValueEditText.setError(getResources().getString(R.string.error_empty_field));
                return false;
            } else if (Float.parseFloat(etValue) == 0.00f) {
                mValueEditText.setError(getResources().getString(R.string.error_zero_value));
                return false;
            }
        } catch (Exception e) {
            mValueEditText.setError(getResources().getString(R.string.incorrect_input));
            return false;
        }
        return true;
    }

    private void setEditTextDefaultValue() {
        mValueEditText.setText(String.valueOf(0));
        mValueEditText.selectAll();
    }

    private void loadCategories() {

        getLoaderManager().initLoader(CATEGORIES_LOADER_ID, null, this);
    }

    private void loadExpenseData() {
        getLoaderManager().initLoader(EXPENSE_LOADER_ID, null, this);
        loadCategories();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {

        String[] projectionFields = null;
        Uri uri = null;
        switch (i) {
            case EXPENSE_LOADER_ID:
                projectionFields = new String[] {
                        Expenses._ID,
                        Expenses.VALUE,
                        Expenses.CATEGORY_ID,
                        Expenses.DATE
                };

                uri = ContentUris.withAppendedId(Expenses.CONTENT_URI, mExtraValue);
                break;
            case CATEGORIES_LOADER_ID:
                projectionFields = new String[] {
                        Categories._ID,
                        Categories.NAME
                };

                uri = Categories.CONTENT_URI;
                break;
        }

        return new CursorLoader(getActivity(),
                uri,
                projectionFields,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case EXPENSE_LOADER_ID:
                int expenseValueIndex = cursor.getColumnIndex(Expenses.VALUE);
                int expenseCategoryIdIndex = cursor.getColumnIndex(Expenses.CATEGORY_ID);

                cursor.moveToFirst();
                mExpenseCategoryId = cursor.getLong(expenseCategoryIdIndex);
                updateSpinnerSelection();
                mDateButon.setText(cursor.getString(cursor.getColumnIndex(Expenses.DATE)));
                mValueEditText.setText(String.valueOf(cursor.getFloat(expenseValueIndex)));
                mValueEditText.selectAll();
                break;
            case CATEGORIES_LOADER_ID:

                if (null == cursor || cursor.getCount() < 1) {
                    mExpenseCategoryId = -1;
                    // Fill the spinner with default values
                    ArrayList<String> defaultItems = new ArrayList<>();
                    defaultItems.add(getResources().getString(R.string.string_no_categories));

                    ArrayAdapter<String> tempAdapter = new ArrayAdapter<String>(getActivity(),
                            android.R.layout.simple_spinner_item,
                            defaultItems);
                    mCategorySpinner.setAdapter(tempAdapter);
                    // Disable the spinner
                    mCategorySpinner.setEnabled(false);
                } else {
                    // Set the original adapter
                    mCategorySpinner.setAdapter(mAdapter);
                    // Update spinner data
                    mAdapter.swapCursor(cursor);
                    // Enable the spinner
                    mCategorySpinner.setEnabled(true);
                    updateSpinnerSelection();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        switch (loader.getId()) {
            case EXPENSE_LOADER_ID:
                mExpenseCategoryId = -1;
                setEditTextDefaultValue();
                break;
            case CATEGORIES_LOADER_ID:
                mAdapter.swapCursor(null);
                break;
        }
    }

    private boolean checkForIncorrectInput() {
        if (!checkValueFieldForIncorrectInput()) {
            mValueEditText.selectAll();
            return false;
        }
        // Future check of other fields

        return true;
    }

    private void insertNewExpense() {
        ContentValues insertValues = new ContentValues();
        insertValues.put(Expenses.VALUE, Float.parseFloat(mValueEditText.getText().toString()));
        insertValues.put(Expenses.DATE, Utils.getDateString(mSelectDate)); // Put current date (today)
        insertValues.put(Expenses.CATEGORY_ID, mExpenseCategoryId);

        getActivity().getContentResolver().insert(
                Expenses.CONTENT_URI,
                insertValues
        );
        Toast.makeText(getActivity(),
                getResources().getString(R.string.expense_added),
                Toast.LENGTH_SHORT).show();

    }




    private void updateExpense(long id) {
        ContentValues updateValues = new ContentValues();
        updateValues.put(Expenses.VALUE, Float.parseFloat(mValueEditText.getText().toString()));
        updateValues.put(Expenses.CATEGORY_ID, mExpenseCategoryId);
        updateValues.put(Expenses.DATE, Utils.getDateString(mSelectDate));

        Uri expenseUri = ContentUris.withAppendedId(Expenses.CONTENT_URI, id);

        getActivity().getContentResolver().update(
                expenseUri,
                updateValues,
                null,
                null
        );

        Toast.makeText(getActivity(),
                getResources().getString(R.string.expense_updated),
                Toast.LENGTH_SHORT).show();
    }

    private void updateSpinnerSelection() {
        mCategorySpinner.setSelection(0);
        for (int pos = 0; pos < mAdapter.getCount(); ++pos) {
            if (mAdapter.getItemId(pos) == mExpenseCategoryId) {
                // Set spinner item selected according to the value from db
                mCategorySpinner.setSelection(pos);
                break;
            }
        }
    }

    private void showDateDialog() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(mSelectDate);
        DialogManager.getInstance().showDatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(year, month, day);
                mSelectDate = calendar.getTime();
                datePicker.setMinDate(calendar.getTimeInMillis()-31536000);
                datePicker.setMaxDate(calendar.getTimeInMillis()+31536000);
                updateDate();
            }
        }, calendar);
    }

    private void updateDate() {
        mDateButon.setText(Utils.getDateString(mSelectDate));
    }

}
