package ru.vzateychuk.mr2.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.vzateychuk.mr2.R;
import ru.vzateychuk.mr2.common.Utils;
import ru.vzateychuk.mr2.model.Article;
import ru.vzateychuk.mr2.model.MyFilter;

/**
 * Adapter for form displayed List of Article
 * Created by vez on 22.11.15.
 */
public class ArticleViewAdapter extends BaseAdapter {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final static String TAG = ArticleViewAdapter.class.getSimpleName();

    /**
     * inflater uses to build this Adapter
     */
    private LayoutInflater mInflater = null;

    /**
     * link to ApplicationContext
     */
    private WeakReference<Context> mContext;

    /**
     * internal storage(list) of Articles
     */
    private List<Article> mList;

    /**
     * Class used for saving links to View components to View.Tag
      */
    static class ViewHolder {
        TextView tvId;
        TextView tvDate;
        TextView tvTitle;
        TextView tvTags;
        ImageView imageArticle;
    }

    /**
     * factory creates newInstance of adapter
     */
    public static ArticleViewAdapter newInstance(Context context, List<Article> articles) {
        Log.d(TAG, "newInstance()");

        ArticleViewAdapter adapter = new ArticleViewAdapter();
        adapter.mContext    = new WeakReference<Context>(context);
        adapter.mInflater   = LayoutInflater.from(context);
        // copy array list
        adapter.mList       = new ArrayList<Article>(articles);
        // sort data in list by default
        if (adapter.mList.size()>0) Collections.sort(adapter.mList);

        return adapter;
    }

    // empty constructor
    public ArticleViewAdapter() {
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position,
                        View convertView,
                        ViewGroup parent) {
        Log.d(TAG, "getView()");

        View view = convertView;

        // this RecycledView will be filled with data
        ViewHolder holder;

        if (convertView == null) {

            // if it's not recycled, initialize some attributes
            holder = new ViewHolder();

            // inflate view with item. article_view - refer to the different types of items layout (article_onepane, article_layout)
            view = mInflater.inflate(R.layout.item_article, parent, false);

            holder.tvId         = (TextView) view.findViewById(R.id.tvId);
            holder.tvDate       = (TextView) view.findViewById(R.id.tvArticleDate);
            holder.tvTitle      = (TextView) view.findViewById(R.id.tvArticleTitle);
            holder.tvTags        = (TextView) view.findViewById(R.id.tvTags);
            holder.imageArticle = (ImageView) view.findViewById(R.id.ivArticle);

            // save references to View elements for future use
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // Get article data from internal storage
        Article article = mList.get(position);

        // set text to View
        holder.tvId.setText(article.getID());

        // display Article Date in human readable format (changed to DateLong 22/06/2016 from timestamp)
        // holder.tvDate.setText(Utils.getDateFromTimestamp(article.getTimestamp() * 1000));
        String sLong = article.getDateLong();
        if (sLong != "") {
              try {
                  long tmp = Long.parseLong(sLong) * 1000;
                  sLong = Utils.getDateFromTimestamp(tmp);
              } catch (NumberFormatException e) {
                  Log.e(TAG, e.getMessage());
              }
        }
        holder.tvDate.setText(sLong);

        // Hide date if there is type = about_us (added 23/06/2016)
        if (!article.getType().equals(MyFilter.TYPE_ABOUT)) holder.tvDate.setVisibility(View.VISIBLE);

        holder.tvTitle.setText(article.getTitle());
        holder.tvTags.setText(article.getTags());

        // get image size from resources
        int max_image_size = (int) mContext.get().getResources().getDimension(R.dimen.image_size_port);
        Bitmap image = Utils.getBitmapFromStorage(mContext.get(), article.getID(), max_image_size);
        holder.imageArticle.setImageBitmap(image);

        return view;
    }
}
