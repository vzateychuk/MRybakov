package ru.vzateychuk.mr2.retrofit;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.List;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import ru.vzateychuk.mr2.model.Article;

/**
 * This is how we turn the IWebServiceAPI into an Retrofit object that translates method calls
 * on the IWebServiceAPI' interface methods into HTTP requests on the server. Parameters / return
 * values are being marshaled to/from JSON.
 */
public class WebServiceAPITest extends TestCase
{

    private Retrofit retrofit;
    private IWebServiceAPI webService;

    @Override
    public void setUp()
    {
        retrofit = new Retrofit.Builder()
                .baseUrl( IWebServiceAPI.base_WebAPI_URL )
                .addConverterFactory( GsonConverterFactory.create() )
                .build();

        webService = retrofit.create( IWebServiceAPI.class );
    }

    /**
     * This test get non-empty list of articles out of the GET web service (using Retrofit).
     *
     * @throws IOException
     */
    public void testDownloadListOfArticles() throws IOException
    {
        // Get list of Articles since 10 Sep 2015
        final String timestamp = "1441886076";
        Call<List<Article>> call = webService.listArticles(timestamp);
        Response<List<Article>> response = call.execute();
        assertNotNull("Response.", response);
        assertTrue("Response code.", response.isSuccess());

        List<Article> articles = response.body();
        assertNotNull("Articles.", articles);
        assertFalse("Articles.", articles.isEmpty());
    }

    /**
     * This test get empty list of articles out of the GET web service (using Retrofit).
     *
     * @throws IOException
     */
    public void testDownloadNoArticles() throws IOException
    {
        // Get list of Articles since now
        final String timestamp = String.valueOf( Math.round( System.currentTimeMillis()/1000 ) );
        Call<List<Article>> call = webService.listArticles(timestamp);
        Response<List<Article>> response = call.execute();
        assertNotNull("Response.", response);
        assertTrue("Response code.", response.isSuccess());

        List<Article> articles = response.body();
        assertNotNull("Articles.", articles);
        assertTrue("Articles.", articles.isEmpty());
    }

}
