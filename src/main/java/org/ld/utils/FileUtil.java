package org.ld.utils;

import org.ld.exception.CodeStackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressWarnings("unused")
public class FileUtil {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 返回文件流
     * 当文件流关闭时自动删除 临时文件
     */
    public static ResponseEntity<FileSystemResource> getFileSystemResourceFile(
            File file,
            Boolean deleteFileAfterClose) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", "attachment; filename=" + file.getName());
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Last-Modified", new Date().toString());
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));
        FileSystemResource fileSystemResource = new FileSystemResource(file) {
            @Override
            public InputStream getInputStream() throws IOException {
                final InputStream in = super.getInputStream();
                return new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return in.read();
                    }

                    @Override
                    public int read(@NotNull byte[] b) throws IOException {
                        return in.read(b);
                    }

                    @Override
                    public int read(@NotNull byte[] b, int off, int len) throws IOException {
                        return in.read(b, off, len);
                    }

                    @Override
                    public long skip(long n) throws IOException {
                        return in.skip(n);
                    }

                    @Override
                    public int available() throws IOException {
                        return in.available();
                    }

                    @Override
                    public void close() throws IOException {
                        in.close();
                        if (deleteFileAfterClose && file.exists()) {
                            LOG.info("删除文件" + (file.delete() ? "成功" : "失败"));
                        } // 自动删除文件
                    }

                    @Override
                    public synchronized void mark(int readlimit) {
                        in.mark(readlimit);
                    }

                    @Override
                    public synchronized void reset() throws IOException {
                        in.reset();
                    }

                    @Override
                    public boolean markSupported() {
                        return super.markSupported();
                    }
                };
            }
        };
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(fileSystemResource);
    }

    /**
     * 解压文件到指定路径
     * 并且换回解压目录
     */
    public static List<String> unZip(File zipFile, Boolean deleteZipFile) {
        List<String> paths = new ArrayList<>();
        try (ZipFile zip = new ZipFile(zipFile, Charset.forName("gbk"))) {
            for (Enumeration<?> entries = zip.entries();
                 entries.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();
                try (InputStream in = zip.getInputStream(entry)) {
                    String outPath = (zipFile.getParentFile().toString() + "/" + zipEntryName).replace("/", File.separator);
                    if (new File(outPath).isDirectory()) {
                        continue;
                    }
                    try (OutputStream out = new FileOutputStream(outPath)) {
                        byte[] buf1 = new byte[2048];
                        int len;
                        while ((len = in.read(buf1)) > 0) {
                            out.write(buf1, 0, len);
                        }
                        paths.add(outPath);
                    }
                }
            }
        } catch (IOException e) {
            throw new CodeStackException(e);
        } finally {
            if (deleteZipFile && zipFile.exists()) {
                LOG.info("删除文件" + (zipFile.delete() ? "成功" : "失败"));
            } // 自动删除文件
        }
        return paths;
    }

    /**
     * 读取Fileduixiang并持久化到本地
     */
    public static File asFile(MultipartFile file) {
        File tmp = new File("/tmp/" + SnowflakeId.get() + "/" + file.getOriginalFilename());
        try {
            LOG.info("创建文件" + (tmp.getParentFile().mkdir() ? "成功" : "失败"));
            file.transferTo(tmp);
        } catch (IOException e) {
            throw new CodeStackException(e);
        }
        return tmp;
    }

    /**
     * 读取执行文本文件的内容
     */
    public static String readText(String fileName, Boolean deleteFileAfterRead) {
        File file = new File(fileName);
        StringBuilder sbf = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append(tempStr);
            }
            return sbf.toString();
        } catch (IOException e) {
            throw new CodeStackException(e);
        } finally {
            if (deleteFileAfterRead && file.exists()) {
                LOG.info("删除文件" + (file.delete() ? "成功" : "失败"));
                if (file.getParentFile().exists() && Objects.requireNonNull(file.getParentFile().listFiles()).length == 0) {
                    LOG.info("删除文件夹" + (file.getParentFile().delete() ? "成功" : "失败"));
                }
            }
        }
    }

}
