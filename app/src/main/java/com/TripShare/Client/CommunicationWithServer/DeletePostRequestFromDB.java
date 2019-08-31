package com.TripShare.Client.CommunicationWithServer;

import android.os.AsyncTask;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpDelete;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

// This class is yet used!!!!

public class DeletePostRequestFromDB extends AsyncTask<String, Integer, String>
{
    String m_postToDeleteID;

    public DeletePostRequestFromDB(String i_postToDeleteID) {
        m_postToDeleteID = i_postToDeleteID;
    }

    protected String doInBackground(String... Args) {
        String output = null;

        try {
            URIBuilder builder = new URIBuilder("http://tripshare-env.cqpn2tvmsr.us-east-1.elasticbeanstalk.com/PostServlet");//("http://tripshare-env.cqpn2tvmsr.us-east-1.elasticbeanstalk.com/PostServlet");
            //URIBuilder builder = new URIBuilder("http://10.0.2.2:8080/TripShareProject/PostServlet");//("http://tripshare-env.cqpn2tvmsr.us-east-1.elasticbeanstalk.com/PostServlet");
            builder.setParameter("m_postID", m_postToDeleteID);
            HttpDelete http_delete = new HttpDelete(builder.build());
            http_delete.setHeader("Accept", "application/json");
            http_delete.setHeader("Content-type", "application/json; charset-UTF-8");
            HttpClient httpClient = HttpClientBuilder.create().build();

            // get the response of the server
            HttpResponse httpResponse = httpClient.execute(http_delete);
            HttpEntity httpEntity = httpResponse.getEntity();
            output = EntityUtils.toString(httpEntity);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return output;
    }
}
