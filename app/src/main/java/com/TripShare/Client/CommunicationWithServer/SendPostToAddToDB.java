package com.TripShare.Client.CommunicationWithServer;

import android.os.AsyncTask;
import com.TripShare.Client.TripShareObjects.Post;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

public class SendPostToAddToDB extends AsyncTask<String, Integer, String>
{
    Utils m_utils = new Utils();
    Post m_postToAdd;

    public SendPostToAddToDB(Post i_postToAdd)
    {
        m_postToAdd = i_postToAdd;
    }

    protected String doInBackground(String... Args)
    {
        String output = null;

        try
        {
            // convert the object we want to send to the server
            //  to a json format and create an entity from it
            String postInJsonFormat = m_utils.convertToJson(m_postToAdd);
            StringEntity postRouteEntity = new StringEntity(postInJsonFormat);

            URIBuilder builder = new URIBuilder("http://10.0.2.2:8080/SaveRouteToDB/PostServlet");
            builder.setParameter("m_PostToAddToDB", postInJsonFormat);
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost http_Post = new HttpPost(builder.build());
            http_Post.setEntity(postRouteEntity);

            // set request headers
            http_Post.setHeader("Accept", "application/json");
            http_Post.setHeader("Content-type", "application/json; charset-UTF-8");

            // get the response of the server
            HttpResponse httpResponse = httpClient.execute(http_Post);
            HttpEntity httpEntity = httpResponse.getEntity();
            output = EntityUtils.toString(httpEntity);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        return output;
    }
}