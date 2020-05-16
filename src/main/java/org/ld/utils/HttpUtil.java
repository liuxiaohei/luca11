package org.ld.utils;

import org.ld.exception.CodeStackException;
import org.springframework.http.MediaType;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

/**
 * todo http工具未完善
 * todo 用jdk 默认方式实现
 * https://blog.csdn.net/xiaojin21cen/article/details/86713894
 */
@SuppressWarnings("unused")
public class HttpUtil {

    private static final org.slf4j.Logger LOG = ZLogger.newInstance();
    public static String STREAM_TYPE = "application/octet-stream";

    /**
     * 根据url 获取 Http 连接默认30s超时
     */
    private static HttpURLConnection getUrlConnection(String url) throws IOException {
        var conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        conn.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        return conn;
    }

    /**
     * 指定url的post请求
     */
    public static String postReq(String u,Object params) {
        try {
            final var url = new URL(u);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type","application/json; charset=UTF-8");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            final var printWriter = new PrintWriter(httpURLConnection.getOutputStream());
            printWriter.write(JsonUtil.obj2Json(params));
            printWriter.flush();
            var in = httpURLConnection.getInputStream();
            var reader = new BufferedReader(new InputStreamReader(in));
            return bufferedReader2json(reader);
        } catch (Exception e) {
            throw new CodeStackException(e);
        }
    }

    /**
     * I/O 将BufferedReader转化成json格式
     */
    private static String bufferedReader2json(BufferedReader bufferedReader) {
        var sb = new StringBuilder();
        String line;
        try {
            while (Objects.nonNull(line = bufferedReader.readLine())) {
                sb.append(line);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public OutputStream put(HttpURLConnection conn) throws IOException {
        conn.setRequestMethod("PUT");
        conn.setInstanceFollowRedirects(false);
        conn.setChunkedStreamingMode(32 << 10); //32kB-chunk
        if (conn.getResponseCode() == 307) {
            var location = conn.getHeaderField("location");
            conn.disconnect();
            conn = getUrlConnection(location);
            conn.setInstanceFollowRedirects(false);
            conn.setChunkedStreamingMode(32 << 10); //32kB-chunk
        }
        HttpURLConnection conn1 = conn;
        return new BufferedOutputStream(conn1.getOutputStream(), 10 * 1024 * 1024) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    try {
                        conn1.getInputStream();
                        if (!((conn1.getResponseCode() == 200)
                                || (conn1.getResponseCode() == 201)
                                || (conn1.getResponseCode() == 202))) {
                            LOG.error("请求失败----Code:" + conn1.getResponseCode() + "Message:" + conn1.getResponseMessage());
                        }
                    } finally {
                        conn1.disconnect();
                    }
                }
            }
        };
    }
}