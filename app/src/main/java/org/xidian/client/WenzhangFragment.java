package org.xidian.client;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import org.json.JSONObject;
import org.xidian.client.view.AutoLoadRecyclerView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 文章列表主界面,包含自动滚动广告栏、文章列表
 *
 * @author mrsimple
 */
public class WenzhangFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        AutoLoadRecyclerView.OnLoadListener {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";

    final protected List<Article> mDataSet = new ArrayList<Article>();
    //    protected int mCategory = Article.ALL;
    protected ArticleAdapter mAdapter;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected AutoLoadRecyclerView mRecyclerView;
    private int mPageIndex = 1;
    private StringBuffer reader;

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        initRefreshView(rootView);
        initAdapter();
        mSwipeRefreshLayout.setRefreshing(true);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mDataSet.addAll(DatabaseHelper.getInstance().loadArticles());
        mAdapter.notifyDataSetChanged();
    }

    protected void initRefreshView(View rootView) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (AutoLoadRecyclerView) rootView.findViewById(R.id.articles_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()
                .getApplicationContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setOnLoadListener(this);
    }

    protected void initAdapter() {
        mAdapter = new ArticleAdapter(mDataSet);
        mAdapter.setOnItemClickListener(new OnItemClickListener<Article>() {

            @Override
            public void onClick(Article article) {
                if (article != null) {
                    loadArticle(article);
                }
            }
        });
        // 设置Adapter
        mRecyclerView.setAdapter(mAdapter);
        getArticles(1);
    }

    public void setArticleCategory(int category) {
//        mCategory = category;
    }

    private void getArticles(final int page) {
        new AsyncTask<Void, Void, List<Article>>() {

            protected void onPreExecute() {
                mSwipeRefreshLayout.setRefreshing(true);
            }

            @Override
            protected List<Article> doInBackground(Void... params) {
                return performRequest(page);
            }

            protected void onPostExecute(List<Article> result) {
                // 移除已经更新的数据
                result.removeAll(mDataSet);
                // 添加心数据
                mDataSet.addAll(result);
                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
                // 存储文章列表
                DatabaseHelper.getInstance().saveArticles(result);
                if (result.size() > 0) {
                    mPageIndex++;
                }
            }
        }.execute();
    }

    private List<Article> performRequest(int page) {
        try {
            String getUrl = "http://192.168.199.228/craw/Home/craw/message_info";
            URL url = new URL(getUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.connect();

            //POST请求
            DataOutputStream out = new DataOutputStream(
                    connection.getOutputStream());
            JSONObject obj = new JSONObject();
            obj.put("page", mPageIndex);
            obj.put("sec", "158249");
            out.writeBytes(obj.toString());
            out.flush();
            out.close();

            //读取响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String lines;
            StringBuffer sb = new StringBuffer("");
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "gbk");
                sb.append(lines);
            }
            String result = sb.toString();
            reader.close();
            connection.disconnect();
            return parse(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<Article>();
    }

    @SuppressLint("SimpleDateFormat")
    private List<Article> parse(String result) {
        List<Article> articleLists = new LinkedList<Article>();
        Gson gson = new Gson();
        bean bean = gson.fromJson(result, org.xidian.client.bean.class);
        int count = bean.getData().size();
        for (int i = 0; i < count; i++) {
            org.xidian.client.bean.DataEntity itemObject = bean.getData().get(i);
            Article articleItem = new Article();
            articleItem.setTitle(new StringBuilder(itemObject.getTitle()).reverse().toString());
            articleItem.setUrl(new StringBuilder(itemObject.getUrl()).reverse().toString());
            articleItem.setCommentnum(itemObject.getComment() + "");
            articleItem.setTime(itemObject.getTime());
            articleLists.add(articleItem);
        }
        return articleLists;
    }

    private String formatDate(SimpleDateFormat dateFormat, String dateString) {
        try {
            Date date = dateFormat.parse(dateString);
            return dateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    protected void loadArticle(Article article) {
        Intent intent = new Intent(getActivity(), NeirongActivity.class);
        intent.putExtra("url", article.getUrl());
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        getArticles(1);
    }

    @Override
    public void onLoad() {
        mSwipeRefreshLayout.setRefreshing(true);
        getArticles(mPageIndex);
    }
}