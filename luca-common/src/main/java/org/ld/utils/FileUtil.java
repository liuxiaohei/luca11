package org.ld.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.ld.exception.CodeStackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 操作本地文件工具类
 */
public class FileUtil {

    public static final Integer DEFAULT_FILE_BUFFER_SIZE_IN_BYTES = 10 * 1024 * 1024; // 10MB

    public static final String DEFAULT_BUFFER_SIZE_STRING = DEFAULT_FILE_BUFFER_SIZE_IN_BYTES + "";

    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 返回文件流
     * 当文件流关闭时自动删除 临时文件
     */
    public static ResponseEntity<FileSystemResource> getFileSystemResourceFile(
            File file,
            Boolean deleteFileAfterClose,
            Boolean deleteDirAfterClose) {
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
                    public int read(byte[] b) throws IOException {
                        return in.read(b);
                    }

                    @Override
                    public int read(byte[] b, int off, int len) throws IOException {
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
                            if (deleteDirAfterClose && file.getParentFile().exists() && Optional
                                    .ofNullable(file.getParentFile())
                                    .map(File::listFiles)
                                    .map(e -> e.length == 0).orElse(false)) {
                                LOG.info("删除文件夹" + (file.getParentFile().delete() ? "成功" : "失败"));
                            }
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
     * 上传文件到指定的路径
     */
    public static void saveFile(MultipartFile f, String dirBasePath) throws IOException {
        String fileName = f.getOriginalFilename();
        String type = f.getContentType();
        System.out.println(fileName + " ," + type);
        String filePath = dirBasePath + File.separator;
        if (!FileUtil.isDir(filePath)) {
            FileUtil.makeDirs(filePath);
        }
        File file = new File(dirBasePath + File.separator + fileName);
        file.createNewFile();
        f.transferTo(file);
    }

    public static boolean isDir(String dirPath) {
        File f = new File(dirPath);
        return f.exists() && f.isDirectory();
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
     * 创建多级目录
     */
    public static void makeDirs(String path) {
        File file = new File(path);
        if (!file.exists() && !file.isDirectory()) {
            LOG.info("创建文件" + (file.mkdirs() ? "成功" : "失败"));
        } else {
            System.out.println("创建目录失败：" + path);
        }
    }

    /**
     * 递归获取指定路径下的所有文件
     */
    public static void traverseFolder(String path, List<File> result) {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (null == files || files.length == 0) {
                System.out.println("文件夹是空的!");
            } else {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        System.out.println("文件夹:    " + file2.getName());
                        traverseFolder(file2.getAbsolutePath(), result);
                    } else {
                        result.add(file2);
                        System.out.println("文件:      " + file2.getName());
                    }
                }
            }
        } else {
            System.out.println("文件不存在!");
        }
    }


    /**
     * 读取执行文本文件的内容
     */
    public static TextFile readText(String fileName, Boolean deleteFileAfterRead) {
        File file = new File(fileName);
        StringBuilder sbf = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append(tempStr);
            }
            String md5 = DigestUtils.md5Hex(new FileInputStream(fileName));
            return new TextFile(sbf.toString(), fileName, md5, file.length());
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

    public static void deleteTextFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            LOG.info("删除文件" + (file.delete() ? "成功" : "失败"));
            if (file.getParentFile().exists() && Objects.requireNonNull(file.getParentFile().listFiles()).length == 0) {
                LOG.info("删除文件夹" + (file.getParentFile().delete() ? "成功" : "失败"));
            }
        }
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        try (ByteArrayOutputStream ignored = out) {
            return out.toByteArray();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TextFile {
        String text;
        String filePath;
        String md5;
        long fileSize;
    }

    private static final int BUFFER_SIZE = 2 * 1024;

    /**
     * https://blog.csdn.net/lidai352710967/article/details/89887978
     * 压缩本地文件
     */
    public static void toZip(String srcDir, OutputStream out, boolean KeepDirStructure) {
        long start = System.currentTimeMillis();
        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            File sourceFile = new File(srcDir);
            compress(sourceFile, zos, sourceFile.getName(), KeepDirStructure);
            long end = System.currentTimeMillis();
            delTempChild(sourceFile);
            System.out.println("压缩完成，耗时：" + (end - start) + " ms");
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils", e);
        }
    }

    /**
     * 递归删除文件夹
     */
    public static void delTempChild(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();//获取文件夹下所有子文件夹
            for (String child : Optional.ofNullable(children).orElseGet(() -> new String[0])) {
                delTempChild(new File(file, child));
            }
        }
        LOG.info("删除文件" + (file.delete() ? "成功" : "失败"));
    }

    private static void compress(File sourceFile,
                                 ZipOutputStream zos,
                                 String name,
                                 boolean KeepDirStructure) throws Exception {
        byte[] buf = new byte[BUFFER_SIZE];
        if (sourceFile.isFile()) {
            zos.putNextEntry(new ZipEntry(name));
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                if (KeepDirStructure) {
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    zos.closeEntry();
                }
            } else {
                for (File file : listFiles) {
                    if (KeepDirStructure) {
                        compress(file, zos, name + "/" + file.getName(), true);
                    } else {
                        compress(file, zos, file.getName(), false);
                    }
                }
            }
        }
    }

    /**
     * 创建 文件
     */
    @Deprecated
    public static void createFileWithOutChannel(String path, int size) throws IOException {
        var file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                System.out.println("创建文件夹失败");
            }
            if (!file.createNewFile()) {
                System.out.println("创建文件失败");
            }
        }
        try (var fos = new FileOutputStream(file)) {
            var b = new byte[1024 * 4];
            for (var i = 0; i < 1024; i++) {
                b[i] = (byte) (i % 2);
            }
            for (var i = 1; i < size / b.length; i++) {
                fos.write(b);
            }
            fos.flush();
        }
    }

    /**
     * 创建 文件
     */
    public static void createFileWithChannel(String path) throws IOException {
        var file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                System.out.println("创建文件夹失败");
            }
            if (!file.createNewFile()) {
                System.out.println("创建文件失败");
            }
        }
        try (var fos = new FileOutputStream(file)) {
            var channel = fos.getChannel();
            var buffer = ByteBuffer.allocate(200 * 1024 * 1024);
            int i = 1;
            while (buffer.position() < buffer.limit() - 50) {
                buffer.putInt(i++);
            }
            buffer.flip();
            channel.write(buffer);
            fos.flush();
        }
    }

    public static void copyFileUseNIO(String src, String dst) throws IOException {
        try (var fi = new FileInputStream(new File(src));
             var fo = new FileOutputStream(new File(dst));
             var inChannel = fi.getChannel();//获得传输通道channel
             var outChannel = fo.getChannel()) {
            var buffer = ByteBuffer.allocate(1024); //创建buffer
            while (true) {
                int eof = inChannel.read(buffer);
                if (eof == -1) {
                    break;
                }
                buffer.flip(); //重设一下buffer的position=0，limit=position
                outChannel.write(buffer);
                buffer.clear();//写完要重置buffer，重设position=0,limit=capacity
            }
        }
    }

    /**
     * 将本地文件copy到指定的outputStream
     */
    public void copyFromLocalFile(boolean delSrc, String src, OutputStream outputStream) throws IOException {
        var file = new File(src);
        if (!file.exists()) {
            throw new CodeStackException(String.format("File %s does not exist.", src));
        }
        if (!file.isFile()) {
            throw new CodeStackException(String.format("%s isn't a file.", src));
        }
        try (var is = new FileInputStream(src); var os = outputStream) {
            FileUtil.copyInputStream2OutputStream(is, os);
        }
        if (delSrc) {
            LOG.info("删除文件" + (file.delete() ? "成功" : "失败"));
        }
    }

    public static void copyInputStream2OutputStream(InputStream from, OutputStream to) {
        try {
            var buffer = new byte[DEFAULT_FILE_BUFFER_SIZE_IN_BYTES];
            var len = from.read(buffer);
            while (len != -1) {
                to.write(buffer, 0, len);
                len = from.read(buffer);
            }
        } catch (Exception e) {
            throw new CodeStackException(e);
        }
    }
}
