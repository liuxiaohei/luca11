package org.ld.utils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.ld.exception.CodeStackException;
import org.ld.uc.UCFunction;
import org.springframework.cglib.core.internal.Function;
import org.springframework.http.MediaType;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@Slf4j
public class HttpClient {
    private static final int READ_TIME_OUT = 30000;
    private static final int CONNECT_TIME_OUT = 30000;
    private static final int MAX_IDLE_CONNECTS = 20;
    public static String STREAM_TYPE = "application/octet-stream";
    private static final OkHttpClient okHttpClient;

    static {
        okHttpClient = new OkHttpClient.Builder()
                .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(MAX_IDLE_CONNECTS, 5, TimeUnit.SECONDS))
                .build();
    }

    /**
     * 返回OutPutStream OutPutStream关闭 连接资源才会关闭
     */
    public static OutputStream getOutputStreamByUrl(String url, String method, int bufferSize) throws IOException {
        UCFunction<String, HttpURLConnection> getConnection = u -> {
            var conn = HttpClient.getStreamUrlConnection(u);
            conn.setRequestMethod(method);
            conn.setInstanceFollowRedirects(false);
            conn.setChunkedStreamingMode(32 << 10); // 32kB-chunk
            return conn;
        };
        HttpURLConnection conn;
        try {
            conn = getConnection.apply(url);
            if (conn.getResponseCode() == 307) {    // 跨VPN情况
                var location = conn.getHeaderField("location");
                conn.disconnect();
                conn = getConnection.apply(location);
            }
        } catch (Throwable e) {
            throw new CodeStackException(e);
        }
        var conn1 = conn;
        return new BufferedOutputStream(conn1.getOutputStream(), bufferSize) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    try {
                        conn1.getInputStream();
                        if (!((conn1.getResponseCode() == 200) || (conn1.getResponseCode() == 201) || (conn1.getResponseCode() == 202))) {
                            log.error("请求失败----Code:" + conn1.getResponseCode() + "Message:" + conn1.getResponseMessage());
                        }
                    } finally {
                        conn1.disconnect();
                    }
                }
            }
        };
    }

    public static HttpURLConnection getStreamUrlConnection(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            return conn;
        } catch (Exception e) {
            throw new CodeStackException(e);
        }
    }

    public static <R> R get(String url, Headers headers, UCFunction<InputStream, R> handler) {
        return execute("GET", url, headers, null, handler);
    }

    public static <R> R post(String url, Headers headers, RequestBody body, UCFunction<InputStream, R> handler) {
        return execute("POST", url, headers, body, handler);
    }

    public static <R> R postForObject(String url, Headers headers, RequestBody body, UCFunction<InputStream, R> handler) {
        return execute("POST", url, headers, body, handler);
    }

    public static <R> R putForObject(String url, Headers headers, RequestBody body, UCFunction<InputStream, R> handler) {
        return execute("PUT", url, headers, body, handler);
    }

    public static <R> R patchForObject(String url, Headers headers, RequestBody body, UCFunction<InputStream, R> handler) {
        return execute("PATCH", url, headers, body, handler);
    }

    public static <R> R delete(String url, Headers headers, UCFunction<InputStream, R> handler) {
        return execute("DELETE", url, headers, null, handler);
    }

    public static <R> R execute(String method, String url, Headers headers, RequestBody body, UCFunction<InputStream, R> handler) {
        Request.Builder requestBuild = new Request.Builder()
                .url(url)
                .method(method, body);
        if (headers != null) {
            requestBuild.headers(headers);
        }
        try {
            Response response = okHttpClient.newCall(requestBuild.build()).execute();
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    try (var in = responseBody.byteStream()) {
                        if (null == in) {
                            return null;
                        }
                        try {
                            return handler.apply(in);
                        } catch (Throwable e) {
                            throw new CodeStackException(e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new CodeStackException(e);
        }
        return null;
    }

    public static <R> R executeWithUnClose(String method, String url, Headers headers, RequestBody body, Function<InputStream, R> handler) {
        Request.Builder requestBuild = new Request.Builder()
                .url(url)
                .method(method, body);
        if (headers != null) {
            requestBuild.headers(headers);
        }
        try {
            Response response = okHttpClient.newCall(requestBuild.build()).execute();
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    var in = responseBody.byteStream();
                    if (null == in) {
                        return null;
                    }
                    return handler.apply(in);
                }
            } else {
                throw new CodeStackException("请求失败");
            }
        } catch (Exception e) {
            throw new CodeStackException(e);
        }
        return null;
    }
}