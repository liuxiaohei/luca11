package org.ld.fs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ld.uc.UCFunction;
import org.ld.utils.*;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** http://hadoop.apache.org/docs/r1.0.4/webhdfs.html */
@Slf4j
@AllArgsConstructor
public class WebHdfsFileSystem {

    private static final String version = "v1";
    private final Map<String, String> selfParams;
    private final URI uri;

    public boolean delete(String path, boolean recursive) {
        return run(path, "DELETE", "DELETE", Map.of("recursive", recursive + ""), WebHdfsFileSystem::getBooleanResponse);
    }

    public boolean mkdirs(String path) {
        return run(path, "MKDIRS", "PUT", new HashMap<>(), WebHdfsFileSystem::getBooleanResponse);
    }

    public boolean rename(String src, String dst) {
        return run(src, "RENAME", "PUT", Map.of("destination", StringUtil.toSafeUrlPath(dst)), WebHdfsFileSystem::getBooleanResponse);
    }

    public boolean truncate(String path, long newLength) {
        return run(path, "TRUNCATE", "POST", Map.of("newlength", newLength + ""), WebHdfsFileSystem::getBooleanResponse);
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

    public OutputStream create(String path) {
        return HttpClient.getOutputStreamByUrl(getUrl(path, Map.of("data", "TRUE", "overwrite", "true", "buffersize", FileUtil.DEFAULT_BUFFER_SIZE_STRING), "CREATE"), "PUT", FileUtil.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES);
    }

    public OutputStream append(String path) {
        return HttpClient.getOutputStreamByUrl(getUrl(path, Map.of("data", "TRUE", "buffersize", FileUtil.DEFAULT_BUFFER_SIZE_STRING), "APPEND"), "POST", FileUtil.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES);
    }

    public InputStream open(final String f) {
        return HttpClient.execute("GET", getUrl(f, Map.of("offset", "0", "buffersize", FileUtil.DEFAULT_BUFFER_SIZE_STRING), "OPEN"), HttpClient.STREAM_HEAD_SUPPLIER.get(), null, is -> is);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listStatus(String path) {
        return (List<Map<String, Object>>) Optional.ofNullable((Map<?, ?>) run(path, "LISTSTATUS", "GET", new HashMap<>(), JsonUtil::stream2Map).get("FileStatuses")).map(r -> r.get("FileStatus")).filter(list -> list instanceof List<?>).map(list -> (List<?>) list).orElseGet(ArrayList::new);
    }

    private <T> T run(String path, String opName, String method, Map<String, String> params, UCFunction<InputStream, T> responseGetter) {
        return HttpClient.execute(method, getUrl(path, params, opName), HttpClient.JSON_HEAD_SUPPLIER.get(), null, responseGetter);
    }

    private String getUrl(final String path, final Map<String, String> params, final String op) {
        return "http://" + uri.getHost() + ":" + uri.getPort() + "/webhdfs/" + version + StringUtil.toSafeUrlPath(path, "/") + Stream.concat(Map.of("op", op).entrySet().stream(), Stream.concat(params.entrySet().stream(), selfParams.entrySet().stream())).map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&", "?", ""));
    }

    private static Boolean getBooleanResponse(InputStream is) throws Exception {
        return new ObjectMapper().convertValue(new ObjectMapper().readTree(StringUtil.stream2String(is)).findValue("boolean"), Boolean.class);
    }
}