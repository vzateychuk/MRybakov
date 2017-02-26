package ru.vzateychuk.mr2.retrofit;

import java.util.List;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;
import ru.vzateychuk.mr2.model.Article;

/**
 * Interface defining methods used by RetroFit to access current
 * article data from the Article web service.
 * Created by vez on 4.11.15.
 */
public interface IWebServiceAPI {
    /**
     * URL to the web service to use with the Retrofit service.
     */
    String base_WebAPI_URL = "http://mrybakov.ru/";

    /**
     * Method used to query the web service for the
     * current data at @a timestamp.  The annotations enable
     * Retrofit to convert the @a parameter into an HTTP
     * request, which would look something like this:
     * "http://mrybakov.ru/dev/mobile/get.php?timestamp=1441886076"
     *
     * @param timestamp
     * @return List<Article>
     */
    @GET("dev/mobile/get.php")
    Call<List<Article>> listArticles(@Query("timestamp") String timestamp);
}
