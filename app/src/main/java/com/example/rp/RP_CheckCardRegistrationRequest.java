package com.example.rp;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RP_CheckCardRegistrationRequest extends StringRequest {
    private static final String CHECK_CARD_URL = "https://192.168.0.2:443/RP_SERVER/RP_CheckCardRegistration.php";
    private Map<String, String> params;

    public RP_CheckCardRegistrationRequest(String userID, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Request.Method.POST, CHECK_CARD_URL, listener, errorListener);
        params = new HashMap<>();
        params.put("userID", userID);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}