package ru.vzateychuk.mr2.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс предоставляет методы для создания или обновления БД в случаях ее отсутствия или устаревания.
 * Created by zateychuk on 14.11.2015.
 */
public class DBHelper extends SQLiteOpenHelper {

    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG = getClass().getSimpleName();

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ArticlesDB.db";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "DBHelper(): constructor");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate(). exeqSQL=" + DBContract.SQL_CREATE_ENTRIES);
        db.execSQL(DBContract.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade(). oldVersion=" + oldVersion + "; newVersion=" + newVersion);

        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(DBContract.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onDowngrade(). oldVersion=" + oldVersion + "; newVersion=" + newVersion);
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(DBContract.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /**
     * articles select from DB
     * Открывает БД на чтение и читает данные для отображения на главной форме. Данные сразу фильтруются согласно фильтру my_filter
     * Важное примечание. Поскольку операция может выполняться длительное время (в случае если БД пересоздается), функцию необходимо вызывать в фоновом потоке
     *
     * @param filter - фильтер который будет применен к данным (Если не null)
     */
    public List<Article> loadArticlesFromDB(@Nullable MyFilter filter) {
        String tmp_message = " is null";
        if (null != filter) tmp_message = filter.toString();
        Log.d(TAG, ".loadArticlesFromDB()," + tmp_message);

        // Define a selection that specifies which columns for the WHERE clause and values for the WHERE clause
        String selection = null;
        String[] selectionArgs = null;

        // if the filter not null initiate selection and selectionArgs
        if (null != filter) {
            selection = "";
            ArrayList<String> selArgsList = new ArrayList<String>();

            if (filter.getDataType() > 0) {
                selection = selection + DBContract.ArticleEntry.COLUMN_NAME_TYPE + "=?";
                selArgsList.add(0, filter.getStringType());
            }
            if (filter.getTag() != "") {
                selection = selection + " AND " + DBContract.ArticleEntry.COLUMN_TAGS + " LIKE ?";
                selArgsList.add(1, "%" + filter.getTag() + "%");
            }
            // convert list to string array
            selectionArgs = new String[selArgsList.size()];
            selArgsList.toArray(selectionArgs);
        }

        // select data from database
        return selectDataFromDB(selection, selectionArgs);
    }

    /**
     * Get maximum of the timestamp values in db
     * returns -1 if db is empty, no data in timestamp
     * */
    public long getMaximumTimestamp() {
        Log.d(TAG, "getMaximumTimestamp()");
        long max_timestamp = -1;

        // Gets the data repository in read mode
        SQLiteDatabase db = this.getReadableDatabase();
        // Define a projection that specifies which columns from the database actually use after this query.
        String[] projection = {
                "MAX("
                + DBContract.ArticleEntry.COLUMN_NAME_TIMESTAMP
                + ") AS "
                + DBContract.ArticleEntry.COLUMN_NAME_TIMESTAMP
        };

        // open cursor
        Cursor cursor = db.query(
                DBContract.ArticleEntry.TABLE_NAME,  // The table to query
                projection,  null, null, null, null, null
        );

        // check if there is any data selected
        if (cursor.moveToFirst()) {
            // discover column indexes from selected data
            int colIndexTimestamp = cursor.getColumnIndexOrThrow(DBContract.ArticleEntry.COLUMN_NAME_TIMESTAMP);
            // get selected data from cursor
            max_timestamp = cursor.getLong(colIndexTimestamp);
        }

        // finally close cursor, database, dbHelper and exit
        cursor.close();
        db.close();
        this.close();

        return max_timestamp * 1000;
    }

    /**
     * insert or update articles in database
     */
    public void insertOrUpdateData(List<Article> articles) {
        Log.d(TAG, ".insertOrUpdateData()");

        // iterate for each articles
        for (Article article : articles) {

            // check if the article with same ID already exists in database
            String selection = DBContract.ArticleEntry.COLUMN_NAME_ENTRY_ID + "=?";
            String[] selectionArgs = {article.getID()};
            List<Article> tmp = selectDataFromDB(selection, selectionArgs);

            // UPDATE if article with same ID already exists in database, INSERT otherwise
            if (tmp.size() > 0) {
                Log.d(TAG, ".insertOrUpdateData(). found article in DB with id=" + article.getID() + ", UPDATE");

                // update if the article from List newer than in DB (article.timestamp > tmp[0].timestamp)
                if (article.getTimestamp() > tmp.get(0).getTimestamp())
                    updateArticleDB(article);

            } else {
                Log.d(TAG, ".insertOrUpdateData(). article not found in DB with id=" + article.getID() + ", INSERT");
                insertArticleDB(article);
            }
        }
    }

    /**
     * Update article in database
     *
     * @returns rows been updated
     */
    private int updateArticleDB(Article article) {
        Log.d(TAG, ".updateArticleDB(), article=" + article);
        // Create a new map of values, where column names are the keys
        ContentValues cv = makeNewContentValue(article);
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Which row to update, based on the ID
        String selection = DBContract.ArticleEntry.COLUMN_NAME_ENTRY_ID + "=?";
        String[] selectionArgs = {article.getID()};

        int count = db.update(
                DBContract.ArticleEntry.TABLE_NAME,
                cv,
                selection,
                selectionArgs);
        db.close();

        return count;
    }

    /**
     * insert new article in database
     *
     * @returns new record _ID
     */
    private long insertArticleDB(Article article) {
        Log.d(TAG, ".insertArticleDB(), article=" + article);
        // Create a new map of values, where column names are the keys
        ContentValues cv = makeNewContentValue(article);
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                DBContract.ArticleEntry.TABLE_NAME,
                null,
                cv);
        db.close();
        return newRowId;
    }

    /**
     * create ContentValue from article. used for update/insert preparation
     *
     * @param article
     */
    private ContentValues makeNewContentValue(Article article) {
        ContentValues cv = new ContentValues();
        cv.put(DBContract.ArticleEntry.COLUMN_NAME_ENTRY_ID, article.getID());   //
        cv.put(DBContract.ArticleEntry.COLUMN_NAME_TYPE, article.getType());
        cv.put(DBContract.ArticleEntry.COLUMN_NAME_TITLE, article.getTitle());
        cv.put(DBContract.ArticleEntry.COLUMN_NAME_TIMESTAMP, article.getTimestamp());
        cv.put(DBContract.ArticleEntry.COLUMN_NAME_DATE, article.getDateLong());
        cv.put(DBContract.ArticleEntry.COLUMN_CONTENT_PATH, article.getContent());
        cv.put(DBContract.ArticleEntry.COLUMN_IMAGE_PATH, article.getDrawing());
        cv.put(DBContract.ArticleEntry.COLUMN_TAGS, article.getTags());
        cv.put(DBContract.ArticleEntry.COLUMN_IS_READED, article.isRead());
        return cv;
    }

    /***
     * @param selection     Define a selection that specifies which columns for the WHERE clause
     * @param selectionArgs Define values for the WHERE clause
     */
    private List<Article> selectDataFromDB(
            @Nullable String selection,
            @Nullable String[] selectionArgs) {
        Log.d(TAG, ".selectDataFromDB()");

        // Gets the data repository in read mode
        SQLiteDatabase db = this.getReadableDatabase();
        // Define a projection that specifies which columns from the database actually use after this query.
        String[] projection = {
                DBContract.ArticleEntry._ID,                     // id Article
                DBContract.ArticleEntry.COLUMN_NAME_ENTRY_ID,    //
                DBContract.ArticleEntry.COLUMN_NAME_TYPE,        // type article
                DBContract.ArticleEntry.COLUMN_NAME_TITLE,
                DBContract.ArticleEntry.COLUMN_NAME_TIMESTAMP,
                DBContract.ArticleEntry.COLUMN_NAME_DATE,
                DBContract.ArticleEntry.COLUMN_CONTENT_PATH,
                DBContract.ArticleEntry.COLUMN_IMAGE_PATH,
                DBContract.ArticleEntry.COLUMN_TAGS,
                DBContract.ArticleEntry.COLUMN_IS_READED
        };

        // How the results sorted in the resulting Cursor
        String sortOrder = DBContract.ArticleEntry.COLUMN_NAME_ENTRY_ID + " DESC";

        Cursor cursor = db.query(
                DBContract.ArticleEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        // get data from cursor
        ArrayList<Article> records = new ArrayList<Article>();
        if (cursor.moveToFirst()) {
            Log.d(TAG, ".selectDataFromDB(), cursor opened and not null");
            do {
                // discover column indexes from selected data
                int colIndexID = cursor.getColumnIndexOrThrow(DBContract.ArticleEntry.COLUMN_NAME_ENTRY_ID);
                int colIndexType = cursor.getColumnIndexOrThrow(DBContract.ArticleEntry.COLUMN_NAME_TYPE);
                int colIndexTitle = cursor.getColumnIndexOrThrow(DBContract.ArticleEntry.COLUMN_NAME_TITLE);
                int colIndexTimestamp = cursor.getColumnIndexOrThrow(DBContract.ArticleEntry.COLUMN_NAME_TIMESTAMP);
                int colIndexDate = cursor.getColumnIndexOrThrow(DBContract.ArticleEntry.COLUMN_NAME_DATE);
                int colIndexContent = cursor.getColumnIndexOrThrow(DBContract.ArticleEntry.COLUMN_CONTENT_PATH);
                int colIndexImage = cursor.getColumnIndexOrThrow(DBContract.ArticleEntry.COLUMN_IMAGE_PATH);
                int colIndexTags = cursor.getColumnIndexOrThrow(DBContract.ArticleEntry.COLUMN_TAGS);
                int colIndexReaded = cursor.getColumnIndexOrThrow(DBContract.ArticleEntry.COLUMN_IS_READED);
                // get selected data from cursor
                String id = cursor.getString(colIndexID);
                String type = cursor.getString(colIndexType);
                String title = cursor.getString(colIndexTitle);
                long timestamp = cursor.getLong(colIndexTimestamp);
                String datelong = cursor.getString(colIndexDate);
                String cont_path = cursor.getString(colIndexContent);
                String image_path = cursor.getString(colIndexImage);
                String tags = cursor.getString(colIndexTags);
                int is_readed = cursor.getInt(colIndexReaded);
                // create new instance of Article
                Article article = new Article(
                        type,
                        datelong,
                        timestamp,
                        id,
                        title,
                        cont_path,
                        image_path,
                        tags);
                // add article to the recordset
                records.add(article);
            } while (cursor.moveToNext());

        } else {
            Log.d(TAG, ".selectDataFromDB(), cursor is NULL, exit");
        }
        // close cursor
        cursor.close();
        // close database
        db.close();
        // finally close dbHelper and exit;
        this.close();
        return records;
    }

}
