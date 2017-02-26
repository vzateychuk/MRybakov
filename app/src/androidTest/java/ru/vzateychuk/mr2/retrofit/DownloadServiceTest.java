package ru.vzateychuk.mr2.retrofit;

import android.util.Log;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.List;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import ru.vzateychuk.mr2.model.Article;

// import retrofit.GsonConverterFactory;

/**
 * Created by vez on 7.11.15.
 */
public class DownloadServiceTest extends TestCase {

    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG = getClass().getSimpleName();

    Article testArticle;

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(IWebServiceAPI.base_WebAPI_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    /**
     * This is how we turn the IWebServiceAPI into an object that
     * will translate method calls on the IWebServiceAPI's interface
     * methods into HTTP requests on the server. Parameters / return
     * values are being marshalled to/from JSON.
     */

    // initialize the fixture state by overriding setUp()
    @Override
    protected void setUp() {
        // new test Ariticle
        String type = "type_article";
        String dateLong = "";
        long timestamp = 1442814652;
        String id = "library_ca_ca_341";
        String title = "u0412u044bu043fu0443u0441u043a 341. u0412u0438u043au0442u043eu0440 u041bu0443u0447u043au043eu0432. u0424u043eu0442u043e u0440u0430u0431u043eu0447u0435u0433u043e u0434u043du044f u0440u0443u043au043eu0432u043eu0434u0438u0442u0435u043bu044f";
        String content = "http://www.mrybakov.ru/library/ca/ca_341/index.php?content=Y";
        String drawing = "http://www.mrybakov.ru/upload/medialibrary/d03/d03072e2e3e9560b3a97ffef111cefb7.JPG";
        String tags = "sup1, sup2, sup3";
        Article testArticle = new Article(
                type,
                dateLong,
                timestamp,
                id,
                title,
                content,
                drawing,
                tags);
        Log.d(TAG, testArticle.toString());
    }

    /**
     * This test sends a POST request to the VideoServlet to add a new video and
     * then sends a second GET request to check that the video showed up in the
     * list of videos.
     *
     * @throws Exception
     */
    public void testDownloadServiceProxy() throws IOException {

        // The Retrofit class generates WebService (an implementation of the IWebServiceAPI interface).
        IWebServiceAPI webService = retrofit.create(IWebServiceAPI.class);

        // Send the GET request to the web service using Retrofit to add the article.
        // Notice how Retrofit provides a nice strongly-typed interface to our
        // HTTP-accessible service that is much cleaner than muddling around
        // with URL query parameters, etc.
        Call<List<Article>> call = webService.listArticles("1441886076");
        Log.d(TAG, "Call created");

        Response<List<Article>> response = call.execute();
        Log.d(TAG, "response created. HTTP status code=" + response.code() + "; isSuccess=" + response.isSuccess() + "; message=" + response.message());

        // sync request
        List<Article> articles = response.body();
        Log.d(TAG, "call.execute().body() passed");

        if (articles != null) {
            Log.d(TAG, "articles loaded=" + articles.size());
            for (Article article : articles) Log.d(TAG, article.toString());
        } else {
            Log.d(TAG, "NO articles loaded");
        }
    }

}
