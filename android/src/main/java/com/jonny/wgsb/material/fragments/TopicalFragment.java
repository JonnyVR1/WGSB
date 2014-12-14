package com.jonny.wgsb.material.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jonny.wgsb.material.MainActivity;
import com.jonny.wgsb.material.R;
import com.jonny.wgsb.material.adapter.TopicalRecyclerViewAdapter;
import com.jonny.wgsb.material.db.DatabaseHandler;
import com.jonny.wgsb.material.parser.JSONParser;
import com.jonny.wgsb.material.ui.helper.Topical;
import com.jonny.wgsb.material.ui.widget.MultiSwipeRefreshLayout;
import com.jonny.wgsb.material.util.ConnectionDetector;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TopicalFragment extends Fragment implements MultiSwipeRefreshLayout.OnRefreshListener {
    private static final String AllTopicalItemsURL = "http://app.wirralgrammarboys.com/get_topical.php";
    private static final String ERROR_URL = "http://www.wirralgrammarboys.comhttp://wirralgrammarboys.com";
    private static final String ERROR_FIX_URL = "http://wirralgrammarboys.com";
    private static final String NEWS_DIR_URL = "http://wirralgrammarboys.com/news/";
    private static final String IMAGE_DIR_URL = "http://wirralgrammarboys.com/images/newsPics/";
    private static final String ADMINIMAGES_DIR_URL = "http://wirralgrammarboys.com/admin/images/";
    private static final String NUNTIUS_DIR_URL = "http://wirralgrammarboys.com/general/nuntius";
    private static final String UPLOADS_DIR_URL = "http://wirralgrammarboys.com/uploads/";
    private static final String TAG_SUCCESS = "success";
    private static final String TOPICAL = "topical";
    private static final String TITLE = "title";
    private static final String STORY = "story";
    private static final String STAFF = "staff";
    private static final String SHOW = "show";
    private static final String RED = "red";
    JSONParser jParser = new JSONParser();
    JSONArray topicalItems = null;
    ProgressDialog mProgress;
    RecyclerView topicalListView;
    DatabaseHandler dbhandler;
    ConnectionDetector cd;
    AsyncTask<Void, Integer, Void> mLoadTopicalTask;
    private MultiSwipeRefreshLayout mSwipeRefreshLayout;
    private Integer contentAvailable = 1;
    private Boolean FlagCancelled = false, taskSuccess;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getActivity();
        cd = new ConnectionDetector(getActivity().getApplicationContext());
        dbhandler = DatabaseHandler.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topical, container, false);
        topicalListView = (RecyclerView) view.findViewById(R.id.topical_list);
        mSwipeRefreshLayout = (MultiSwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        ((MainActivity) getActivity()).setupActionBar(getString(R.string.topical_information));
        setupSwipeRefresh();
        if (dbhandler.getTopicalCount() > 0) {
            getTopicalList();
        } else {
            if (cd.isConnectingToInternet()) {
                mSwipeRefreshLayout.setRefreshing(true);
                loadTopical(true);
            } else {
                internetDialogue(getResources().getString(R.string.no_internet));
            }
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (contentAvailable == 0) {
            if (cd.isConnectingToInternet()) {
                mSwipeRefreshLayout.setRefreshing(true);
                loadTopical(true);
            } else {
                internetDialogue(getResources().getString(R.string.no_internet));
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dbhandler.getNewsCount() == 0) {
            contentAvailable = 0;
        } else {
            contentAvailable = 1;
        }
        if (mLoadTopicalTask != null && mLoadTopicalTask.getStatus() == AsyncTask.Status.PENDING) {
            FlagCancelled = true;
            mLoadTopicalTask.cancel(true);
            mLoadTopicalTask = null;
        } else if (mLoadTopicalTask != null && mLoadTopicalTask.getStatus() == AsyncTask.Status.RUNNING) {
            FlagCancelled = true;
            mLoadTopicalTask.cancel(true);
            mLoadTopicalTask = null;
        } else if (mLoadTopicalTask != null && mLoadTopicalTask.getStatus() == AsyncTask.Status.FINISHED) {
            FlagCancelled = true;
            mLoadTopicalTask = null;
        }
    }

    @Override
    public void onRefresh() {
        if (cd.isConnectingToInternet()) {
            mSwipeRefreshLayout.setRefreshing(true);
            loadTopical(false);
        } else {
            internetDialogue(getResources().getString(R.string.no_internet_refresh));
        }
    }

    private void setupSwipeRefresh() {
        mSwipeRefreshLayout.setScrollableView(topicalListView);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorFab, R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    private void internetDialogue(String string) {
        AlertDialog.Builder alertBox = new AlertDialog.Builder(mContext);
        alertBox.setIcon(R.drawable.fail);
        alertBox.setMessage(string);
        alertBox.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.android.settings", "com.android.settings.Settings");
                startActivity(intent);
            }
        });
        alertBox.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        alertBox.show();
    }

    private void getTopicalList() {
        List<HashMap<String, String>> topicalListItems = new ArrayList<>();
        List<Topical> topical = dbhandler.getAllTopical();
        for (Topical t : topical) {
            HashMap<String, String> map = new HashMap<>();
            map.put("listID", t.id.toString());
            map.put("listTitle", t.title);
            topicalListItems.add(map);
        }
        TopicalRecyclerViewAdapter adapter;
        topicalListView.setAdapter(adapter = new TopicalRecyclerViewAdapter(topicalListItems, R.layout.list_topical));
        topicalListView.setLayoutManager(new LinearLayoutManager(mContext));
        topicalListView.setItemAnimator(new DefaultItemAnimator());
        adapter.setOnItemClickListener(new TopicalRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TextView idText = (TextView) view.findViewById(R.id.topicalId);
                Integer topicalId = Integer.parseInt(idText.getText().toString());
                Bundle args = new Bundle();
                args.putInt("id", topicalId);
                ((MainActivity) getActivity()).topicalFragmentSpecific.setArguments(args);
                ((MainActivity) getActivity()).selectItem(4);
            }
        });
    }

    private void loadTopical(final Boolean firstRun) {
        mLoadTopicalTask = new AsyncTask<Void, Integer, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (firstRun) {
                    mProgress = new ProgressDialog(getActivity());
                    mProgress.setMessage("Loading Topical Information, Please wait...");
                    mProgress.setIndeterminate(false);
                    mProgress.setCancelable(true);
                    mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgress.show();
                }
            }

            @Override
            protected Void doInBackground(Void... args) {
                List<NameValuePair> params = new ArrayList<>();
                try {
                    JSONObject json = jParser.makeHttpRequest(AllTopicalItemsURL, params);
                    int success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        dbhandler.deleteAllTopical();
                        topicalItems = json.getJSONArray(TOPICAL);
                        if (firstRun) mProgress.setMax(topicalItems.length());
                        int progressCount = 0;
                        for (int i = 0; i < topicalItems.length(); i++) {
                            JSONObject obj = topicalItems.getJSONObject(i);
                            String title = obj.getString(TITLE);
                            String story = obj.getString(STORY);
                            int staff = obj.getInt(STAFF);
                            int show = obj.getInt(SHOW);
                            int red = obj.getInt(RED);
                            story = replace(story);
                            Integer id = getId(i);
                            if (staff == 0 && show == 1) {
                                if (id > dbhandler.getTopicalCount()) {
                                    dbhandler.addTopical(new Topical(id, title, story, red));
                                } else {
                                    dbhandler.updateTopical(new Topical(id, title, story, red));
                                }
                            }
                            if (isCancelled() || FlagCancelled) break;
                            progressCount++;
                            publishProgress((int) (progressCount * 100 / topicalItems.length()));
                        }
                        taskSuccess = true;
                    } else {
                        Log.e("JSON Response", "success == 0");
                        taskSuccess = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    taskSuccess = false;
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... progress) {
                super.onProgressUpdate(progress);
                if (firstRun) mProgress.setProgress(progress[0]);
            }

            @Override
            protected void onPostExecute(Void result) {
                mLoadTopicalTask = null;
                if (firstRun) mProgress.dismiss();
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        getTopicalList();
                        mSwipeRefreshLayout.setRefreshing(false);
                        if (!taskSuccess)
                            internetDialogue(getResources().getString(R.string.no_working_connection));
                    }
                });
            }
        };
        mLoadTopicalTask.execute(null, null, null);
    }

    private String replace(String story) {
        if (story.contains("/news/") && !story.contains(NEWS_DIR_URL)) {
            story = story.replaceAll("/news/", NEWS_DIR_URL);
        }
        if (story.contains("/images/newsPics/") && !story.contains(IMAGE_DIR_URL)) {
            story = story.replaceAll("/images/newsPics/", IMAGE_DIR_URL);
        }
        if (story.contains("/admin/images/") && !story.contains(ADMINIMAGES_DIR_URL)) {
            story = story.replaceAll("/admin/images/", ADMINIMAGES_DIR_URL);
        }
        if (story.contains("/general/nuntius") && !story.contains(NUNTIUS_DIR_URL)) {
            story = story.replaceAll("/general/nuntius", NUNTIUS_DIR_URL);
        }
        if (story.contains("/uploads/") && !story.contains(UPLOADS_DIR_URL)) {
            story = story.replaceAll("/uploads/", UPLOADS_DIR_URL);
        }
        if (story.contains(ERROR_URL)) {
            story = story.replaceAll(ERROR_URL, ERROR_FIX_URL);
        }
        if (story.contains("<!--<img")) {
            story = story.replaceAll("<!--<img [^>]*>-->", "");
        } else {
            story = story.replaceAll("<img [^>]*>", "");
        }
        return story;
    }

    private Integer getId(Integer i) {
        Integer id;
        if (dbhandler.getTopical(i) == null && i > 1) {
            id = i;
        } else if (dbhandler.getTopical(i - 1) == null && i > 1) {
            id = i - 1;
        } else if (dbhandler.getTopical(i - 2) == null && i > 2) {
            id = i - 2;
        } else if (dbhandler.getTopical(i - 3) == null && i > 3) {
            id = i - 3;
        } else {
            id = i + 1;
        }
        return id;
    }
}