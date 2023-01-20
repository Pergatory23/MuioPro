package com.theteam.muioproapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.IsEnvReadyResult;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.support.api.client.Status;
import com.theteam.muioproapp.common.ObjectWrapperForBinder;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity {
    private IapClient iapClient;
    private ProgressBar spinner;
    private Button continueBtn;
    private TextView errorTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = findViewById(R.id.progressBar);
        continueBtn = findViewById(R.id.continueBtn);
        errorTxt = findViewById(R.id.error_message);
        errorTxt.setVisibility(View.GONE);
        spinner.setVisibility(View.INVISIBLE);
        continueBtn.setOnClickListener(v -> {
            continueBtn.setEnabled(false);
            spinner.setVisibility(View.VISIBLE);
        });
        checkEnvironment();
    }

    private void checkEnvironment() {
        // Obtain in-app product details configured in AppGallery Connect, and then show the products
        iapClient = Iap.getIapClient(MainActivity.this);
        Task<IsEnvReadyResult> task = iapClient.isEnvReady();
        task.addOnSuccessListener(result -> {
            // Obtain the execution result.
            String carrierId = result.getCarrierId();
            makeText(MainActivity.this, "Welcome", LENGTH_SHORT).show();
            spinner.setVisibility(View.INVISIBLE);
            continueBtn.setOnClickListener(v -> {
                final Bundle bundle = new Bundle();
                bundle.putBinder("iapObject", new ObjectWrapperForBinder(iapClient));
                startActivity(new Intent(MainActivity.this, ProductList.class).putExtras(bundle));
            });
            continueBtn.setText("Continue");
            continueBtn.setEnabled(true);
        }).addOnFailureListener(e -> {
            if (e instanceof IapApiException) {
                IapApiException apiException = (IapApiException) e;
                Status status = apiException.getStatus();
                if (status.getStatusCode() == OrderStatusCode.ORDER_HWID_NOT_LOGIN) {
                    spinner.setVisibility(View.GONE);
                    // HUAWEI ID is not signed in.
                    if (status.hasResolution()) {
                        Log.e("not signed in", status.getErrorString());
                        System.out.println("HUAWEI ID is not signed in");
                        errorTxt.setText("Looks like you're not signed in your AppGallery account!");
                        errorTxt.setVisibility(View.VISIBLE);
                    }
                } else if (status.getStatusCode() == OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED) {
                    spinner.setVisibility(View.GONE);
                    // The current country/region does not support HUAWEI IAP.
                    System.out.println("Region not support IAP");
                    Log.e("Region not support IAP", status.getStatusMessage() + "\n" + task.getResult().getCountry());
                    errorTxt.setText("Seems like your Country/Region does not support HUAWEI IAP");
                    errorTxt.setVisibility(View.VISIBLE);
                }
            } else {
                spinner.setVisibility(View.GONE);
                // Other external errors.
                errorTxt.setText("Unknown error has been occurred while trying to connect to your Huawei account! \n Please check you account in AppGallery.");
                errorTxt.setVisibility(View.VISIBLE);
                Log.e("External errors", e.getMessage());
                continueBtn.setOnClickListener(v -> {
                    errorTxt.setVisibility(View.GONE);
                    spinner.setVisibility(View.VISIBLE);
                    continueBtn.setEnabled(false);
                    checkEnvironment();
                });
                continueBtn.setText("Try again");
                continueBtn.setEnabled(true);
            }
        });
    }
}