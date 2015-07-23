package com.hackathon.chatable.heartfound;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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


public class emailEntry extends Activity {
    ArrayList<String> pressed;
    EditText emailEdit;

    private class MyAsyncTask extends AsyncTask<String, Integer, Double> {
        @Override
        protected Double doInBackground(String... params) {
            postData(params[0]);
            return null;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pressed = new ArrayList<String>();


        setContentView(R.layout.activity_email_entry);

        emailEdit   = (EditText)findViewById(R.id.emailEdit);
    }

    public void postClickEmail(View v)
    {
        String em = emailEdit.getText().toString();
        new MyAsyncTask().execute(em);
    }

    public void postData(String email) {

        HttpClient httpClient = new DefaultHttpClient();
        // replace with your url
        HttpPost httpPost = new HttpPost("http://requestb.in/16p3rf21");

        //Post Data
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
        for(int i = 0; i < pressed.size(); i++)
        {
            nameValuePair.add(new BasicNameValuePair(pressed.get(i).toString(), "YES"));
        }

        nameValuePair.add(new BasicNameValuePair("email",email));

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

    public void onCheckboxClicked(View view) {

        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.chkEvent:
                if (checked) {
                    pressed.add("events");
                }
                else
                    pressed.remove("events");
                break;
            case R.id.chkHealth:
                if (checked)
                    pressed.add("health");
                else
                    pressed.remove("health");
                break;
            case R.id.chkNews:
                if (checked)
                    pressed.add("acceptsMarketing");
                else
                    pressed.remove("acceptsMarketing");
                break;
            case R.id.chkRecipes:
                if (checked)
                    pressed.add("recipes");
                else
                    pressed.remove("recipes");
                break;

        }
    }

    private void showMessage(String msg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        TextView textView = new TextView(this);
        textView.setText(msg);
        textView.setTextSize(25);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(textView);
        builder.setPositiveButton("Ok", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_email_entry, menu);
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
