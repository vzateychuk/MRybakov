package ru.vzateychuk.mr2.model;

import ru.vzateychuk.mr2.R;

/**
 * Uses for applyFilter data
 * Created by vez on 12.11.15.
 */
public class MyFilter {

    public static final String TYPE_ARTICLE    = "type_article";
    public static final String TYPE_SCHEDULE   = "type_schedule";
    public static final String TYPE_ABOUT      = "type_about";
    public static final String TYPE_UNKNOWN    = "unknown";

    private int mDataType;
    private String mTag;

    public int getDataType() {
        return mDataType;
    }

    /**
    * Uses for string representation for data_type filter
    * @return "" if mDataType = -1;
    * @return  "unknown" if mDataType not one of known types: nav_articles, nav_schedule, nav_about
    */
    public String getStringType() {
        String result;
        switch (mDataType) {
            case R.id.nav_articles:
                result = TYPE_ARTICLE;
                break;
            case R.id.nav_schedule:
                result = TYPE_SCHEDULE;
                break;
            case R.id.nav_about:
                result = TYPE_ABOUT;
                break;
            default:
                result = TYPE_UNKNOWN;
        }
        return result;
    }


    public String getTag() {
        return mTag;
    }

    public void setTag(String tag) {
        this.mTag = tag;
    }

    /**
     * Factory method makes new filter
     * Created by vez on 12.11.15.
     */
    public static MyFilter newInstance(
            int dataType,
            String tag) {
        MyFilter filter = new MyFilter();
        filter.mDataType = dataType;
        filter.mTag = tag;
        return filter;
    }

    @Override
    public String toString() {
        return "MyFilter{" +
                "mDataType=" + mDataType +
                ", mTag='" + mTag + '\'' +
                '}';
    }
}
