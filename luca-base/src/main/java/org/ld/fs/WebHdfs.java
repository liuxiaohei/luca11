package org.ld.fs;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ld.uc.UCFunction;
import org.ld.utils.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

/** http://hadoop.apache.org/docs/r1.0.4/webhdfs.html */
@Slf4j
@AllArgsConstructor
public class WebHdfs {
    private static final UCFunction<InputStream, Boolean> boolResult = is -> JsonUtil.getResponse(is, "boolean", Boolean.class);
    private static final String version = "v1";
    private final Map<String, String> selfParams;
    private final String address;

    public Boolean delete(final String path, final boolean recursive) {
        return HttpClient.delete(getUrl(path, Map.of("recursive", recursive + ""), "DELETE"), HttpClient.JSON_HEAD_SUPPLIER.get(), boolResult);
    }

    public Boolean mkdirs(final String path) {
        return HttpClient.put(getUrl(path, new HashMap<>(), "MKDIRS"), HttpClient.JSON_HEAD_SUPPLIER.get(), null, boolResult);
    }

    public Boolean rename(final String src, final String dst) {
        return HttpClient.put(getUrl(src, Map.of("destination", StringUtil.toSafeUrlPath(dst)), "RENAME"), HttpClient.JSON_HEAD_SUPPLIER.get(), null, boolResult);
    }

    public Boolean truncate(final String path, long newLength) {
        return HttpClient.post(getUrl(path, Map.of("newlength", newLength + ""), "TRUNCATE"), HttpClient.JSON_HEAD_SUPPLIER.get(), null, boolResult);
    }

    public void setPermission(final String p, final String permission) {
        HttpClient.put(getUrl(p, Map.of("permission", permission), "SETPERMISSION"), HttpClient.JSON_HEAD_SUPPLIER.get(), null, e -> 0);
    }

    public void setOwner(final String p, final String owner, final String group) {
        HttpClient.put(getUrl(p, Map.of("owner", owner, "group", group), "SETOWNER"), HttpClient.JSON_HEAD_SUPPLIER.get(), null, e -> 0);
    }

    public Map<String, Object> getFileChecksum(final String p) {
        return HttpClient.get(getUrl(p, new HashMap<>(), "GETFILECHECKSUM"), HttpClient.JSON_HEAD_SUPPLIER.get(), JsonUtil::stream2Map);
    }

    /** http://cn.voidcc.com/question/p-dxrzacex-xw.html 可用这个方法获取文件夹的大小 */
    public Map<String, Object> getContentSummary(final String path) {
        return HttpClient.get(getUrl(path, new HashMap<>(), "GETCONTENTSUMMARY"), HttpClient.JSON_HEAD_SUPPLIER.get(), JsonUtil::stream2Map);
    }

    public void concat(final String trg, final String[] srcs) {
        HttpClient.post(getUrl(trg, Map.of("sources", String.join(",", srcs)), "CONCAT"), HttpClient.JSON_HEAD_SUPPLIER.get(), null, e -> 0);
    }

    public Map<String, Object> getFileStatus(final String path) {
        return HttpClient.get(getUrl(path, new HashMap<>(), "GETFILESTATUS"), HttpClient.JSON_HEAD_SUPPLIER.get(), JsonUtil::stream2Map);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listStatus(final String path) {
        return HttpClient.get(getUrl(path, new HashMap<>(), "LISTSTATUS"), HttpClient.JSON_HEAD_SUPPLIER.get(), is -> (List<Map<String, Object>>) Optional.ofNullable((Map<?, ?>) ((Map<?, ?>) JsonUtil.stream2Map(is)).get("FileStatuses")).map(r -> r.get("FileStatus")).filter(list -> list instanceof List<?>).map(list -> (List<?>) list).orElseGet(ArrayList::new));
    }

    public OutputStream create(final String path) {
        return HttpClient.getOutputStreamByUrl(getUrl(path, Map.of("data", "TRUE", "overwrite", "true", "buffersize", FileUtil.DEFAULT_BUFFER_SIZE_STRING), "CREATE"), "PUT", FileUtil.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES);
    }

    public OutputStream append(final String path) {
        return HttpClient.getOutputStreamByUrl(getUrl(path, Map.of("data", "TRUE", "buffersize", FileUtil.DEFAULT_BUFFER_SIZE_STRING), "APPEND"), "POST", FileUtil.DEFAULT_FILE_BUFFER_SIZE_IN_BYTES);
    }

    public <T> T open(final String f,UCFunction<InputStream, T> handler) {
        return HttpClient.execute("GET", getUrl(f, Map.of("offset", "0", "buffersize", FileUtil.DEFAULT_BUFFER_SIZE_STRING), "OPEN"), HttpClient.STREAM_HEAD_SUPPLIER.get(), null, handler);
    }

    private String getUrl(final String path, final Map<String, String> params, final String op) {
        return "http://" + address + "/webhdfs/" + version + StringUtil.toSafeUrlPath(path, "/") + Stream.concat(Map.of("op", op).entrySet().stream(), Stream.concat(params.entrySet().stream(), selfParams.entrySet().stream())).map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&", "?", ""));
    }
}