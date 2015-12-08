package com.krp.android.recyclerwithviewpager;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alexvasilkov.android.commons.utils.GsonHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ContentsActivity extends AppCompatActivity implements View.OnClickListener,
        OnMainContentItemActionListener {
    public static final String TAG = ContentsActivity.class.getSimpleName();

    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mPager;
    private TextView mDoneBtn;

    private Spinner mSpinner;
    private CardView mCardAssignee;

    private ProgressDialog mDialog;
    private boolean shouldRequestLoader;

    private boolean isDefaultSelection; // is default system selection
    private AssigneeSpinnerAdapter mSpinnerAdapter;
    private MainContentsRecyclerPagerAdapter mPagerAdapter;

    private final List<String> LIST_TAB_TITLE = new ArrayList<>(3);
    private final Map<Integer, Set<Integer>> MAP_MAIN_CONTENTS_SELECTED = new HashMap<>(LIST_TAB_TITLE.size());
    private final Map<Integer, Integer> MAP_ASSIGNEES_SELECTED = new HashMap<>(LIST_TAB_TITLE.size());

    private final int INQUIRY_TYPE_1 = 0;
    private final int INQUIRY_TYPE_2 = 1;
    private final int INQUIRY_TYPE_3 = 2;
    /**
     * Class init() method to load tab titles
     */
    {
        LIST_TAB_TITLE.add("Content-I");
        LIST_TAB_TITLE.add("Content-II");
        LIST_TAB_TITLE.add("Content-III");

        MAP_MAIN_CONTENTS_SELECTED.put(INQUIRY_TYPE_1, new HashSet<Integer>(5)); // temp minimum capacity 5
        MAP_MAIN_CONTENTS_SELECTED.put(INQUIRY_TYPE_2, new HashSet<Integer>(5)); // temp minimum capacity 5
        MAP_MAIN_CONTENTS_SELECTED.put(INQUIRY_TYPE_3, new HashSet<Integer>(5)); // temp minimum capacity 5

        MAP_ASSIGNEES_SELECTED.put(INQUIRY_TYPE_1, 0);
        MAP_ASSIGNEES_SELECTED.put(INQUIRY_TYPE_2, 0);
        MAP_ASSIGNEES_SELECTED.put(INQUIRY_TYPE_3, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contents);

        setupToolbar();
        setupViewPager();
        setupTabLayout();
        setupProgressDialog();
        setupSpinner();
        setupAssignButton();

        shouldRequestLoader = true;
        mCardAssignee.setEnabled(false);
    }

    private void setupAssignButton() {
        mDoneBtn = (TextView) findViewById(R.id.btn_done);
        mDoneBtn.setEnabled(false); // initially disabling button
        mDoneBtn.setAlpha(0.6f);    // initially decreasing alpha
        mDoneBtn.setOnClickListener(this);
    }

    private void setupSpinner() {
        mCardAssignee = (CardView) findViewById(R.id.card_assignee);
        mSpinner = (Spinner) mCardAssignee.findViewById(R.id.sp_assign);

        mSpinnerAdapter = new AssigneeSpinnerAdapter();
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long assigneeId) {
                if (!isDefaultSelection) { // if not selected by system
                    onAssigneeSelected((int) assigneeId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // nothing selected
            }
        });
    }

    private void setupProgressDialog() {
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading...");
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
    }

    private void setupTabLayout() {
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabLayout.setupWithViewPager(mPager);
        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    private void setupViewPager() {
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new MainContentsRecyclerPagerAdapter(this, LIST_TAB_TITLE, this);
        mPager.setAdapter(mPagerAdapter);
        mPager.setOffscreenPageLimit(3);
        mPager.addOnPageChangeListener(mOnPageChangeListener);
    }

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        ActionBar abr = getSupportActionBar();
        abr.setDisplayHomeAsUpEnabled(true);
        abr.setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_unassigned_leads, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_filter:
                return true;

            default: return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_done:
                if(Network.isNetworkAvailable(this)) {
                    new AssignContents().execute(getParamsForAssign(getInquiryType(mPager.getCurrentItem())));
                } else {
                    alert("No internet connection.");
                }
                break;

            default: return;
        }
    }

    /**
     * Show progress dialog
     */
    protected void showProgressDialog() {
        if(mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
        }
    }

    /**
     * Dismiss progress dialog
     */
    protected void dismissProgressDialog() {
        if(mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    /**
     * Show message in toast as alert
     * @param message
     */
    protected void alert(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Start async tasks in parallel
     * ...
     * Should load assignee list in background
     * On the same period loads the unassigned leads
     * @param task
     * @param params
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void startAsyncTaskInParallel(AsyncTask task, String... params) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        else
            task.execute(params);
    }

    /**
     * Request params for unassigned leads
     * @param contentType
     */
    private String[] getParamsForMainContents(int contentType) {
        return new String[] {
                SharedPrefs.getString(this, SharedPrefs.PREFS_AUTH, SharedPrefs.KEY_APP, ""), // key
                String.valueOf(contentType),   // inquiryType
                SharedPrefs.getString(this, SharedPrefs.PREFS_AUTH, SharedPrefs.KEY_USER_ID, "") };   // userId
    }

    /**
     * Request params for unassigned leads
     * @param contentType
     */
    private String[] getParamsForAssignees(int contentType) {
        return new String[] {
                SharedPrefs.getString(this, SharedPrefs.PREFS_AUTH, SharedPrefs.KEY_APP, ""), // key
                String.valueOf(contentType) };   // inquiryType
    }


    /**
     * Request params for assign leads to assignee
     * @param contentType
     */
    private String[] getParamsForAssign(int contentType) {
        Set<Integer> selectedContents = null;
        int pageSelected = mPager.getCurrentItem();
        switch (pageSelected) {
            case INQUIRY_TYPE_1:
            case INQUIRY_TYPE_2:
            case INQUIRY_TYPE_3:
                selectedContents = MAP_MAIN_CONTENTS_SELECTED.get(pageSelected);
                break;
        }

        StringBuilder contentIds = new StringBuilder();
        for(Integer contentId : selectedContents) {
            if(contentIds.length() == 0) {
                contentIds.append(contentId);
                continue;
            }
            contentIds.append(",").append(contentId);
        }

        return new String[] {
                SharedPrefs.getString(this, SharedPrefs.PREFS_AUTH, SharedPrefs.KEY_APP, ""), // key
                String.valueOf(MAP_ASSIGNEES_SELECTED.get(pageSelected)),       // assignedTo
                contentIds.toString(),     // inqLeadIds
                SharedPrefs.getString(this, SharedPrefs.PREFS_AUTH, SharedPrefs.KEY_USER_ID, "") }; // loggedInUser
    }

    /**
     * Request body for INQUIRY_TYPE: SELLER(2), USED_CAR_BUYER(1), NEW_CAR_BUYER(3)
     * @param position
     */
    private int getInquiryType(int position) {
        switch (position) {
            case 0: // Content-I --> Tab position
                return  2; // Content-I type : 2

            case 1: // Content-II --> Tab position
                return 1; // Content-II type : 1

            case 2: // Content-III --> Tab position
                return 3; // Content-III type : 3

            default: return -1;
        }
    }

    /**
     * PagerAdapter --> OnScrollListener
     * ----
     * To show progress dialog while
     * Loading contents
     */
    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if(shouldRequestLoader) {
                // execute main contents loader in executor
                startAsyncTaskInParallel(new MainContentsLoader(),
                        getParamsForMainContents(getInquiryType(position)));
                // execute assignee loader in executor
                startAsyncTaskInParallel(new AssigneeLoader(),
                        getParamsForAssignees(getInquiryType(position)));
            }
            isDefaultSelection = true;
            mSpinner.setSelection(0); // set default setection to hint "Select assignee"
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if(state == ViewPager.SCROLL_STATE_IDLE) {
                shouldRequestLoader = true;
                // execute main content loader in executor
                startAsyncTaskInParallel(new MainContentsLoader(),
                        getParamsForMainContents(getInquiryType(mPager.getCurrentItem())));
                // execute assignee loader in executor
                startAsyncTaskInParallel(new AssigneeLoader(),
                        getParamsForAssignees(getInquiryType(mPager.getCurrentItem())));
            } else {
                shouldRequestLoader = false;
            }
        }
    };

    /**
     * Callback on lead selected
     * @param customerId
     * @param selected
     */
    @Override
    public void onMainContentSelected(int customerId, boolean selected) {
        int pageSelected = mPager.getCurrentItem();
        switch (pageSelected) {
            case INQUIRY_TYPE_1:
            case INQUIRY_TYPE_2:
            case INQUIRY_TYPE_3:
                if(selected) {
                    MAP_MAIN_CONTENTS_SELECTED.get(pageSelected)
                            .add(customerId); // add main content ID
                } else {
                    MAP_MAIN_CONTENTS_SELECTED.get(pageSelected)
                            .remove(customerId); // remove main content ID
                }

                // enabling assign button
                if(MAP_ASSIGNEES_SELECTED.get(pageSelected) != -1 && MAP_ASSIGNEES_SELECTED.get(pageSelected) != 0 &&
                        MAP_MAIN_CONTENTS_SELECTED.get(pageSelected).size() != 0) {
                    mDoneBtn.setEnabled(true);
                    mDoneBtn.setAlpha(1);
                } else {
                    mDoneBtn.setEnabled(false);
                    mDoneBtn.setAlpha(0.6f);
                }
                break;
        }
    }

    /**
     * Callback on call button clicked
     * @param customerId
     * @param number
     */
    @Override
    public void onCall(int customerId, String number) {
        try {
            ContactUtil.callPhone(this, number);
        } catch (Exception e) {
            alert("Please try later!!");
        }
    }

    /**
     * Callback on assignee selected
     * @param assigneeId
     */
    public void onAssigneeSelected(int assigneeId) {
        int pageSelected = mPager.getCurrentItem();
        switch (pageSelected) {
            case INQUIRY_TYPE_1:
            case INQUIRY_TYPE_2:
            case INQUIRY_TYPE_3:
                MAP_ASSIGNEES_SELECTED.put(pageSelected, assigneeId);

                // enabling assign button
                if(assigneeId != -1 && assigneeId != 0 &&
                        MAP_MAIN_CONTENTS_SELECTED.get(pageSelected).size() != 0) {
                    mDoneBtn.setEnabled(true);
                    mDoneBtn.setAlpha(1);
                } else {
                    mDoneBtn.setEnabled(false);
                    mDoneBtn.setAlpha(0.6f);
                }
                break;
        }
    }

    /**
     * AsyncTask --> Main contents loader
     */
    protected class MainContentsLoader extends AsyncTask<String, String, Set<MainContent>> {
        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected Set<MainContent> doInBackground(String... params) {
            Map<String, String> entities = new HashMap<>();
            entities.put("key", params[0]);
            entities.put("contentType", params[1]);
            entities.put("userId", params[2]);

            Set<MainContent> leads = null;   // TODO using set to remove duplicate entries if any
            try {
                JSONArray jsonArray = new JSONArray("[" +
                        "{" +
                        "\"customerId\":\"1\"," +
                        "\"customerName\":\"Ram\"," +
                        "\"customerMobile\":\"1234567890\"," +
                        "\"CarDetails\":\"Main content\"," +
                        "\"LeadDate\":\"10 Jan 2015\"" +
                        "}," +
                        "{" +
                        "\"customerId\":\"2\"," +
                        "\"customerName\":\"Ram\"," +
                        "\"customerMobile\":\"1234567890\"," +
                        "\"CarDetails\":\"Main content\"," +
                        "\"LeadDate\":\"10 Jan 2015\"" +
                        "}," +
                        "{" +
                        "\"customerId\":\"3\"," +
                        "\"customerName\":\"Ram\"," +
                        "\"customerMobile\":\"1234567890\"," +
                        "\"CarDetails\":\"Main content\"," +
                        "\"LeadDate\":\"10 Jan 2015\"" +
                        "}," +
                        "{" +
                        "\"customerId\":\"4\"," +
                        "\"customerName\":\"Ram\"," +
                        "\"customerMobile\":\"1234567890\"," +
                        "\"CarDetails\":\"Main content\"," +
                        "\"LeadDate\":\"10 Jan 2015\"" +
                        "}," +
                        "{" +
                        "\"customerId\":\"5\"," +
                        "\"customerName\":\"Ram\"," +
                        "\"customerMobile\":\"1234567890\"," +
                        "\"CarDetails\":\"Main content\"," +
                        "\"LeadDate\":\"10 Jan 2015\"" +
                        "}," +
                        "{" +
                        "\"customerId\":\"6\"," +
                        "\"customerName\":\"Ram\"," +
                        "\"customerMobile\":\"1234567890\"," +
                        "\"CarDetails\":\"Main content\"," +
                        "\"LeadDate\":\"10 Jan 2015\"" +
                        "}," +
                        "{" +
                        "\"customerId\":\"7\"," +
                        "\"customerName\":\"Ram\"," +
                        "\"customerMobile\":\"1234567890\"," +
                        "\"CarDetails\":\"Main content\"," +
                        "\"LeadDate\":\"10 Jan 2015\"" +
                        "}," +
                        "{" +
                        "\"customerId\":\"8\"," +
                        "\"customerName\":\"Ram\"," +
                        "\"customerMobile\":\"1234567890\"," +
                        "\"CarDetails\":\"Main content\"," +
                        "\"LeadDate\":\"10 Jan 2015\"" +
                        "}," +
                        "{" +
                        "\"customerId\":\"9\"," +
                        "\"customerName\":\"Ram\"," +
                        "\"customerMobile\":\"1234567890\"," +
                        "\"CarDetails\":\"Main content\"," +
                        "\"LeadDate\":\"10 Jan 2015\"" +
                        "}," +
                        "{" +
                        "\"customerId\":\"10\"," +
                        "\"customerName\":\"Ram\"," +
                        "\"customerMobile\":\"1234567890\"," +
                        "\"CarDetails\":\"Main content\"," +
                        "\"LeadDate\":\"10 Jan 2015\"" +
                        "}," +
                        "{" +
                        "\"customerId\":\"11\"," +
                        "\"customerName\":\"Ram\"," +
                        "\"customerMobile\":\"1234567890\"," +
                        "\"CarDetails\":\"Main content\"," +
                        "\"LeadDate\":\"10 Jan 2015\"" +
                        "}," +
                        "{" +
                        "\"customerId\":\"12\"," +
                        "\"customerName\":\"Ram\"," +
                        "\"customerMobile\":\"1234567890\"," +
                        "\"CarDetails\":\"Main content\"," +
                        "\"LeadDate\":\"10 Jan 2015\"" +
                        "}," +
                        "{" +
                        "\"customerId\":\"13\"," +
                        "\"customerName\":\"Ram\"," +
                        "\"customerMobile\":\"1234567890\"," +
                        "\"CarDetails\":\"Main content\"," +
                        "\"LeadDate\":\"10 Jan 2015\"" +
                        "}," +
                        "{" +
                        "\"customerId\":\"14\"," +
                        "\"customerName\":\"Ram\"," +
                        "\"customerMobile\":\"1234567890\"," +
                        "\"CarDetails\":\"Main content\"," +
                        "\"LeadDate\":\"10 Jan 2015\"" +
                        "}" +
                        "]");

                if(jsonArray.length() != 0) {
                    leads = new HashSet<>();
                    for(int index=0; index < jsonArray.length(); index++) {
                        leads.add(GsonHelper.fromJson(jsonArray.getString(index), MainContent.class));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return leads;
        }

        @Override
        protected void onPostExecute(Set<MainContent> contents) {
            dismissProgressDialog();

            if(contents == null) {
                return;
            }

            int pageSelected = mPager.getCurrentItem();
            switch (pageSelected) {
                case INQUIRY_TYPE_1:
                case INQUIRY_TYPE_2:
                case INQUIRY_TYPE_3:
                    Set<Integer> selectedContents = MAP_MAIN_CONTENTS_SELECTED.get(pageSelected);
                    if(selectedContents != null && selectedContents.size() > 0) {
                        for(MainContent content : contents) {
                            if(selectedContents.contains(content.customerId)) {
                                content.selected = true;
                            }
                        }
                    }
                    break;
            }

            // notify the updated main contents: 1> update from server; 2> update if already selected previously
            mPagerAdapter.notifyContentUpdates(contents);
        }
    }


    /**
     * AsyncTask --> Assignee loader
     */
    protected class AssigneeLoader extends AsyncTask<String, String, Set<Assignee>> {
        @Override
        protected void onPreExecute() {
            mCardAssignee.setEnabled(false);
        }

        @Override
        protected Set<Assignee> doInBackground(String... params) {
            Map<String, String> entities = new HashMap<>();
            entities.put("key", params[0]);
            entities.put("contentType", params[1]);

            Set<Assignee> leads = null;   // TODO using set to remove duplicate entries if any
            try {
                JSONArray jsonArray = new JSONArray("[" +
                        "{" +
                        "\"userId\":\"1\"," +
                        "\"name\":\"Suresh\"" +
                        "}," +
                        "{" +
                        "\"userId\":\"1\"," +
                        "\"name\":\"Mahesh\"" +
                        "}," +
                        "{" +
                        "\"userId\":\"1\"," +
                        "\"name\":\"Rajesh\"" +
                        "}," +
                        "{" +
                        "\"userId\":\"1\"," +
                        "\"name\":\"Rupesh\"" +
                        "}," +
                        "{" +
                        "\"userId\":\"1\"," +
                        "\"name\":\"Surendar\"" +
                        "}," +
                        "{" +
                        "\"userId\":\"1\"," +
                        "\"name\":\"Mahendar\"" +
                        "}" +
                        "]");

                if(jsonArray.length() != 0) {
                    leads = new TreeSet<>();
                    for(int index=0; index < jsonArray.length(); index++) {
                        leads.add(GsonHelper.fromJson(jsonArray.getString(index), Assignee.class));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return leads;
        }

        @Override
        protected void onPostExecute(Set<Assignee> assignees) {
            mCardAssignee.setEnabled(true); // enable spinner to be selected

            if(assignees == null) {
                return;
            }

            // notify the updated assignees
            mSpinnerAdapter.add(assignees);
            mSpinnerAdapter.notifyDataSetChanged();

            int pageSelected = mPager.getCurrentItem();
            switch (pageSelected) {
                case INQUIRY_TYPE_1:
                case INQUIRY_TYPE_2:
                case INQUIRY_TYPE_3:
                    isDefaultSelection = false;
                    mSpinner.setSelection(mSpinnerAdapter.getAssigneePosition(
                            MAP_ASSIGNEES_SELECTED.get(pageSelected)));
                    break;
            }
        }
    }

    /**
     * AsyncTask --> Assign contents to assignee
     */
    protected class AssignContents extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected String doInBackground(String... params) {
            Map<String, String> entities = new HashMap<>();
            entities.put("key", params[0]);
            entities.put("sendTo", params[1]);
            entities.put("contentIds", params[2]);
            entities.put("userId", params[3]);

            String response = null;
            try {
                response = "Successfully assigned";

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            dismissProgressDialog();

            if(response == null) {
                alert((response == null? "" : response+"!! ") +"Please try again.");
                return;
            }

            // alert success message and finish the screen
            alert(response);
            finish();
        }
    }
}


/**
 * DataTransferObject --> UnassignedLeads
 */
class MainContent implements Comparable<MainContent> {
    int customerId;   // 13673
    String customerName;  // unknown
    String customerMobile;  // 2525646415
    String CarDetails;    // Aston Martin DB9 Coupe
    String LeadDate;  // 2015-10-27T20:17:35.427
    boolean selected;   // whether selected to assign or not

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MainContent that = (MainContent) o;

        return this.customerId == that.customerId;

    }

    @Override
    public int hashCode() {
        return customerId;
    }

    @Override
    public int compareTo(MainContent another) {
        return this.LeadDate.compareTo(another.LeadDate);
    }
}


/**
 * DataTransferObject --> AssignedUser
 */
class Assignee implements Comparable<Assignee> {
    int userId;   // 18440
    String name;  // zinnia

    public Assignee(String name, int userId) {
        this.name = name;
        this.userId = userId;
    }

    /**
     * toString() --> used as passing assignee names to the spinner adapter
     * @return
     */
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Assignee another) {
        return this.name.compareTo(another.name);
    }
}


/**
 * Callback for actions on unassigned leads
 */
interface OnMainContentItemActionListener {
    void onMainContentSelected(int customerId, boolean selected);
    void onCall(int customerId, String number);
}


/**
 * RecyclerView --> ViewHolder
 */
class UnassignedLeadsRecyclerViewHolder extends RecyclerView.ViewHolder {
    private TextView tvName, tvCall, tvStock, tvTime;
    private LinearLayout mLayoutCall;
    private CheckBox cbSelected;
    private int customerId;

    private OnMainContentItemActionListener actionListener;

    public UnassignedLeadsRecyclerViewHolder(View itemView, OnMainContentItemActionListener listener) {
        super(itemView);
        actionListener = listener;

        tvName = (TextView) itemView.findViewById(R.id.tv_name);
        tvCall = (TextView) itemView.findViewById(R.id.tv_call);
        tvStock = (TextView) itemView.findViewById(R.id.tv_stock);
        tvTime = (TextView) itemView.findViewById(R.id.tv_time);
        cbSelected = (CheckBox) itemView.findViewById(R.id.cb_selected);
        mLayoutCall = (LinearLayout) itemView.findViewById(R.id.layout_call);

        if(actionListener != null) {
            cbSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    actionListener.onMainContentSelected(customerId, isChecked); // here no use of customerId
                }
            });
        }
    }

    public void setValues(final int customerId, String leadName, final String mobileNumber,
                          String stockDetails, String time, boolean selected) {
        this.customerId = customerId;
        tvName.setText(leadName);
        tvStock.setText(stockDetails);
        tvTime.setText(time);
        cbSelected.setChecked(selected);

        try {
            long number = Long.valueOf(mobileNumber);
            tvCall.setText(mobileNumber);
            if(actionListener != null) {
                mLayoutCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        actionListener.onCall(customerId, mobileNumber);
                    }
                });
            }
        } catch (NumberFormatException e) {
            mLayoutCall.setVisibility(View.INVISIBLE);
            mLayoutCall.setOnClickListener(null);
        }
    }
}


/**
 * RecyclerView --> RecyclerViewAdapter
 */
class MainContentsRecyclerAdapter extends RecyclerView.Adapter<UnassignedLeadsRecyclerViewHolder> {
    private List<MainContent> mListUnassignedLeads;
    private OnMainContentItemActionListener actionListener;

    public MainContentsRecyclerAdapter(List<MainContent> listUnassignedLeads,
                                       OnMainContentItemActionListener listener) {
        mListUnassignedLeads = listUnassignedLeads;
        actionListener = listener;
    }

    @Override
    public UnassignedLeadsRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UnassignedLeadsRecyclerViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_layout_main_contents, parent, false),
                actionListener);
    }

    @Override
    public void onBindViewHolder(UnassignedLeadsRecyclerViewHolder holder, int position) {
        MainContent lead = mListUnassignedLeads.get(position);

        holder.setValues(
                lead.customerId,
                lead.customerName,
                lead.customerMobile,
                lead.CarDetails,
                lead.LeadDate,
                lead.selected);
    }

    @Override
    public int getItemCount() {
        return mListUnassignedLeads == null ? 0 : mListUnassignedLeads.size();
    }

    /**
     * notify content updates
     */
    public void notifyContentUpdates(Set<MainContent> leads) {
        if(mListUnassignedLeads == null) {
            mListUnassignedLeads = new ArrayList<>(leads);
        } else {
            mListUnassignedLeads.removeAll(mListUnassignedLeads);
            mListUnassignedLeads.addAll(leads);
        }
        notifyDataSetChanged();
    }
}


/**
 * ViewPager --> PagerAdapter
 */
class MainContentsRecyclerPagerAdapter extends PagerAdapter {
    private Context mContext;
    private ViewPager mViewPager;
    private LayoutInflater mLayoutInflater;

    private List<String> mListTagTitle;
    private static final int MAX_SPAN_COUNT = 2;

    private OnMainContentItemActionListener actionListener;

    public MainContentsRecyclerPagerAdapter(Context context, List<String> listTagTitle,
                                            OnMainContentItemActionListener listener) {
        this.mContext = context;
        this.mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.mListTagTitle = listTagTitle;
        actionListener = listener;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RecyclerView) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        mViewPager = (ViewPager) container;
        RecyclerView recyclerView = (RecyclerView) mLayoutInflater.inflate(R.layout.recycler, container, false);
        recyclerView.setAdapter(new MainContentsRecyclerAdapter(null, actionListener));
        recyclerView.setLayoutManager(new GridLayoutManager(mViewPager.getContext(), MAX_SPAN_COUNT));

        container.addView(recyclerView);
        return recyclerView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RecyclerView) object);
    }

    @Override
    public int getCount() {
        return mListTagTitle == null ? 0 : mListTagTitle.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mListTagTitle.get(position);
    }

    /**
     * Content update callback
     */
    public void notifyContentUpdates(Set<MainContent> leads) {
        RecyclerView recyclerView = (RecyclerView) mViewPager.getChildAt(mViewPager.getCurrentItem());
        ((MainContentsRecyclerAdapter) recyclerView.getAdapter()).notifyContentUpdates(leads);
        notifyDataSetChanged();
    }
}


/**
 * Spinner --> AssigneeSpinnerAdapter
 */
class AssigneeSpinnerAdapter extends BaseAdapter {
    private List<Assignee> assignees;

    public AssigneeSpinnerAdapter() {
        assignees = new ArrayList<>();
        assignees.add(0, new Assignee(" -- Select assignee -- ", -1));
    }

    public void add(Collection<? extends Assignee> collection) {
        assignees.removeAll(assignees);
        assignees.add(0, new Assignee(" -- Select assignee -- ", -1));
        assignees.addAll(collection);
    }

    /**
     * returns --> assigneePosition in the list adapter
     * @param assigneeId
     * @return
     */
    public int getAssigneePosition(int assigneeId) {
        for(Assignee assignee : assignees) {
            if(assignee.userId == assigneeId) {
                return assignees.indexOf(assignee);
            }
        }
        return -1;
    }

    @Override
    public int getCount() {
        return assignees == null ? 0 : assignees.size();
    }

    @Override
    public Object getItem(int position) {
        return assignees.get(position);
    }

    @Override
    public long getItemId(int position) {
        return assignees.get(position).userId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.source_spinner_dropdown_item, parent, false);
        }
        TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
        tv.setText(assignees.get(position).name);
        return convertView;
    }
}