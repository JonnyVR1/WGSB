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
import com.jonny.wgsb.material.adapter.NewsRecyclerViewAdapter;
import com.jonny.wgsb.material.db.DatabaseHandler;
import com.jonny.wgsb.material.parser.JSONParser;
import com.jonny.wgsb.material.ui.helper.News;
import com.jonny.wgsb.material.ui.widget.MultiSwipeRefreshLayout;
import com.jonny.wgsb.material.util.ConnectionDetector;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewsFragment extends Fragment implements MultiSwipeRefreshLayout.OnRefreshListener {
    private static final String AllNewsItemsURL = "http://app.wirralgrammarboys.com/get_news.php";
    private static final String TAG_SUCCESS = "success";
    private static final String NEWS = "news";
    private static final String TITLE = "title";
    private static final String STORY = "story";
    private static final String IMAGE_SRC = "imageSrc";
    private static final String DATE = "date";
    private static final String ERROR_URL = "http://www.wirralgrammarboys.comhttp://wirralgrammarboys.com";
    private static final String ERROR_FIX_URL = "http://wirralgrammarboys.com";
    private static final String NEWS_DIR_URL = "http://wirralgrammarboys.com/news/";
    private static final String IMAGE_DIR_URL = "http://wirralgrammarboys.com/images/newsPics/";
    private static final String ADMINIMAGES_DIR_URL = "http://wirralgrammarboys.com/admin/images/";
    private static final String NUNTIUS_DIR_URL = "http://wirralgrammarboys.com/general/nuntius";
    private static final String UPLOADS_DIR_URL = "http://wirralgrammarboys.com/uploads/";
    JSONParser jParser = new JSONParser();
    JSONArray newsItems = null;
    ProgressDialog mProgress;
    RecyclerView newsListView;
    DatabaseHandler dbhandler;
    ConnectionDetector cd;
    AsyncTask<Void, Integer, Void> mLoadNewsTask;
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
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        newsListView = (RecyclerView) view.findViewById(R.id.news_list);
        newsListView.setLayoutManager(new LinearLayoutManager(mContext));
        newsListView.setItemAnimator(new DefaultItemAnimator());
        mSwipeRefreshLayout = (MultiSwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        ((MainActivity) getActivity()).setupActionBar(getString(R.string.news));
        setupSwipeRefresh();
        if (dbhandler.getNewsCount() > 0) {
            getNewsList();
        } else {
            if (cd.isConnectingToInternet()) {
                mSwipeRefreshLayout.setRefreshing(true);
                loadNews(true);
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
                loadNews(true);
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
        if (mLoadNewsTask != null && mLoadNewsTask.getStatus() == AsyncTask.Status.PENDING) {
            FlagCancelled = true;
            mLoadNewsTask.cancel(true);
            mLoadNewsTask = null;
        } else if (mLoadNewsTask != null && mLoadNewsTask.getStatus() == AsyncTask.Status.RUNNING) {
            FlagCancelled = true;
            mLoadNewsTask.cancel(true);
            mLoadNewsTask = null;
        } else if (mLoadNewsTask != null && mLoadNewsTask.getStatus() == AsyncTask.Status.FINISHED) {
            FlagCancelled = true;
            mLoadNewsTask = null;
        }
    }

    @Override
    public void onRefresh() {
        if (cd.isConnectingToInternet()) {
            mSwipeRefreshLayout.setRefreshing(true);
            loadNews(false);
        } else {
            internetDialogue(getResources().getString(R.string.no_internet_refresh));
        }
    }

    private void setupSwipeRefresh() {
        mSwipeRefreshLayout.setScrollableView(newsListView);
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

    private void getNewsList() {
        List<HashMap<String, String>> newsListItems = new ArrayList<>();
        List<News> news = dbhandler.getAllNews();
        for (News n : news) {
            HashMap<String, String> map = new HashMap<>();
            map.put("listID", n.id.toString());
            map.put("listTitle", n.title);
            map.put("listDate", n.date);
            map.put("listURL", n.imageSrc);
            newsListItems.add(map);
        }
        NewsRecyclerViewAdapter adapter;
        newsListView.setAdapter(adapter = new NewsRecyclerViewAdapter(newsListItems, R.layout.list_news));
        adapter.setOnItemClickListener(new NewsRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TextView idText = (TextView) view.findViewById(R.id.newsId);
                Integer newsId = Integer.parseInt(idText.getText().toString());
                Bundle args = new Bundle();
                args.putInt("id", newsId);
                ((MainActivity) getActivity()).newsFragmentSpecific.setArguments(args);
                ((MainActivity) getActivity()).selectItem(2);
            }
        });
    }

    private void loadNews(final Boolean firstRun) {
        mLoadNewsTask = new AsyncTask<Void, Integer, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (firstRun) {
                    mProgress = new ProgressDialog(mContext);
                    mProgress.setMessage("Loading news, Please wait...");
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
                    JSONObject json = jParser.makeHttpRequest(AllNewsItemsURL, params);
                    int success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        newsItems = json.getJSONArray(NEWS);
                        if (firstRun) mProgress.setMax(newsItems.length());
                        int progressCount = 0;
                        for (int i = 0; i < newsItems.length(); i++) {
                            JSONObject obj = newsItems.getJSONObject(i);
                            Integer id = i + 1;
                            String title = obj.getString(TITLE);
                            String story = obj.getString(STORY);
                            String imageSrc = IMAGE_DIR_URL + obj.getString(IMAGE_SRC).replaceAll(" ", "%20");
                            String date = obj.getString(DATE).replaceAll(" ", "");
                            story = replace(story);
                            date = buildDate(date);
                            if (id > dbhandler.getNewsCount()) {
                                dbhandler.addNews(new News(id, title, story, imageSrc, date));
                            } else {
                                dbhandler.updateNews(new News(id, title, story, imageSrc, date));
                            }
                            if (cd.isWiFiConnected())
                                Picasso.with(mContext).load(imageSrc).fetch();
                            if (isCancelled() || FlagCancelled) break;
                            progressCount++;
                            publishProgress((int) (progressCount * 100 / newsItems.length()));
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
                mLoadNewsTask = null;
                if (firstRun) mProgress.dismiss();
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        getNewsList();
                        mSwipeRefreshLayout.setRefreshing(false);
                        if (!taskSuccess)
                            internetDialogue(getResources().getString(R.string.no_working_connection));
                    }
                });
            }
        };
        mLoadNewsTask.execute(null, null, null);
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

    private String buildDate(String date) {
        String[] splitDate = splitStringEvery(date);
        Integer dayDateInt = Integer.parseInt(splitDate[0]);
        String dayDateString = (new StringBuilder(dayDateInt + getDayNumberSuffix(Integer.parseInt(splitDate[0])))).toString();
        String monthDateString = getMonthName(Integer.parseInt(splitDate[1]));
        String yearDateString = splitDate[2] + splitDate[3];
        date = dayDateString + " " + monthDateString + " " + yearDateString;
        return date;
    }

    private String[] splitStringEvery(String s) {
        int arrayLength = (int) Math.ceil(((s.length() / (double) 2)));
        String[] result = new String[arrayLength];
        int j = 0;
        int lastIndex = result.length - 1;
        for (int i = 0; i < lastIndex; i++) {
            result[i] = s.substring(j, j + 2);
            j += 2;
        }
        result[lastIndex] = s.substring(j);
        return result;
    }

    private String getDayNumberSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    private String getMonthName(int monthNo) {
        switch (monthNo) {
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            default:
                return "December";
        }
    }
}