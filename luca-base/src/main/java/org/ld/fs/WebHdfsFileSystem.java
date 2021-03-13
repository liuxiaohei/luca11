package org.ld.fs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.DFSUtilClient;
import org.apache.hadoop.hdfs.protocol.FsPermissionExtension;
import org.apache.hadoop.hdfs.protocol.HdfsFileStatus;
import org.apache.hadoop.hdfs.web.ByteRangeInputStream;
import org.apache.hadoop.hdfs.web.resources.OffsetParam;
import org.apache.hadoop.util.StringUtils;
import org.codehaus.jackson.map.ObjectReader;
import org.ld.exception.CodeStackException;
import org.ld.uc.UCFunction;
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
import java.util.stream.Stream;

@Slf4j
@SuppressWarnings("unused")
public class WebHdfsFileSystem {

    private static final Integer DEFAULT_FILE_BUFFER_SIZE_IN_BYTES = 10 * 1024 * 1024; // 10MB
    private final  Map<String,String> selfParams;
    private final String version;
    @Getter
    private final URI uri;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebHdfsFileSystem(Map<String,String> selfParams, URI uri) {
        this.selfParams = selfParams;
        this.uri = uri;
        this.version = "v1";
    }

    public FSDataOutputStream create(String path) throws IOException {
        return runWithHttp(path, Map.of("data", "TRUE", "overwrite", String.valueOf(true), "buffersize", String.valueOf(DEFAULT_FILE_BUFFER_SIZE_IN_BYTES)), HttpFSOperation.CREATE);
    }

    public FSDataOutputStream append(String path) throws IOException {
        return runWithHttp(path, Map.of("data", "TRUE","buffersize", String.valueOf(DEFAULT_FILE_BUFFER_SIZE_IN_BYTES)), HttpFSOperation.APPEND);
    }

    public FSDataInputStream open(final String f) throws IOException {
        final String openUrl = getUrl(f, Map.of("offset", String.valueOf(0L),"buffersize", "" + (WebHdfsFileSystem.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES > 0 ? (int) WebHdfsFileSystem.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES : DEFAULT_FILE_BUFFER_SIZE_IN_BYTES)), HttpFSOperation.OPEN.name());
        return new FSDataInputStream(
                new ByteRangeInputStream(
                        new ByteRangeInputStream.URLOpener(null) {
                            @Override
                            protected HttpURLConnection connect(long offset, boolean resolved)
                                    throws IOException {
                                assert offset == 0;
                                HttpURLConnection conn = HttpClient.getStreamUrlConnection(openUrl);
                                conn.setRequestMethod(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                                setURL(conn.getURL());
                                return conn;
                            }
                        },
                        new ByteRangeInputStream.URLOpener(null) {
                            @Override
                            protected HttpURLConnection connect(final long offset, final boolean resolved) throws IOException {
                                final URL offsetUrl = offset == 0L ? new URL(openUrl) : new URL(openUrl + "&" + new OffsetParam(offset));
                                HttpURLConnection conn = HttpClient.getStreamUrlConnection(offsetUrl.toString());
                                conn.setRequestMethod(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                                setURL(conn.getURL());
                                return conn;
                            }
                        }) {
                    private static final String OFFSET_PARAM_PREFIX = OffsetParam.NAME + "=";

                    @Override
                    protected URL getResolvedUrl(HttpURLConnection connection) throws IOException {
                        URL url = connection.getURL();
                        String query = url.getQuery();
                        if (query == null) {
                            return url;
                        }
                        final String lower = StringUtils.toLowerCase(query);
                        if (!lower.startsWith(OFFSET_PARAM_PREFIX) && !lower.contains("&" + OFFSET_PARAM_PREFIX)) {
                            return url;
                        }
                        StringBuilder b = null;
                        for (final StringTokenizer st = new StringTokenizer(query, "&"); st.hasMoreTokens(); ) {
                            final String token = st.nextToken();
                            if (!StringUtils.toLowerCase(token).startsWith(OFFSET_PARAM_PREFIX)) {
                                if (b == null) {
                                    b = new StringBuilder("?").append(token);
                                } else {
                                    b.append('&').append(token);
                                }
                            }
                        }
                        query = b == null ? "" : b.toString();
                        final String urlStr = url.toString();
                        return new URL(urlStr.substring(0, urlStr.indexOf('?')) + query);
                    }
                });
    }

    public boolean exists(String path) {
        try {
            return this.getFileStatus(path) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean delete(String path, boolean recursive) {
        return runWithHttp(path, HttpFSOperation.DELETE, Map.of("recursive", String.valueOf(recursive)), m -> getBooleanResponse(path + "DELETE", m));
    }

    public boolean mkdirs(String path) {
        return runWithHttp(path, HttpFSOperation.MKDIRS, new HashMap<>(), m -> getBooleanResponse("MKDIRS", m));
    }

    public boolean rename(String src, String dst) {
        return runWithHttp(src, HttpFSOperation.RENAME, Map.of("destination", URLEncoder.encode(dst, StandardCharsets.UTF_8)), m -> getBooleanResponse(src + "RENAME", m));
    }

    public boolean truncate(String path, long newLength) {
        return runWithHttp(path, HttpFSOperation.TRUNCATE, Map.of("newlength", String.valueOf(newLength)), m -> getBooleanResponse(path + "TRUNCATE", m));
    }

    public void setPermission(final String p, final FsPermission permission) {
        runWithHttp(p, HttpFSOperation.SETPERMISSION, Map.of("permission", "777"), e -> null);
    }

    public void setOwner(final String p, final String owner, final String group) {
        runWithHttp(p, HttpFSOperation.SETOWNER, Map.of("owner", owner,"group", group), e -> null);
    }

    public MD5MD5CRC32FileChecksum getFileChecksum(final String p) {
        ObjectReader reader = new org.codehaus.jackson.map.ObjectMapper().reader(Map.class);
        return runWithHttp(p, HttpFSOperation.GETFILECHECKSUM, new HashMap<>(), is -> toMD5MD5CRC32FileChecksum(reader.readValue(is)));
    }

    /**
     * http://cn.voidcc.com/question/p-dxrzacex-xw.html 可用这个方法获取文件夹的大小
     * Return the {@link ContentSummary} of a given {@link Path}.
     */
    public ContentSummary getContentSummary(String path) {
        ObjectReader reader = new org.codehaus.jackson.map.ObjectMapper().reader(Map.class);
        Map<?, ?> json = runWithHttp(path, HttpFSOperation.GETCONTENTSUMMARY, new HashMap<>(), reader::readValue);
        return toContentSummary(json);
    }

    //todo 不能这样直接转化
    static ContentSummary toContentSummary(Map<?, ?> json) {
        String j = JsonUtil.obj2Json(json);
        return JsonUtil.json2Obj(j,ContentSummary.class);
    }

    static MD5MD5CRC32FileChecksum toMD5MD5CRC32FileChecksum(Map<?, ?> json) throws IOException {
        if (json == null) {
            return null;
        } else {
            final var m = (Map<?,?>) json.get(FileChecksum.class.getSimpleName());
            final var algorithm = (String) m.get("algorithm");
            final var length = ((Number) m.get("length")).intValue();
            final MD5MD5CRC32FileChecksum checksum;
            switch (MD5MD5CRC32FileChecksum.getCrcTypeFromAlgorithmName(algorithm)) {
                case CRC32:
                    checksum = new MD5MD5CRC32GzipFileChecksum();
                    break;
                case CRC32C:
                    checksum = new MD5MD5CRC32CastagnoliFileChecksum();
                    break;
                default:
                    throw new CodeStackException("Unknown algorithm: " + algorithm);
            }
            checksum.readFields(new DataInputStream(new ByteArrayInputStream(StringUtils.hexStringToByte((String) m.get("bytes")))));
            if (!checksum.getAlgorithmName().equals(algorithm)) {
                throw new CodeStackException("Algorithm not matched. Expected " + algorithm + ", Received " + checksum.getAlgorithmName());
            } else if (length != checksum.getLength()) {
                throw new CodeStackException("Length not matched: length=" + length + ", checksum.getLength()=" + checksum.getLength());
            } else {
                return checksum;
            }
        }
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

    public FileStatus getFileStatus(String path) {
        ObjectReader reader = new org.codehaus.jackson.map.ObjectMapper().reader(Map.class);
        Map<?, ?> json = runWithHttp(path, HttpFSOperation.GETFILESTATUS, new HashMap<>(), reader::readValue);
        return makeMyQualified(toFileStatus(json, true), path);
    }

    private static HdfsFileStatus toFileStatus(Map<?, ?> json, boolean includesType) {
        if (json == null) {
            return null;
        } else {
            Map<?, ?> m = includesType ? (Map<?,?>) json.get(FileStatus.class.getSimpleName()) : json;
            return new HdfsFileStatus(
                    ((Number) m.get("length")).longValue(),
                    m.get("type").equals("DIRECTORY"),
                    ((Number) m.get("replication")).shortValue(),
                    ((Number) m.get("blockSize")).longValue(),
                    ((Number) m.get("modificationTime")).longValue(),
                    ((Number) m.get("accessTime")).longValue(),
                    toFsPermission((String) m.get("permission"), (Boolean) m.get("aclBit"), (Boolean) m.get("encBit")),
                    (String) m.get("owner"),
                    (String) m.get("group"),
                    !m.get("type").equals("SYMLINK") ? null : DFSUtilClient.string2Bytes((String) m.get("symlink")),
                    DFSUtilClient.string2Bytes((String) m.get("pathSuffix")),
                    m.containsKey("fileId") ? ((Number) m.get("fileId")).longValue() : 0L,
                    m.get("childrenNum") == null ? -1 : ((Number)m.get("childrenNum")).intValue(),
                    null,
                    m.containsKey("storagePolicy") ? (byte) ((int) ((Number) m.get("storagePolicy")).longValue()) : 0);
        }
    }
    private static FsPermission toFsPermission(String s, Boolean aclBit, Boolean encBit) {
        FsPermission perm = new FsPermission(Short.parseShort(s, 8));
        boolean aBit = aclBit != null ? aclBit : false;
        boolean eBit = encBit != null ? encBit : false;
        return !aBit && !eBit ? perm : new FsPermissionExtension(perm, aBit, eBit);
    }

    public FileStatus[] listStatus(String path) {
        ObjectReader reader = new org.codehaus.jackson.map.ObjectMapper().reader(Map.class);
        Map<?, ?> json = runWithHttp(path, HttpFSOperation.LISTSTATUS, new HashMap<>(), reader::readValue);
        final Map<?, ?> rootmap = (Map<?, ?>) json.get(FileStatus.class.getSimpleName() + "es");
        final List<?> array = Optional.of(FileStatus.class.getSimpleName())
                .map(rootmap::get)
                .filter(list -> list instanceof List<?>)
                .map(list -> (List<?>) list)
                .orElseGet(ArrayList::new);
        final FileStatus[] statuses = new FileStatus[array.size()];
        int i = 0;
        for (Object object : array) {
            final Map<?, ?> m = (Map<?, ?>) object;
            statuses[i++] = makeMyQualified(toFileStatus(m, false), path);
        }
        return statuses;
    }

    public FileStatus[] listStatus(String f, PathFilter filter) {
        return Stream.of(listStatus(f)).filter(s -> filter.accept(s.getPath())).toArray(FileStatus[]::new);
    }

    /**
     * 将带有特殊字符的路径转换成为url可显示的路径 delimiters 为可以进行特殊处理不去转换的特殊字符集合
     */
    private static String toSafePath(String path, String... exclude) {
        if (exclude.length == 0) {
            return URLEncoder.encode(path, StandardCharsets.UTF_8);
        } else if (exclude.length == 1) {
            final String delimiter = exclude[0];
            if (delimiter.equals(path)) return path;
            final List<String> sl = new ArrayList<>();
            for (String a : path.split(delimiter)) {
                sl.add(URLEncoder.encode(a, StandardCharsets.UTF_8));
            }
            return String.join(delimiter, sl);
        } else {
            final String delimiter = exclude[0];
            final List<String> sl = new ArrayList<>();
            for (String a : path.split(delimiter)) {
                sl.add(toSafePath(a, Stream.of(exclude).skip(1).toArray(String[]::new)));
            }
            return String.join(delimiter, sl);
        }
    }


    private <T> T runWithHttp(String path, HttpFSOperation op, Map<String, String> params, UCFunction<InputStream, T> responseGetter) {
        return HttpClient.execute(op.restType, getUrl(path, params, op.name()), new Headers.Builder().add("Content-Type", op.getContentType()).build(), null, responseGetter);
    }

    /**
     * 返回OutPutStream OutPutStream关闭 连接资源才会关闭
     */
    private FSDataOutputStream runWithHttp(String path, Map<String, String> params, HttpFSOperation op) throws IOException {
        final String url = getUrl(path, params, op.name());
        HttpURLConnection conn = HttpClient.getStreamUrlConnection(url);
        conn.setRequestMethod(op.getRestType());
        conn.setInstanceFollowRedirects(false);
        conn.setChunkedStreamingMode(32 << 10); //32kB-chunk
        if (conn.getResponseCode() == 307) {
            String location = conn.getHeaderField("location");
            conn.disconnect();
            conn = HttpClient.getStreamUrlConnection(location);
            conn.setRequestMethod(op.getRestType());
            conn.setInstanceFollowRedirects(false);
            conn.setChunkedStreamingMode(32 << 10); //32kB-chunk
        }
        HttpURLConnection conn1 = conn;
        return new FSDataOutputStream(new BufferedOutputStream(conn1.getOutputStream(), DEFAULT_FILE_BUFFER_SIZE_IN_BYTES)) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    try {
                        conn1.getInputStream();
                        log.info(op.name() + ":" + "Path:" + path + " " + op.getDesc() + " => " + op.getRestType() + ":" + url);
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

    private String getUrl(final String path, final Map<String, String> params, final String op) {
        Map<String, String> params1 = params.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        selfParams.forEach(params1::put);
        params1.put("op", op);
        return "http://" + getUri().getHost() + ":" + getUri().getPort() + "/webhdfs/" + version + toSafePath(path, "/") + params1.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&", "?", ""));
    }

    private Boolean getBooleanResponse(String op, InputStream is) {
        try {
            return objectMapper.convertValue(objectMapper.readTree(StringUtil.stream2String(is)).findValue("boolean"), Boolean.class);
        } catch (Exception e) {
            throw new CodeStackException(e);
        }
    }

    private FileStatus makeMyQualified(HdfsFileStatus f, String parent) {
        return new FileStatus(f.getLen(), f.isDir(), f.getReplication(), f.getBlockSize(), f.getModificationTime(), f.getAccessTime(), f.getPermission(), f.getOwner(), f.getGroup(), f.isSymlink() ? new Path(f.getSymlink()) : null, f.getFullPath(new Path(parent)));
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
