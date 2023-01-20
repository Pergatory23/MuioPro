package com.theteam.muioproapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseReq;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseResult;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.ProductInfo;
import com.huawei.hms.iap.entity.ProductInfoReq;
import com.huawei.hms.iap.entity.ProductInfoResult;
import com.huawei.hms.iap.entity.PurchaseIntentReq;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.huawei.hms.support.api.client.Status;
import com.theteam.muioproapp.common.CipherUtil;
import com.theteam.muioproapp.common.Key;
import com.theteam.muioproapp.common.ObjectWrapperForBinder;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class ProductList extends AppCompatActivity {
    public IapClient iapClient;
    private String item_name = "TestProductName";
    private String item_price = "1.00";
    private String item_productId = "product001";
    private List<HashMap<String, Object>> products = new ArrayList<>();
    private ListView listView;
    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_list);
        listView = findViewById(R.id.itemlist);
        spinner = findViewById(R.id.progressBar2);
        ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(0xFF38406E));
        iapClient = (IapClient) ((ObjectWrapperForBinder) getIntent().getExtras().getBinder("iapObject")).getData();
        if (iapClient != null)
            loadProduct(iapClient);
    }

    /**
     * Load products information and show the products
     */
    private void loadProduct(IapClient iapClient) {
        spinner.setVisibility(View.VISIBLE);
        listView = (ListView) findViewById(R.id.itemlist);
        // Obtain in-app product details configured in AppGallery Connect, and then show the products
        Task<ProductInfoResult> task = iapClient.obtainProductInfo(createProductInfoReq());
        task.addOnSuccessListener(new OnSuccessListener<ProductInfoResult>() {
            @Override
            public void onSuccess(ProductInfoResult result) {
                if (result != null && !result.getProductInfoList().isEmpty()) {
                    // Show products to users. You can customize the following method or refer to the Reference section.
                    showProduct(result.getProductInfoList());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                System.out.println("Error loading products");
                Log.e("IAP", e.getMessage());
                spinner.setVisibility(View.GONE);
                makeText(ProductList.this, "Error loading products", LENGTH_SHORT).show();
            }
        });
    }

    private void showProduct(List<ProductInfo> productInfoList) {
        spinner.setVisibility(View.GONE);
        for (ProductInfo productInfo : productInfoList) {
            HashMap<String, Object> item1 = new HashMap<>();
            item1.put(item_name, productInfo.getProductName());
            item1.put(item_price, productInfo.getPrice());
            item1.put(item_productId, productInfo.getProductId());
            products.add(item1);
            //to show the products
            SimpleAdapter simpleAdapter = new SimpleAdapter(
                    ProductList.this, products, R.layout.item_layout,
                    new String[]{item_name, item_price}, new int[]{
                    R.id.item_name, R.id.item_price});
            listView.setAdapter(simpleAdapter);
            simpleAdapter.notifyDataSetChanged();
            listView.setOnItemClickListener((adapterView, view, pos, l) -> {
                        String productId = (String) products.get(pos).get(ProductList.this.item_productId);
                        gotoPay(ProductList.this, productId, IapClient.PriceType.IN_APP_CONSUMABLE);
                    }
            );
        }
    }

    private ProductInfoReq createProductInfoReq() {
        ProductInfoReq req = new ProductInfoReq();
        req.setPriceType(IapClient.PriceType.IN_APP_CONSUMABLE);
        ArrayList<String> productIds = new ArrayList<>();
        // Pass in the item_productId list of products to be queried.
        // The product ID is the same as that set by a developer when configuring product information in AppGallery Connect.
        productIds.add("product001");
        productIds.add("product002");
        productIds.add("product003");
        productIds.add("product004");
        productIds.add("product005");
        productIds.add("product006");
        productIds.add("product007");
        productIds.add("product008");
        productIds.add("product009");
        req.setProductIds(productIds);
        return req;
    }

    /**
     * Create orders for in-app products in the PMS.
     *
     * @param activity  indicates the activity object that initiates a request.
     * @param productId ID list of products to be queried. Each product ID must exist and be unique in the current app.
     * @param type      In-app product type.
     */
    private void gotoPay(final Activity activity, String productId, int type) {
        Log.i("IAP", "call createPurchaseIntent");
        IapClient mClient = Iap.getIapClient(activity);
        Task<PurchaseIntentResult> task = mClient.createPurchaseIntent(createPurchaseIntentReq(type, productId));
        task.addOnSuccessListener(result -> {
            Log.i("IAP", "createPurchaseIntent, onSuccess");
            if (result == null) {
                Log.e("IAP", "result is null");
                makeText(ProductList.this, "Error result is null", LENGTH_SHORT).show();
                return;
            }
            Status status = result.getStatus();
            if (status == null) {
                Log.e("IAP", "status is null");
                makeText(ProductList.this, "Error status is null", LENGTH_SHORT).show();
                return;
            }
            // You should pull up the page to complete the payment process.
            if (status.hasResolution()) {
                try {
                    status.startResolutionForResult(activity, 9006);
                } catch (IntentSender.SendIntentException exp) {
                    Log.e("IAP", exp.getMessage());
                    makeText(ProductList.this, "Error " + exp.getMessage(), LENGTH_SHORT).show();
                }
            } else {
                Log.e("IAP", "intent is null");
                makeText(ProductList.this, "Error intent is null", LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("IAP", e.getMessage());
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            if (e instanceof IapApiException) {
                IapApiException apiException = (IapApiException) e;
                int returnCode = apiException.getStatusCode();
                Log.e("IAP", "createPurchaseIntent, returnCode: " + returnCode);
                makeText(ProductList.this, "Error createPurchaseIntent, returnCode: " + returnCode, LENGTH_SHORT).show();
                // Handle error scenarios
            } else {
                // Other external errors
            }
        });
    }

    /**
     * Create a PurchaseIntentReq instance.
     *
     * @param type      In-app product type.
     * @param productId ID of the in-app product to be paid.
     *                  The in-app product ID is the product ID you set during in-app product configuration in AppGallery Connect.
     * @return PurchaseIntentReq
     */
    private PurchaseIntentReq createPurchaseIntentReq(int type, String productId) {
        PurchaseIntentReq req = new PurchaseIntentReq();
        req.setProductId(productId);
        req.setPriceType(type);
        req.setDeveloperPayload("test");
        return req;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9006) {
            if (data == null) {
                Toast.makeText(this, "error data is null", Toast.LENGTH_SHORT).show();
                return;
            }
            PurchaseResultInfo purchaseResultInfo = Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data);
            switch (purchaseResultInfo.getReturnCode()) {
                case OrderStatusCode.ORDER_STATE_SUCCESS:
                    // Verify signature of payment results.
                    boolean success = CipherUtil.doCheck(purchaseResultInfo.getInAppPurchaseData(), purchaseResultInfo.getInAppDataSignature(), Key.getPublicKey());
                    if (success) {
                        // Call the consumeOwnedPurchase interface to consume it after successfully delivering the product to your user.
                        consumeOwnedPurchase(this, purchaseResultInfo.getInAppPurchaseData());
                    } else {
                        Toast.makeText(this, "Pay successful, sign failed", Toast.LENGTH_SHORT).show();
                    }
                    return;
                case OrderStatusCode.ORDER_STATE_CANCEL:
                    // The User cancels payment.
                    Toast.makeText(this, "user cancel", Toast.LENGTH_SHORT).show();
                    return;
                case OrderStatusCode.ORDER_PRODUCT_OWNED:
                    // The user has already owned the product.
                    Toast.makeText(this, "you have owned the product", Toast.LENGTH_SHORT).show();
                    // You can check if the user has purchased the product and decide whether to provide goods
                    // If the purchase is a consumable product, consuming the purchase and deliver product
                    return;
                default:
                    Toast.makeText(this, "Pay failed", Toast.LENGTH_SHORT).show();
                    break;
            }
            return;
        }
    }

    /**
     * Consume the unconsumed purchase with type 0 after successfully delivering the product, then the Huawei payment server will update the order status and the user can purchase the product again.
     *
     * @param inAppPurchaseData JSON string that contains purchase order details.
     */
    private void consumeOwnedPurchase(final Context context, String inAppPurchaseData) {
        Log.i("IAP", "call consumeOwnedPurchase");
        IapClient mClient = Iap.getIapClient(context);
        Task<ConsumeOwnedPurchaseResult> task = mClient.consumeOwnedPurchase(createConsumeOwnedPurchaseReq(inAppPurchaseData));
        task.addOnSuccessListener(result -> {
            // Consume success
            Log.i("IAP", "consumeOwnedPurchase success");
            Toast.makeText(context, "Pay success, and the product has been delivered", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e("IAP", e.getMessage());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            if (e instanceof IapApiException) {
                IapApiException apiException = (IapApiException) e;
                Status status = apiException.getStatus();
                int returnCode = apiException.getStatusCode();
                Log.e("IAP", "consumeOwnedPurchase fail, returnCode: " + returnCode);
            } else {
                // Other external errors
            }
        });
    }

    /**
     * Create a ConsumeOwnedPurchaseReq instance.
     *
     * @param purchaseData JSON string that contains purchase order details.
     * @return ConsumeOwnedPurchaseReq
     */
    private ConsumeOwnedPurchaseReq createConsumeOwnedPurchaseReq(String purchaseData) {
        ConsumeOwnedPurchaseReq req = new ConsumeOwnedPurchaseReq();
        // Parse purchaseToken from InAppPurchaseData in JSON format.
        try {
            InAppPurchaseData inAppPurchaseData = new InAppPurchaseData(purchaseData);
            req.setPurchaseToken(inAppPurchaseData.getPurchaseToken());
        } catch (JSONException e) {
            Log.e("IAP", "createConsumeOwnedPurchaseReq JSONExeption");
            Toast.makeText(this, "Error createConsumeOwnedPurchaseReq JSONExeption", Toast.LENGTH_SHORT).show();
        }
        return req;
    }
}
