package com.TripShare.Client.CommunicationWithServer;

import android.os.AsyncTask;
import android.util.Log;
import com.TripShare.Client.Common.ApplicationManager;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;

public class GetRoutesFromDB extends AsyncTask<String, Integer, String>
{
    private String body;

    @Override
    protected String doInBackground(String... Args)
    {
        String output = null;

        try
        {
            HttpClient httpClient = HttpClientBuilder.create().build();

            // Build URI
            URIBuilder builder = new URIBuilder("http://tripshare-env.cqpn2tvmsr.us-east-1.elasticbeanstalk.com/RouteServlet");

            builder.setParameter("m_userID", String.valueOf(ApplicationManager.getLoggedInUser().getID()));

            // Send request to server
            HttpGet http_get = new HttpGet(builder.build());
            HttpResponse response = httpClient.execute(http_get);

            // Handle response
            ResponseHandler<String> handler = new BasicResponseHandler();
            body = handler.handleResponse(response);
            int code = response.getStatusLine().getStatusCode();

            // retrieve the list of routes from response need to update the list view in the main thread.
            if(code == 200)
            {
                ApplicationManager.setUserRoutes(body);
            }
        }
        catch (Exception e)
        {
            Log.e("log_tag", "Error in http connection " + e.toString());
            output = "Error in http connection " + e.toString();
        }
        return output;
    }
}
