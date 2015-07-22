package com.hackathon.chatable.heartfound;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.aevi.configuration.TerminalConfiguration;
import com.aevi.helpers.CompatibilityException;
import com.aevi.helpers.ServiceState;
import com.aevi.helpers.services.AeviServiceConnectionCallback;
import com.aevi.mail.MailService;
import com.aevi.mail.MailServiceProvider;
import com.aevi.payment.PaymentAppConfiguration;
import com.aevi.payment.PaymentAppConfigurationRequest;
import com.aevi.payment.PaymentRequest;
import com.aevi.payment.TransactionResult;
import com.aevi.payment.TransactionStatus;

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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;




public class MainActivity extends Activity {

    private class MyAsyncTask extends AsyncTask<String, Integer, Double>{
        @Override
        protected Double doInBackground(String... params) {
            postData(params[0]);
            return null;
        }

    };

    private static final String TAG = MainActivity.class.getSimpleName();

    private PaymentAppConfiguration paymentAppConfiguration = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if TerminalServices are installed
        try {
            if (TerminalConfiguration.isTerminalServicesInstalled(this) == false) {
               showExitDialog("Terminal Services is not installed or installed incorrectly.\nThis application will now exit.");
                return;
            }
        } catch(CompatibilityException e) {
            showExitDialog(e.getMessage() + "\nThis application will now exit.");
            return;
        }

        // Ensure the payment application/simulator is installed. If not, present an alert dialog
        // to the user and exit.
        ServiceState paymentAppState = PaymentAppConfiguration.getPaymentApplicationStatus(this);
        Log.d(TAG, "Payment App State: " + paymentAppState);

        if (paymentAppState == ServiceState.NOT_INSTALLED) {
            showExitDialog("A payment application is not installed.\n This application will now exit.");
        } else if (paymentAppState == ServiceState.NO_PERMISSION) {
            showExitDialog("A payment application installed but this App does not have the permission to use it.\n This application will now exit.");
        } else if (paymentAppState == ServiceState.UNAVAILABLE) {
            showExitDialog("The payment application is unavailable.\n This application will now exit.");
        } else {
            // Get the Payment App Configuration
            fetchPaymentAppConfiguration();
        }
    }

    public void loginScreen(View v)
    {
        Intent i = new Intent(this, emailEntry.class);
        startActivity(i);
    }

    /**
     * Called by Android when the control returns to this activity.
     *
     * @param requestCode the code associated with the request (0)
     * @param resultCode  the Intent result code
     * @param data        the result data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0) {


                // Save the Payment Configuration in the Application Object
                paymentAppConfiguration = PaymentAppConfiguration.fromIntent(data);

                Log.d(TAG, "PaymentAppConfiguration retrieved. Currency code is: " + paymentAppConfiguration.getDefaultCurrency().getCurrencyCode());
            } else if (requestCode > 0) {

                TransactionResult transactionResult = TransactionResult.fromIntent(data);
                // Check whether the transaction was successful
                if (transactionResult.getTransactionStatus()== TransactionStatus.APPROVED)
                {
                    showMessage("Transaction Successful");
                } else if (transactionResult.getTransactionStatus()== TransactionStatus.DECLINED){
                    showMessage("Transaction failed");
                }
                else if (transactionResult.getTransactionStatus()== TransactionStatus.TIMEOUT){
                    showMessage("Transaction timed out");
                }
            }
        } else {
            showExitDialog("There was a problem obtaining the PaymentAppConfiguration object.\n This application will now exit.");
        }
    }

    public void postClick(View v)
    {
        new MyAsyncTask().execute("JACK");
    }
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



    public void myMeth(View v) {
        BigDecimal parsedAmount = new BigDecimal("15");

        //Log.d(tag, "Creating a payment request for movieId:" + movieId + ", amount:" + parsedAmount);

        PaymentRequest paymentRequest = new PaymentRequest(parsedAmount);
        startActivityForResult(paymentRequest.createIntent(), Integer.parseInt("35"));
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

    private void showExitDialog(String messageStr) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        TextView textView = new TextView(this);
        textView.setText(messageStr);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(textView);
        builder.setPositiveButton("Ok", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * fetch the Payment Configuration
     */
    private void fetchPaymentAppConfiguration() {
        startActivityForResult(PaymentAppConfigurationRequest.createIntent(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
