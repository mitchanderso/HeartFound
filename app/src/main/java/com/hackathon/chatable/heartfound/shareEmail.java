package com.hackathon.chatable.heartfound;

import android.app.Activity;
import android.content.Intent;
import android.app.ActionBar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class shareEmail extends Activity {

    EditText shareEmailEdit;

    private class MyAsyncTask extends AsyncTask<String, Integer, Double> {
        @Override
        protected Double doInBackground(String... params) {
            postData(params[0]);
            return null;
        }

    };

    public void postData(String name) {

        //showMessage("Transaction timed out");

        HttpClient httpClient = new DefaultHttpClient();
        // replace with your url
        HttpPost httpPost = new HttpPost("http://requestb.in/o2gqj4o2");


        //Post Data
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
        nameValuePair.add(new BasicNameValuePair("username", name));
        nameValuePair.add(new BasicNameValuePair("password", "123456789"));


        //Encoding POST data
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
        } catch (UnsupportedEncodingException e) {
            // log exception
            e.printStackTrace();
        }

        //making POST request.
        try {
            HttpResponse response = httpClient.execute(httpPost);
            // write response to log
            Log.d("Http Post Response:", response.toString());
        } catch (ClientProtocolException e) {
            // Log exception
            e.printStackTrace();
        } catch (IOException e) {
            // Log exception
            e.printStackTrace();
        }
    }

    public void postClickShareEmail(View v)
    {
        String em = shareEmailEdit.getText().toString();
        new MyAsyncTask().execute(em);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_email);

        shareEmailEdit   = (EditText)findViewById(R.id.emailEdit);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share_email, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
