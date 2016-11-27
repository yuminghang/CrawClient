/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Umeng, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.xidian.client;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mrsimple
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_ARTICLES = "articles";
    public static final String TABLE_ARTICLE_CONTENT = "article_content";
    static final String DB_NAME = "tech_frontier.db";
    static final int DB_VERSION = 1;
    private static final String CREATE_ARTICLES_TABLE_SQL = "CREATE TABLE articles (  "
//            + " id INTEGER(100) ,"
            + " title VARCHAR(50) ,"
            + " url VARCHAR(100), "
            + " commentnum VARCHAR(100) ,"
            + " time VARCHAR(50) "
            + " )";
    private static final String CREATE_ARTICLE_CONTENT_TABLE_SQL = "CREATE TABLE article_content (  "
            + " post_id INTEGER PRIMARY KEY UNIQUE, "
            + " content TEXT NOT NULL "
            + " )";
    static DatabaseHelper sDatabaseHelper;
    private SQLiteDatabase mDatabase;

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mDatabase = getWritableDatabase();
    }

    public static void init(Context context) {
        if (sDatabaseHelper == null) {
            sDatabaseHelper = new DatabaseHelper(context);
        }
    }

    public static DatabaseHelper getInstance() {
        if (sDatabaseHelper == null) {
            throw new NullPointerException("sDatabaseHelper is null,please call init method first.");
        }
        return sDatabaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ARTICLES_TABLE_SQL);
        db.execSQL(CREATE_ARTICLE_CONTENT_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + TABLE_ARTICLES);
        db.execSQL("DROP TABLE " + TABLE_ARTICLE_CONTENT);
        onCreate(db);
    }

    public void saveArticles(List<Article> dataList) {
        for (Article article : dataList) {
            mDatabase.insertWithOnConflict(TABLE_ARTICLES, null, article2ContentValues(article),
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    private ContentValues article2ContentValues(Article item) {
        ContentValues newValues = new ContentValues();
        newValues.put("title", item.getTitle());
        newValues.put("url", item.getUrl());
        newValues.put("commentnum", item.getCommentnum());
        newValues.put("time", item.getTime());
        return newValues;
    }

    public List<Article> loadArticles() {
        Cursor cursor = mDatabase.rawQuery("select * from " + TABLE_ARTICLES, null);
        List<Article> result = parseArticles(cursor);
        cursor.close();
        return result;
    }

    private List<Article> parseArticles(Cursor cursor) {
        List<Article> articles = new ArrayList<Article>();
        while (cursor.moveToNext()) {
            Article item = new Article();
            item.title = cursor.getString(0);
            item.url = cursor.getString(1);
            item.commentnum = cursor.getString(2);
            item.time = "" + cursor.getInt(3);
            // 解析数据
            articles.add(item);
        }
        return articles;
    }

//    public void saveArticleDetails(ArticleDetail detail) {
//        mDatabase.insertWithOnConflict(TABLE_ARTICLE_CONTENT, null,
//                articleDetailtoContentValues(detail),
//                SQLiteDatabase.CONFLICT_REPLACE);
//    }
//
//    public ArticleDetail loadArticleDetail(String postId) {
//        Cursor cursor = mDatabase.rawQuery("select * from " + TABLE_ARTICLE_CONTENT
//                + " where post_id = "
//                + postId, null);
//        ArticleDetail detail = new ArticleDetail(postId, parseArticleCotent(cursor));
//        cursor.close();
//        return detail;
//    }

    private String parseArticleCotent(Cursor cursor) {
        return cursor.moveToNext() ? cursor.getString(1) : "";
    }

//    protected ContentValues articleDetailtoContentValues(ArticleDetail detail) {
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("post_id", detail.postId);
//        contentValues.put("content", detail.content);
//        return contentValues;
//    }

}
