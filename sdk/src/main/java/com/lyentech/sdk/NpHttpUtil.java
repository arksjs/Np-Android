package com.lyentech.sdk;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author by jason-何伟杰，2022/5/18
 * des:网络请求工具
 */
public class NpHttpUtil {
    public final static int READ_TIMEOUT = 10;
    public final static int CONNECT_TIMEOUT = 10;
    public final static int WRITE_TIMEOUT = 10;
    private static final byte[] LOCKER = new byte[0];
    private static NpHttpUtil mInstance;
    private OkHttpClient mOkHttpClient;

    /**
     * 自定义网络回调接口
     */
    public interface NetCall {
        void success(Call call, Response response) throws IOException;

        void failed(Call call, IOException e);
    }

    public static NpHttpUtil getInstance() {
        if (mInstance == null) {
            synchronized (LOCKER) {
                if (mInstance == null) {
                    mInstance = new NpHttpUtil();
                }
            }
        }
        return mInstance;
    }

    //异步get
    public void getAsync(String url, NetCall netCall) {
        Request.Builder builder = new Request.Builder();
        Request request = builder.get().url(url).build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (netCall != null)
                    netCall.failed(call, e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (netCall != null) netCall.success(call, response);
            }
        });
    }

    //异步post
    public void postAsync(String url, Map<String, String> map, NetCall netCall) {
        RequestBody body = setRequestBody(map);
        Request.Builder builder = new Request.Builder();
        Request request = builder.post(body).url(url).build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (netCall != null)
                    netCall.failed(call, e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (netCall != null) netCall.success(call, response);
            }
        });
    }

    //异步请求json
    public void postJsonAsync(String url, String json, NetCall netCall) {
        MediaType mediaType = MediaType.Companion.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.Companion.create(json, mediaType);
        Request.Builder builder = new Request.Builder();
        Request request = builder.post(body).url(url).build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (netCall != null) netCall.failed(call, e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (netCall != null) netCall.success(call, response);
            }
        });
    }

    /**
     * post的请求参数，构造RequestBody
     *
     * @param BodyParams
     * @return
     */
    private RequestBody setRequestBody(Map<String, String> BodyParams) {
        RequestBody body = null;
        okhttp3.FormBody.Builder formEncodingBuilder = new okhttp3.FormBody.Builder();
        if (BodyParams != null) {
            Iterator<String> iterator = BodyParams.keySet().iterator();
            String key = "";
            while (iterator.hasNext()) {
                key = iterator.next().toString();
                formEncodingBuilder.add(key, BodyParams.get(key));
                Log.d("post http", "post_Params===" + key + "====" + BodyParams.get(key));
            }
        }
        body = formEncodingBuilder.build();
        return body;

    }

    private NpHttpUtil() {
        OkHttpClient.Builder cBuilder = new OkHttpClient.Builder();
        cBuilder.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
        cBuilder.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        cBuilder.writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        cBuilder.sslSocketFactory(createSSLSocketFactory(), new TrustAllCerts());
        cBuilder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
        mOkHttpClient = cBuilder.build();
    }

    /**
     * 生成安全套接字工厂，用于https请求的证书跳过
     *
     * @return
     */
    private SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }
        return ssfFactory;
    }

    /**
     * 用于信任所有证书
     */
    class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
