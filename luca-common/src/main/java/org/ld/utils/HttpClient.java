package org.ld.utils;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.ld.exception.CodeStackException;
import org.ld.uc.UCFunction;
import org.springframework.http.MediaType;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * https://www.jianshu.com/p/a71a42f4634b
 */
@SuppressWarnings("unused")
@Slf4j
public class HttpClient {

    public static final Supplier<Headers> JSON_HEAD_SUPPLIER = () -> new Headers.Builder().add("Content-Type", "application/json").build();
    public static final Supplier<Headers> STREAM_HEAD_SUPPLIER = () -> new Headers.Builder().add("Content-Type", "application/octet-stream").build();

    private static class OkHttpClientHandler {
        private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(20, 5, TimeUnit.SECONDS))
                .retryOnConnectionFailure(true)
                .build();
    }

    /**
     * 返回OutPutStream OutPutStream关闭 连接资源才会关闭
     * todo 暂时没有 从Okhttp 中找到可以直接从一次请求的连接中获取conn对象的手段
     */
    @SneakyThrows
    public static OutputStream getOutputStreamByUrl(String url, String method, int bufferSize) {
        UCFunction<String, HttpURLConnection> getConnection = u -> {
            var conn = HttpClient.getStreamUrlConnection(u);
            conn.setRequestMethod(method);
            conn.setInstanceFollowRedirects(false);
            conn.setChunkedStreamingMode(32 << 10); // 32kB-chunk
            return conn;
        };
        var conn = getConnection.apply(url);
        if (conn.getResponseCode() == 307) {    // 跨VPN情况
            var location = conn.getHeaderField("location");
            conn.disconnect();
            conn = getConnection.apply(location);
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
                        var code = conn1.getResponseCode();
                        if (!(code >= 200 && code < 300)) {
                            log.error("请求失败 Code:" + conn1.getResponseCode() + "Message:" + conn1.getResponseMessage());
                        }
                    } finally {
                        conn1.disconnect();
                    }
                }
            }
        };
    }

    @SneakyThrows
    public static HttpURLConnection getStreamUrlConnection(String url) {
        final var conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        conn.setRequestProperty("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return conn;
    }

    public static <R> R get(String url, Headers headers, UCFunction<InputStream, R> handler) {
        return execute("GET", url, headers, null, handler);
    }

    public static <R> R post(String url, Headers headers, RequestBody body, UCFunction<InputStream, R> handler) {
        return execute("POST", url, headers, body, handler);
    }

    public static <R> R put(String url, Headers headers, RequestBody body, UCFunction<InputStream, R> handler) {
        return execute("PUT", url, headers, body, handler);
    }

    public static <R> R patch(String url, Headers headers, RequestBody body, UCFunction<InputStream, R> handler) {
        return execute("PATCH", url, headers, body, handler);
    }

    public static <R> R delete(String url, Headers headers, UCFunction<InputStream, R> handler) {
        return execute("DELETE", url, headers, null, handler);
    }

    @SneakyThrows
    public static <R> R execute(String method, String url, Headers headers, RequestBody body, UCFunction<InputStream, R> handler) {
        return executeWithUnClose(method, url, headers, body, is -> {
            @Cleanup var in = is;
            if (null != in) {
                return handler.apply(in);
            }
            return null;
        });
    }

    @SneakyThrows
    public static <R> R executeWithUnClose(String method, String url, Headers headers, RequestBody body, UCFunction<InputStream, R> handler) {
        var requestBuild = new Request.Builder()
                .url(url)
                .method(method, body);
        if (headers != null) {
            requestBuild.headers(headers);
        }
        var response = OkHttpClientHandler.okHttpClient.newCall(requestBuild.build()).execute();
        if (response.isSuccessful()) {
            var responseBody = response.body();
            if (responseBody != null) {
                var in = responseBody.byteStream();
                if (null == in) {
                    return null;
                }
                return handler.apply(in);
            }
        } else {
            throw new CodeStackException("请求失败 URL :" + url + " method:" + method);
        }
        return null;
    }
}