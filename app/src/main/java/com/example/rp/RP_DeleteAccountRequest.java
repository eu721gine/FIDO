package com.example.rp;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class RP_DeleteAccountRequest extends StringRequest {
    private static final String URL = "https://192.168.0.2:443/RP_SERVER/DeleteAccount.php"; // 수정 필요한 주소
    private final Map<String, String> params;

    public RP_DeleteAccountRequest(String userIDToDelete, Response.Listener<String> listener, Response.ErrorListener errorListener, Context context) {
        super(Method.POST, URL, listener, errorListener);

        params = new HashMap<>();
        params.put("userID", userIDToDelete);
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }

    // 메서드를 호출하여 계정 삭제 요청 실행
    public static void deleteAccount(String userIDToDelete, Context context, Response.Listener<String> responseListener, Response.ErrorListener errorListener) {
        try {
            RP_DeleteAccountRequest deleteAccountRequest = new RP_DeleteAccountRequest(
                    userIDToDelete,
                    responseListener,
                    errorListener,
                    context
            );

            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(deleteAccountRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}