package com.example.pocketmanager;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

public class OverviewFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        PopupMenu.OnMenuItemClickListener {
    private static final int SUM_LOADER_ID = 0;
    private static final int LIST_LOADER_ID = 1;

    private static final String REPORT_TYPE = "report_type";
    private static final String SELECTION_ARGS = "selection_args";
    private static final int DATE_REPORT = 10;
    private static final int DATE_RANGE_REPORT = 11;

    private ListView mExpensesListView;
    private ExpenseViewAdapter mAdapter;
    private Date startDate=new Date();
    private String startDateString;
    private String endDateString;
    private String dateString;
    private Date endDate = new Date();
    private View mProgressBar;
    private Button strtBtn;
    private Button endBtn;
    private LinearLayout rangeDate;
    private TextView dateShow;
    private TextView mTotalValueTextView;
    private TextView mTotalCurrencyTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_overview, container, false);

        mExpensesListView = (ListView) rootView.findViewById(R.id.expenses_report_list_view);
        mTotalValueTextView = (TextView) rootView.findViewById(R.id.expenses_report_total_text_view);
        strtBtn=(Button)rootView.findViewById(R.id.btn_start);
        endBtn=(Button)rootView.findViewById(R.id.btn_end);
        dateShow=(TextView) rootView.findViewById(R.id.tv_dateShow);
        rangeDate=(LinearLayout) rootView.findViewById(R.id.rangeDateLayout);

        mExpensesListView.setEmptyView(rootView.findViewById(R.id.expenses_report_empty_list_view));
        mTotalValueTextView.setText(String.valueOf(0.0f));
        strtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDateString=showDateDialog();
                strtBtn.setText(startDateString);
            }
        });
        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endDateString=showDateDialog();
                endBtn.setText(endDateString);
                makeDateRangeReport();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new ExpenseViewAdapter(getActivity());
        mExpensesListView.setAdapter(mAdapter);

        initLoaders();
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadReportData();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_report, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter_expenses_menu_item:
                View menuItemView = getActivity().findViewById(R.id.filter_expenses_menu_item);
                showPopupMenu(menuItemView);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.setOnMenuItemClickListener(OverviewFragment.this);

        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.report_filter_popup, popup.getMenu());
        popup.show();
    }

    /* from PopupMenu.OnMenuItemClickListener */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.today_filter_option:
                makeTodaysReport();
                return true;
            case R.id.week_filter_option:
                makeWeeklyReport();
                return true;
            case R.id.month_filter_option:
                makeMonthlyReport();
                return true;
            case R.id.date_filter_option:
                makeDateReport();
                return true;
            case R.id.range_filter_option:
                rangeDate.setVisibility(View.VISIBLE);
                return true;
            default:
                return false;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int reportType = args.getInt(REPORT_TYPE);
        String[] selectionArgs = args.getStringArray(SELECTION_ARGS);
        Uri uri = null;
        switch (id) {
            case SUM_LOADER_ID:
                if (reportType == DATE_REPORT) {
                    uri = ExpensesContract.ExpensesWithCategories.SUM_DATE_CONTENT_URI;
                } else if (reportType == DATE_RANGE_REPORT) {
                    uri = ExpensesContract.ExpensesWithCategories.SUM_DATE_RANGE_CONTENT_URI;
                }
                break;
            case LIST_LOADER_ID:
                if (reportType == DATE_REPORT) {
                    uri = ExpensesContract.ExpensesWithCategories.DATE_CONTENT_URI;
                } else if (reportType == DATE_RANGE_REPORT) {
                    uri = ExpensesContract.ExpensesWithCategories.DATE_RANGE_CONTENT_URI;
                }
                break;
        }

        return new CursorLoader(getActivity(),
                uri,
                null,
                null,
                selectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case SUM_LOADER_ID:
                int valueSumIndex = data.getColumnIndex(ExpensesContract.Expenses.VALUES_SUM);
                data.moveToFirst();
                float valueSum = data.getFloat(valueSumIndex);
                mTotalValueTextView.setText(String.valueOf(valueSum));
                break;

            case LIST_LOADER_ID:
                mAdapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }



    private void reloadReportData() {
        makeTodaysReport();
    }

    private void makeTodaysReport() {
        String today = Utils.getDateString(new Date());
        dateShow.setText(today);

        String[] selectionArgs = { today };

        restartLoaders(DATE_REPORT, selectionArgs);
    }

    private void makeWeeklyReport() {
        Calendar calendar = Calendar.getInstance();
        Date todayDate = new Date();
        calendar.setTime(todayDate); // Set today
        calendar.add(Calendar.DAY_OF_YEAR, -7); // Subtract 7 days from today
        String startDate = Utils.getDateString(calendar.getTime());
        String endDate = Utils.getDateString(todayDate);

        dateShow.setText(startDate+"-"+endDate);
        String[] selectionArgs = { startDate, endDate };

        restartLoaders(DATE_RANGE_REPORT, selectionArgs);
    }

    private void makeMonthlyReport() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set the first day of month to 1
        String startDate = Utils.getDateString(calendar.getTime()); // Get start of month
        String endDate = Utils.getDateString(new Date());

        getActivity().setTitle(getString(R.string.filter_months_expenses));

        String[] selectionArgs = { startDate, endDate };
        dateShow.setText(startDate+"-"+endDate);

        restartLoaders(DATE_RANGE_REPORT, selectionArgs);
    }

    private void makeDateReport() {
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                String dateString = Utils.getDateString(calendar.getTime());
                String systemFormatDateStr = Utils.getSystemFormatDateString(getActivity(),
                        calendar.getTime());

                getActivity().setTitle(getString(R.string.filter_date_expenses, systemFormatDateStr));

                String[] selectionArgs = { dateString };

                restartLoaders(DATE_REPORT, selectionArgs);
                dateShow.setText(dateString);
            }
        };

        DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(listener);
        datePickerFragment.show(getActivity().getSupportFragmentManager(), "date_picker");
//        dateString=showDateDialog();
//        dateShow.setText(dateString);
    }



    private void makeDateRangeReport() {
        final DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                String startDateStr = Utils.getDateString(startDate);
                String endDateStr = Utils.getDateString(calendar.getTime());
                String sysFormatEndDateStr = Utils.getSystemFormatDateString(getActivity(),
                        calendar.getTime());
                String sysFormatStartDateStr = Utils.getSystemFormatDateString(getActivity(), startDate);

                getActivity().setTitle(getString(R.string.filter_date_range_expenses,
                        sysFormatStartDateStr, sysFormatEndDateStr));

                String[] selectionArgs = { startDateStr, endDateStr };

                restartLoaders(DATE_RANGE_REPORT, selectionArgs);
            }
        };

        DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                startDate = calendar.getTime();

               DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(endDateListener);
               datePickerFragment.show(getActivity().getSupportFragmentManager(), "end_date_picker");
            }
        };

        DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(startDateListener);
        datePickerFragment.show(getActivity().getSupportFragmentManager(), "start_date_picker");
    }

    private void restartLoaders(int reportType, String[] selectionArgs) {
        Bundle args = createBundleArgs(reportType, selectionArgs);

        getLoaderManager().restartLoader(SUM_LOADER_ID, args, this);
        getLoaderManager().restartLoader(LIST_LOADER_ID, args, this);
    }

    private void initLoaders() {
        // Retrieve today's date string
        String today = Utils.getDateString(new Date());
        String[] selectionArgs = { today };

        Bundle args = createBundleArgs(DATE_REPORT, selectionArgs);

        // Initialize the CursorLoaders
        getLoaderManager().initLoader(SUM_LOADER_ID, args, this);
        getLoaderManager().initLoader(LIST_LOADER_ID, args, this);
    }

    private Bundle createBundleArgs(int reportType, String[] selectionArgs) {
        Bundle args = new Bundle();
        args.putInt(REPORT_TYPE, reportType);
        args.putStringArray(SELECTION_ARGS, selectionArgs);
        return args;
    }

    private String showDateDialog() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        DialogManager.getInstance().showDatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(year, month, day);
                startDate = calendar.getTime();
            }
        }, calendar);
        return Utils.getDateString(startDate);
    }
}
