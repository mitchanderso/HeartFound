package com.hackathon.chatable.heartfound;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aevi.configuration.TerminalConfiguration;
import com.aevi.helpers.CompatibilityException;
import com.aevi.helpers.ServiceState;
import com.aevi.helpers.services.AeviServiceConnectionCallback;
import com.aevi.payment.PaymentAppConfiguration;
import com.aevi.payment.PaymentAppConfigurationRequest;
import com.aevi.payment.PaymentRequest;
import com.aevi.payment.TransactionResult;
import com.aevi.payment.TransactionStatus;
import com.aevi.printing.PrintService;
import com.aevi.printing.PrintServiceProvider;
import com.aevi.printing.model.Alignment;
import com.aevi.printing.model.PrintPayload;

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
import java.util.Currency;
import java.util.List;




public class MainActivity extends Activity {


    private static final String TAG = MainActivity.class.getSimpleName();
    int lockCost;

    private PaymentAppConfiguration paymentAppConfiguration = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lockCost = 10;

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
                    printTicket("HEART FOUNDATION - AUSTRALIA");
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
        Intent i = new Intent(this, shareEmail.class);
        startActivity(i);
    }

    public void donateClick(View v)
    {
        Intent i = new Intent(this, donateActivity.class);
        startActivity(i);
    }



    public void myMeth(View v) {
        BigDecimal parsedAmount = new BigDecimal(lockCost);

        //Log.d(tag, "Creating a payment request for movieId:" + movieId + ", amount:" + parsedAmount);

        PaymentRequest paymentRequest = new PaymentRequest(parsedAmount);
        paymentRequest.setCurrency(Currency.getInstance("AUD"));
        startActivityForResult(paymentRequest.createIntent(), Integer.parseInt("35"));
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    @JavascriptInterface
    public void printTicket(final String title) {
        final PrintServiceProvider printServiceProvider = new PrintServiceProvider(getBaseContext());
        printServiceProvider.connect(new AeviServiceConnectionCallback<PrintService>() {
            public void onConnect(PrintService service) {
                if (service == null) {
                    Toast.makeText(getBaseContext(), "Printer service failed to open", Toast.LENGTH_LONG).show();
                    return;
                }

                PrintPayload ticket = new PrintPayload();
                ticket.append(title).align(Alignment.CENTER);
                ticket.appendEmptyLine();
                ticket.appendEmptyLine();
                ticket.append("--------------------------------").align(Alignment.CENTER);
                ticket.append("Love Lock        10$").align(Alignment.RIGHT);
                ticket.appendEmptyLine();
                ticket.appendEmptyLine();
                ticket.append("--------------------------------").align(Alignment.CENTER);

                ticket.appendEmptyLine();
                ticket.append("Use your phone cam to get your  ").align(Alignment.CENTER);
                ticket.append("unique share URL  ").align(Alignment.CENTER);

                BitmapFactory.Options bitmapFactoryOptions = service.getDefaultPrinterSettings().asBitmapFactoryOptions();
                Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.qr_code, bitmapFactoryOptions);
                ticket.append(logo).contrastLevel(100).align(Alignment.CENTER);

                ticket.append("Heart Disease affects ").align(Alignment.CENTER);
                ticket.append("over 3 million Australians ").align(Alignment.CENTER);
                ticket.append("every year and in many cases").align(Alignment.CENTER);
                ticket.append("is preventable").align(Alignment.CENTER);
                ticket.append("learn more at").align(Alignment.CENTER);
                ticket.append("www.heartfoundation.org.au").align(Alignment.CENTER);

                BitmapFactory.Options bitmapFactoryOptions2 = service.getDefaultPrinterSettings().asBitmapFactoryOptions();
                Bitmap logo2 = BitmapFactory.decodeResource(getResources(), R.drawable.heart_foundation, bitmapFactoryOptions2);
                ticket.append(toGrayscale(logo2)).contrastLevel(100).align(Alignment.CENTER);

                ticket.appendEmptyLine();
                ticket.appendEmptyLine();
                ticket.appendEmptyLine();

                try {
                    int result = service.print(ticket);
                    if (result <= 0) {
                        // handle print job failed here
                        Toast.makeText(getBaseContext(), "Failed to print ticket. Printer unavailable", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "Error while attempting to print ticket: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }

        });

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
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void mainSettings()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText et = new EditText(this);
        et.setHint("Enter the Lock Cost");
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(et);
        builder.setPositiveButton("Confirm", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    String cost = et.getText().toString();
                    lockCost = Integer.parseInt(cost);
                    dialog.dismiss();
                }
        }).setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            mainSettings();
        }

        return super.onOptionsItemSelected(item);
    }
}
