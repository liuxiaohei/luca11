package org.ld.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

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
}
