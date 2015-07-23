package com.hackathon.chatable.heartfound;

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
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
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

import java.math.BigDecimal;
import java.util.Currency;


public class donateActivity extends Activity {
    EditText donateAmountEdit;
    BigDecimal parsedAmount;
    private static final String TAG = MainActivity.class.getSimpleName();
    private PaymentAppConfiguration paymentAppConfiguration = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        donateAmountEdit   = (EditText)findViewById(R.id.donateAmountEdit);

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
                ticket.append("Charitable Donation of ").align(Alignment.LEFT);
                ticket.append("$ " + parsedAmount.toString()).align(Alignment.RIGHT);
                ticket.appendEmptyLine();
                ticket.appendEmptyLine();
                ticket.append("Tax Deductible").align(Alignment.LEFT);
                ticket.append("--------------------------------").align(Alignment.CENTER);

                ticket.appendEmptyLine();
                ticket.append("Use your phone QR Scanner to get").align(Alignment.CENTER);
                ticket.append("the Heart Foundations ").align(Alignment.CENTER);
                ticket.append("Health Information Service ").align(Alignment.CENTER);
                ticket.append("contact details, call anytime ").align(Alignment.CENTER);
                ticket.append("with any health questions or concerns ").align(Alignment.CENTER);

                BitmapFactory.Options bitmapFactoryOptions = service.getDefaultPrinterSettings().asBitmapFactoryOptions();
                Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.qr_code_details, bitmapFactoryOptions);
                ticket.append(logo).contrastLevel(100).align(Alignment.CENTER);

                ticket.append("Heart Disease affects ").align(Alignment.CENTER);
                ticket.append("over 3 million Australians ").align(Alignment.CENTER);
                ticket.append("every year and in many cases").align(Alignment.CENTER);
                ticket.append("is preventable").align(Alignment.CENTER);
                ticket.append("learn more at").align(Alignment.CENTER);
                ticket.append("www.heartfoundation.org.au").align(Alignment.CENTER);

                ticket.appendEmptyLine();
                ticket.appendEmptyLine();
                ticket.append("Call the Health Information").align(Alignment.CENTER);
                ticket.append("Service 1300 36 27 87").align(Alignment.CENTER);

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

    /**
     * fetch the Payment Configuration
     */
    private void fetchPaymentAppConfiguration() {
        startActivityForResult(PaymentAppConfigurationRequest.createIntent(), 0);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_donate, menu);
        return true;
    }

    public void onClickDonate(View v)
    {
        String donateAmt = donateAmountEdit.getText().toString();
        parsedAmount = new BigDecimal(donateAmt);

        //Log.d(tag, "Creating a payment request for movieId:" + movieId + ", amount:" + parsedAmount);

        PaymentRequest paymentRequest = new PaymentRequest(parsedAmount);
        paymentRequest.setCurrency(Currency.getInstance("AUD"));
        startActivityForResult(paymentRequest.createIntent(), Integer.parseInt("35"));
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
