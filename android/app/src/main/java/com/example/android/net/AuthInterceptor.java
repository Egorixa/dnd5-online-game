package com.example.android.net;

import android.text.TextUtils;

import com.example.android.data.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Добавляет заголовок Authorization: Bearer &lt;token&gt; ко всем запросам,
 * если токен есть в SessionManager.
 */
public class AuthInterceptor implements Interceptor {

    private final SessionManager session;

    public AuthInterceptor(SessionManager session) {
        this.session = session;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = session != null ? session.getToken() : null;
        if (TextUtils.isEmpty(token)) {
            return chain.proceed(original);
        }
        Request req = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(req);
    }
}
