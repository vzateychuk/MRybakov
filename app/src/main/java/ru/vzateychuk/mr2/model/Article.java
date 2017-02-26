package ru.vzateychuk.mr2.model;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This class represent data recieved from web service
 * Created by vez on 4.11.15.
 */
public class Article implements Comparable<Article>{
    /**
     * Debugging tag used by the Android logger.
     */
    private final String TAG = getClass().getSimpleName();

    @SerializedName("Type")
    @Expose
    private String mType;
    @SerializedName("DateLong")
    @Expose
    @Nullable
    private String mDateLong;
    @SerializedName("timestamp")
    @Expose
    private long mTimestamp;
    @SerializedName("ID")
    @Expose
    private String mID;
    @SerializedName("Title")
    @Expose
    private String mTitle;
    @SerializedName("Content")
    @Expose
    private String mContent;
    @SerializedName("Drawing")
    @Expose
    private String mDrawing;
    @SerializedName("Tags")
    @Expose
    private String mTags;

    // flag if this Article already readed
    private boolean mIsRead = false;

    // empty constructor required by retrofit
    public Article() {
    }

    /**
     * @param timestamp
     * @param Tags
     * @param Type
     * @param ID
     * @param Content
     * @param Title
     * @param DateLong
     * @param Drawing
     */
    public Article(String Type,
                   String DateLong,
                   long timestamp,
                   String ID,
                   String Title,
                   String Content,
                   String Drawing,
                   String Tags) {
        this.mType = Type;
        this.mDateLong = DateLong;
        this.mTimestamp = timestamp;
        this.mID = ID;
        this.mTitle = Title;
        this.mContent = Content;
        this.mDrawing = Drawing;
        this.mTags = Tags;
        Log.d(TAG, "Created=" + this.toString());
    }

    /**
     * @return The Type
     */
    public String getType() {
        return mType;
    }

    /**
     *
     * @param Type
     * The Type
     */
    public void setType(String Type) {
        mType = Type;
    }

    /**
     *
     * @return
     * return Article date in Epoch Unix Time Stamp format
     */
    public String getDateLong() {
        return mDateLong;
    }

    /**
     *
     * @param DateLong
     * The DateLong
     */
    public void setDateLong(String DateLong) {
        this.mDateLong = DateLong;
    }

    /**
     *
     * @return
     * The timestamp
     */
    public long getTimestamp() {
        return mTimestamp;
    }

    /**
     *
     * @param timestamp
     * The timestamp
     */
    public void setTimestamp(long timestamp) {
        this.mTimestamp = timestamp;
    }

    /**
     *
     * @return
     * The ID
     */
    public String getID() {
        return mID;
    }

    /**
     *
     * @param ID
     * The ID
     */
    public void setID(String ID) {
        this.mID = ID;
    }

    /**
     *
     * @return
     * The Title
     */
    public String getTitle() {
        return mTitle.replaceAll("&quot;", "'");
    }

    /**
     *
     * @param Title
     * The Title
     */
    public void setTitle(String Title) {
        // replace &quot; symbol
        this.mTitle = Title;
    }

    /**
     *
     * @return
     * The Content
     */
    public String getContent() {
        return mContent;
    }

    /**
     *
     * @param Content
     * The Content
     */
    public void setContent(String Content) {
        this.mContent = Content;
    }

    /**
     *
     * @return
     * The Drawing
     */
    public String getDrawing() {
        return mDrawing;
    }

    /**
     *
     * @param Drawing
     * The Drawing
     */
    public void setDrawing(String Drawing) {
        this.mDrawing = Drawing;
    }

    /**
     * @return
     * The Tags
     */
    public String getTags() {
        return mTags;
    }

    /**
     * @param Tags The Tags
     */
    public void setTags(String Tags) {
        this.mTags = Tags;
    }

    public boolean isRead() {
        return mIsRead;
    }
    public void setRead(boolean isRead) {
        this.mIsRead = isRead;
    }


    @Override
    public String toString() {
        return "Article{" +
                "Type='" + mType + '\'' +
                ", DateLong=" + mDateLong +
                ", timestamp=" + mTimestamp +
                ", ID='" + mID + '\'' +
                ", Title='" + mTitle + '\'' +
                ", Content='" + mContent + '\'' +
                ", Drawing='" + mDrawing + '\'' +
                ", Tags='" + mTags + '\'' +
                ", IsRead='" + mIsRead + '\'' +
                '}';
    }

    // implements from interface Comparable<ArticleRecord>
    // uses to sort ArrayList by Title
    @Override
    public int compareTo(Article another) {
        return another.mTitle.compareTo(this.mTitle);
    }
}
