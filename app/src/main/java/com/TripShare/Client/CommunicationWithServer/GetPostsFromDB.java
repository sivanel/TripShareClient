package com.TripShare.Client.CommunicationWithServer;

import android.os.AsyncTask;
import android.util.Log;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;

public class GetPostsFromDB extends AsyncTask<String, Integer, String>
{
    String body;
    AddAllItemsToListViewListener m_listenerAddItems;
    long m_userID;
    int m_firstPositionToRetrieve;

    public GetPostsFromDB(AddAllItemsToListViewListener i_listener, long i_userID, int i_firstPositionToRetrieve)
    {
        m_listenerAddItems = i_listener;
        m_userID = i_userID;
        m_firstPositionToRetrieve = i_firstPositionToRetrieve;
    }
    @Override
    protected String doInBackground(String... Args)
    {
        String output = null;

        try
        {
            HttpClient httpClient = HttpClientBuilder.create().build();

            // Build URI
            URIBuilder builder = new URIBuilder("http://10.0.2.2:8080/TripShareProject/PostServlet");//("http://tripshare-env.cqpn2tvmsr.us-east-1.elasticbeanstalk.com/PostServlet");
            // TODO: change "0" to m_userID !!!!!!!!!!!!!!!!!!!!!!!!
            builder.setParameter("m_userID", "0"); // The value is 0 right now but will change in the future to user ID // TODO
            builder.setParameter("m_firstPositionToRetrieve", String.valueOf(m_firstPositionToRetrieve));

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
                m_listenerAddItems.addAllItemsToView(body);
            }
        }
        catch (Exception e)
        {
            Log.e("log_tag", "Error in http connection " + e.toString());
            output = "Error in http connection " + e.toString();
        }
        return output;
    }

    public interface AddAllItemsToListViewListener
    {
        void addAllItemsToView(String i_body);
    }
}
