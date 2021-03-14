package org.ld.fs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.apache.hadoop.hdfs.web.ByteRangeInputStream;
import org.apache.hadoop.hdfs.web.resources.OffsetParam;
import org.ld.uc.UCFunction;
import org.ld.utils.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/** http://hadoop.apache.org/docs/r1.0.4/webhdfs.html */
@Slf4j
@AllArgsConstructor
public class WebHdfsFileSystem {

    private static final String version = "v1";
    private final Map<String, String> selfParams;
    private final URI uri;

    public OutputStream create(String path) throws IOException {
        return HttpClient.getOutputStreamByUrl(getUrl(path, Map.of("data", "TRUE", "overwrite", String.valueOf(true), "buffersize", String.valueOf(FileUtil.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES)), "CREATE"), "PUT", FileUtil.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES);
    }

    public OutputStream append(String path) throws IOException {
        return HttpClient.getOutputStreamByUrl(getUrl(path, Map.of("data", "TRUE", "buffersize", String.valueOf(FileUtil.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES)), "APPEND"), "POST", FileUtil.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES);
    }

    public boolean delete(String path, boolean recursive) {
        return run(path, "DELETE", "DELETE", Map.of("recursive", String.valueOf(recursive)), WebHdfsFileSystem::getBooleanResponse);
    }

    public boolean mkdirs(String path) {
        return run(path, "MKDIRS", "PUT", new HashMap<>(), WebHdfsFileSystem::getBooleanResponse);
    }

    public boolean rename(String src, String dst) {
        return run(src, "RENAME", "PUT", Map.of("destination", StringUtil.toSafeUrlPath(dst)), WebHdfsFileSystem::getBooleanResponse);
    }

    public boolean truncate(String path, long newLength) {
        return run(path, "TRUNCATE", "POST", Map.of("newlength", String.valueOf(newLength)), WebHdfsFileSystem::getBooleanResponse);
    }

    public void setPermission(final String p, final String permission) {
        run(p, "SETPERMISSION", "PUT", Map.of("permission", permission), e -> 0);
    }

    public void setOwner(final String p, final String owner, final String group) {
        run(p, "SETOWNER", "PUT", Map.of("owner", owner, "group", group), e -> 0);
    }

    public Map<String, Object> getFileChecksum(final String p) {
        return run(p, "GETFILECHECKSUM", "GET", new HashMap<>(), JsonUtil::stream2Map);
    }

    /** http://cn.voidcc.com/question/p-dxrzacex-xw.html 可用这个方法获取文件夹的大小 */
    public Map<String, Object> getContentSummary(String path) {
        return run(path, "GETCONTENTSUMMARY", "GET", new HashMap<>(), JsonUtil::stream2Map);
    }

    public void concat(final String trg, final String[] srcs) {
        run(trg, "CONCAT", "POST", Map.of("sources", String.join(",", srcs)), e -> 0);
    }

    public Map<String, Object> getFileStatus(String path) {
        return run(path, "GETFILESTATUS", "GET", new HashMap<>(), JsonUtil::stream2Map);
    }

    public InputStream open(final String f) throws IOException {
        final BiFunction<String, Consumer<URL>, HttpURLConnection> getConnection = (url, urlSetter) -> {
            final var conn = HttpClient.getStreamUrlConnection(url);
            urlSetter.accept(conn.getURL());
            return conn;
        };
        final String OFFSET_PARAM_PREFIX = "offset" + "=";
        final var openUrl = getUrl(f, Map.of("offset", String.valueOf(0L), "buffersize", "" + (FileUtil.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES > 0 ? (int) FileUtil.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES : FileUtil.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES)), "OPEN");
        return new ByteRangeInputStream(
                new ByteRangeInputStream.URLOpener(null) {
                    protected HttpURLConnection connect(long offset, boolean resolved) {
                        assert offset == 0;
                        return getConnection.apply(openUrl, this::setURL);
                    }
                },
                new ByteRangeInputStream.URLOpener(null) {
                    protected HttpURLConnection connect(final long offset, final boolean resolved) throws IOException {
                        return getConnection.apply((offset == 0L ? new URL(openUrl) : new URL(openUrl + "&" + new OffsetParam(offset))).toString(), this::setURL);
                    }
                }) {
            @Override
            protected URL getResolvedUrl(HttpURLConnection connection) throws IOException {
                final var url = connection.getURL();
                if (url.getQuery() == null || Optional.of(url.getQuery().toLowerCase(Locale.ENGLISH)).map(lower -> !lower.startsWith(OFFSET_PARAM_PREFIX) && !lower.contains("&" + OFFSET_PARAM_PREFIX)).orElse(false)) {
                    return url;
                }
                StringBuilder b = null;
                final var st = new StringTokenizer(url.getQuery(), "&");
                while (st.hasMoreTokens()) {
                    final var token = st.nextToken();
                    if (!token.toLowerCase(Locale.ENGLISH).startsWith(OFFSET_PARAM_PREFIX)) {
                        b = b == null ? new StringBuilder("?").append(token) : b.append('&').append(token);
                    }
                }
                return new URL(Optional.of(url.toString()).map(urlStr -> urlStr.substring(0, urlStr.indexOf('?'))).orElse("") + (b == null ? "" : b.toString()));
            }
        };
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listStatus(String path) {
        return (List<Map<String, Object>>) Optional.ofNullable((Map<?, ?>) run(path, "LISTSTATUS", "GET", new HashMap<>(), JsonUtil::stream2Map).get("FileStatuses")).map(r -> r.get("FileStatus")).filter(list -> list instanceof List<?>).map(list -> (List<?>) list).orElseGet(ArrayList::new);
    }

    private <T> T run(String path, String opName, String method, Map<String, String> params, UCFunction<InputStream, T> responseGetter) {
        return HttpClient.execute(method, getUrl(path, params, opName), new Headers.Builder().add("Content-Type", "application/json").build(), null, responseGetter);
    }

    private String getUrl(final String path, final Map<String, String> params, final String op) {
        return "http://" + uri.getHost() + ":" + uri.getPort() + "/webhdfs/" + version + StringUtil.toSafeUrlPath(path, "/") + Stream.concat(Map.of("op", op).entrySet().stream(), Stream.concat(params.entrySet().stream(), selfParams.entrySet().stream())).map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&", "?", ""));
    }

    private static Boolean getBooleanResponse(InputStream is) throws Exception {
        return new ObjectMapper().convertValue(new ObjectMapper().readTree(StringUtil.stream2String(is)).findValue("boolean"), Boolean.class);
    }
}