package com.example.rp;

import static com.example.rp.RP_RegisterRequest.getPinnedCertSslSocketFactory;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.example.duduhgee.R;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

public class RP_VerifyRequest extends StringRequest {
    private static final String TAG = "verify";
    final static private String URL = "https://192.168.0.2:443/RP_SERVER/Verify.php";
    private Map<String ,String> map;


    public RP_VerifyRequest(String userID, String p_id, String message, String signature,String publicKey, Response.Listener<String> listener, Context context) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        super(Method.POST, URL, listener, null);

        Log.d(TAG, "아이디: " + userID);
        Log.d(TAG, "제품id: " + p_id);
        Log.d(TAG, "챌린지: " + message);
        Log.d(TAG, "서명  : " + signature);
        //Log.d(TAG, "공개키: " + publicKey);

        SSLSocketFactory sslSocketFactory = getPinnedCertSslSocketFactory(context, R.raw.bpmserver);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);

        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        });

        map= new HashMap<>();
        map.put("userID", userID);
        map.put("p_id",p_id);
        map.put("message", message);
        map.put("signature", signature);
        map.put("publicKey", publicKey);
    }
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }

    @Override
    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=UTF-8";
    }
}
