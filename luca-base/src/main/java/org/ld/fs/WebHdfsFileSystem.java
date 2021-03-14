package org.ld.fs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.web.ByteRangeInputStream;
import org.apache.hadoop.hdfs.web.resources.OffsetParam;
import org.ld.exception.CodeStackException;
import org.ld.uc.UCFunction;
import org.ld.utils.FileUtil;
import org.ld.utils.HttpClient;
import org.ld.utils.JsonUtil;
import org.ld.utils.StringUtil;
import org.springframework.http.MediaType;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * http://hadoop.apache.org/docs/r1.0.4/webhdfs.html
 */
@Slf4j
@SuppressWarnings("unused")
@AllArgsConstructor
public class WebHdfsFileSystem {

    private static final String OFFSET_PARAM_PREFIX = OffsetParam.NAME + "=";
    private static final String version = "v1";
    private final Map<String, String> selfParams;
    @Getter
    private final URI uri;

    public OutputStream create(String path) throws IOException {
        return HttpClient.getOutputStreamByUrl(getUrl(path, Map.of("data", "TRUE", "overwrite", String.valueOf(true), "buffersize", String.valueOf(FileUtil.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES)), HttpFSOperation.CREATE.name()), HttpFSOperation.CREATE.getRestType(), FileUtil.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES);
    }

    public OutputStream append(String path) throws IOException {
        return HttpClient.getOutputStreamByUrl(getUrl(path, Map.of("data", "TRUE", "buffersize", String.valueOf(FileUtil.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES)), HttpFSOperation.APPEND.name()), HttpFSOperation.APPEND.getRestType(), FileUtil.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES);
    }

    public boolean exists(String path) {
        try {
            return this.getFileStatus(path) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean delete(String path, boolean recursive) {
        return runWithHttp(path, HttpFSOperation.DELETE, Map.of("recursive", String.valueOf(recursive)), this::getBooleanResponse);
    }

    public boolean mkdirs(String path) {
        return runWithHttp(path, HttpFSOperation.MKDIRS, new HashMap<>(), this::getBooleanResponse);
    }

    public boolean rename(String src, String dst) {
        return runWithHttp(src, HttpFSOperation.RENAME, Map.of("destination", URLEncoder.encode(dst, StandardCharsets.UTF_8)), this::getBooleanResponse);
    }

    public boolean truncate(String path, long newLength) {
        return runWithHttp(path, HttpFSOperation.TRUNCATE, Map.of("newlength", String.valueOf(newLength)), this::getBooleanResponse);
    }

    public void setPermission(final String p, final String permission) {
        runWithHttp(p, HttpFSOperation.SETPERMISSION, Map.of("permission", permission), e -> null);
    }

    public void setOwner(final String p, final String owner, final String group) {
        runWithHttp(p, HttpFSOperation.SETOWNER, Map.of("owner", owner, "group", group), e -> null);
    }

    public Map<String, Object> getFileChecksum(final String p) {
        return runWithHttp(p, HttpFSOperation.GETFILECHECKSUM, new HashMap<>(), JsonUtil::stream2Map);
    }

    /**
     * http://cn.voidcc.com/question/p-dxrzacex-xw.html 可用这个方法获取文件夹的大小
     */
    public Map<String, Object> getContentSummary(String path) {
        return runWithHttp(path, HttpFSOperation.GETCONTENTSUMMARY, new HashMap<>(), JsonUtil::stream2Map);
    }

    public boolean createNewFile(String f) throws IOException {
        if (exists(f)) {
            return false;
        } else {
            create(f).close();
            return true;
        }
    }

    public void concat(final String trg, final String[] srcs) {
        runWithHttp(trg, HttpFSOperation.CONCAT, Map.of("sources", String.join(",", srcs)), e -> null);
    }

    public Map<String, Object> getFileStatus(String path) {
        return runWithHttp(path, HttpFSOperation.GETFILESTATUS, new HashMap<>(), JsonUtil::stream2Map);
    }

    public InputStream open(final String f) throws IOException {
        final var openUrl = getUrl(f, Map.of("offset", String.valueOf(0L), "buffersize", "" + (FileUtil.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES > 0 ? (int) FileUtil.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES : FileUtil.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES)), HttpFSOperation.OPEN.name());
        return new ByteRangeInputStream(
                new ByteRangeInputStream.URLOpener(null) {
                    @Override
                    protected HttpURLConnection connect(long offset, boolean resolved) throws IOException {
                        assert offset == 0;
                        var conn = HttpClient.getStreamUrlConnection(openUrl);
                        conn.setRequestMethod(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                        setURL(conn.getURL());
                        return conn;
                    }
                },
                new ByteRangeInputStream.URLOpener(null) {
                    @Override
                    protected HttpURLConnection connect(final long offset, final boolean resolved) throws IOException {
                        var conn = HttpClient.getStreamUrlConnection((offset == 0L ? new URL(openUrl) : new URL(openUrl + "&" + new OffsetParam(offset))).toString());
                        conn.setRequestMethod(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                        setURL(conn.getURL());
                        return conn;
                    }
                }) {
            @Override
            protected URL getResolvedUrl(HttpURLConnection connection) throws IOException {
                var url = connection.getURL();
                var query = url.getQuery();
                if (query == null) {
                    return url;
                }
                final var lower = query.toLowerCase(Locale.ENGLISH);
                if (!lower.startsWith(OFFSET_PARAM_PREFIX) && !lower.contains("&" + OFFSET_PARAM_PREFIX)) {
                    return url;
                }
                StringBuilder b = null;
                for (final var st = new StringTokenizer(query, "&"); st.hasMoreTokens(); ) {
                    final var token = st.nextToken();
                    if (!token.toLowerCase(Locale.ENGLISH).startsWith(OFFSET_PARAM_PREFIX)) {
                        if (b == null) {
                            b = new StringBuilder("?").append(token);
                        } else {
                            b.append('&').append(token);
                        }
                    }
                }
                query = b == null ? "" : b.toString();
                final var urlStr = url.toString();
                return new URL(urlStr.substring(0, urlStr.indexOf('?')) + query);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listStatus(String path) {
        final var r = (Map<?, ?>) runWithHttp(path, HttpFSOperation.LISTSTATUS, new HashMap<>(), JsonUtil::stream2Map).get("FileStatuses");
        final var array = Optional.of(FileStatus.class.getSimpleName())
                .map(r::get)
                .filter(list -> list instanceof List<?>)
                .map(list -> (List<?>) list)
                .orElseGet(ArrayList::new);
        return (List<Map<String, Object>>) array;
    }

    public void copyFromLocalFile(boolean delSrc, String src, String target) throws IOException {
        var file = new File(src);
        if (!file.exists()) {
            throw new CodeStackException(String.format("File %s does not exist.", src));
        }
        if (!file.isFile()) {
            throw new CodeStackException(String.format("%s isn't a file.", src));
        }
        try (var is = new FileInputStream(src); var os = create(target)) {
            FileUtil.copyInputStream2OutputStream(is,os);
        }
        if (delSrc) {
            FileUtil.LocalFileSystemHolder.localFileSystem.delete(new Path(src), true);
        }
    }

    private <T> T runWithHttp(String path, HttpFSOperation op, Map<String, String> params, UCFunction<InputStream, T> responseGetter) {
        return HttpClient.execute(op.restType, getUrl(path, params, op.name()), new Headers.Builder().add("Content-Type", op.getContentType()).build(), null, responseGetter);
    }

    private String getUrl(final String path, final Map<String, String> params, final String op) {
        var p = params.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        selfParams.forEach(p::put);
        p.put("op", op);
        return "http://" + getUri().getHost() + ":" + getUri().getPort() + "/webhdfs/" + version + StringUtil.toSafePath(path, "/") + p.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&", "?", ""));
    }

    private Boolean getBooleanResponse(InputStream is) {
        try {
            final var objectMapper = new ObjectMapper();
            return objectMapper.convertValue(objectMapper.readTree(StringUtil.stream2String(is)).findValue("boolean"), Boolean.class);
        } catch (Exception e) {
            throw new CodeStackException(e);
        }
    }

    @Getter
    @AllArgsConstructor
    enum HttpFSOperation {
        CREATE("PUT", MediaType.APPLICATION_OCTET_STREAM_VALUE, "Create and write to a file"),
        APPEND("POST", MediaType.APPLICATION_OCTET_STREAM_VALUE, "Append to a file"),
        OPEN("GET", MediaType.APPLICATION_OCTET_STREAM_VALUE, "Open and read a file"),
        MKDIRS("PUT", MediaType.APPLICATION_JSON_VALUE, "Recursive make directories"),
        RENAME("PUT", MediaType.APPLICATION_JSON_VALUE, "Rename a file/directory"),
        DELETE("DELETE", MediaType.APPLICATION_JSON_VALUE, "Delete a file/directory"),
        GETFILESTATUS("GET", MediaType.APPLICATION_JSON_VALUE, "Status of a file/directory"),
        LISTSTATUS("GET", MediaType.APPLICATION_JSON_VALUE, "List a directory"),
        TRUNCATE("POST", MediaType.APPLICATION_JSON_VALUE, "Trunccate a file"),
        CONCAT("POST", MediaType.APPLICATION_JSON_VALUE, "CONCAT a file"),
        GETCONTENTSUMMARY("GET", MediaType.APPLICATION_JSON_VALUE, "Get content summary of a directory"),
        GETFILECHECKSUM("GET", MediaType.APPLICATION_JSON_VALUE, "Get file checksum"),
        GETHOMEDIRECTORY("GET", MediaType.APPLICATION_JSON_VALUE, "Get home directory"),
        SETPERMISSION("PUT", MediaType.APPLICATION_JSON_VALUE, "Set permission"),
        SETOWNER("PUT", MediaType.APPLICATION_JSON_VALUE, "Set owner"),
        SETREPLICATION("PUT", MediaType.APPLICATION_JSON_VALUE, "Set replication factor"),
        SETTIMES("PUT", MediaType.APPLICATION_JSON_VALUE, "Set access or modification time");

        String restType;
        String contentType;
        String desc;
    }
}
