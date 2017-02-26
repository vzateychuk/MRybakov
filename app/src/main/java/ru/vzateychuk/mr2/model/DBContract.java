package ru.vzateychuk.mr2.model;

import android.provider.BaseColumns;

/**
 * Класс-контракт явно указывает структуру схемы систематическим и самодокументирующим способом.
 * Представляет собой контейнер, определяющий имена для URI-адресов, таблиц и столбцов.
 * подробнее: http://developer.android.com/intl/ru/training/basics/data-storage/databases.html
 * Created by zateychuk on 14.11.2015.
 */
public final class DBContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DBContract() {
    }

    /* Inner class that defines the table contents */
    public static abstract class ArticleEntry implements BaseColumns {
        public static final String TABLE_NAME = "ArticlesTable";
        public static final String COLUMN_NAME_ENTRY_ID = "article_id";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_DATE = "date_article";
        public static final String COLUMN_CONTENT_PATH = "content_path";
        public static final String COLUMN_IMAGE_PATH = "image_path";
        public static final String COLUMN_TAGS = "tags";
        public static final String COLUMN_IS_READED = "is_readed";
    }

    /*
    * После определения внешнего вида базы данных следует реализовать методы создания и обслуживания базы данных и таблиц.
    * Ниже выражения для создания и удаления таблиц
    * */
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ArticleEntry.TABLE_NAME + " (" +
                    ArticleEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ArticleEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    ArticleEntry.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
                    ArticleEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    ArticleEntry.COLUMN_NAME_TIMESTAMP + INT_TYPE + COMMA_SEP +
                    ArticleEntry.COLUMN_NAME_DATE + INT_TYPE + COMMA_SEP +
                    ArticleEntry.COLUMN_CONTENT_PATH + TEXT_TYPE + COMMA_SEP +
                    ArticleEntry.COLUMN_IMAGE_PATH + TEXT_TYPE + COMMA_SEP +
                    ArticleEntry.COLUMN_TAGS + TEXT_TYPE + COMMA_SEP +
                    ArticleEntry.COLUMN_IS_READED + INT_TYPE +
            " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ArticleEntry.TABLE_NAME;
}
