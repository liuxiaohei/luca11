package org.ld.controller;

import org.ld.utils.SnowflakeId;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

/**
 * @author ld
 */
@RestController
@RequestMapping("/file")
public class FileController {

    /**
     * 上传文件
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public Object upload(@RequestParam MultipartFile file) throws IOException {
        final var fileName = file.getOriginalFilename();
        final var suffix = Optional.ofNullable(fileName).filter(e -> e.contains(".")).map(e -> e.substring(e.lastIndexOf(".") + 1)).orElse("");
        final var key = SnowflakeId.get().toString() + "_" + (file.getSize() / 1024) + Optional.of(suffix).map(e -> "." + e).orElse("");
        final var inputStream = file.getInputStream();
        return null;
    }

    @RequestMapping(value = "/upload/single", method = RequestMethod.POST)
    public Mono<String> single(@RequestPart("file") Mono<FilePart> file) throws IOException {
        //此时已转换为File类，具体的业务逻辑我就忽略了
        return file.map(filePart -> {
            Path tempFile = null;
            try {
                tempFile = Files.createTempFile("test", filePart.filename());
            } catch (IOException e) {
                e.printStackTrace();
            }
            AsynchronousFileChannel channel = null;
            try {
                channel = AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            DataBufferUtils.write(filePart.content(), channel, 0).doOnComplete(() -> {
                System.out.println("finish");
            }).subscribe();
            return tempFile;
        })
                .map(Path::toFile)
                .flatMap(fileSinge -> file.map(FilePart::filename));
    }

    @RequestMapping(value = "/upload/numty", method = RequestMethod.POST)
    public Mono<List<String>> more(@RequestPart("file") Flux<FilePart> file) throws IOException {
        //此时已转换为File类，具体的业务逻辑我就忽略了
        return file.map(filePart -> {
            Path tempFile = null;
            try {
                tempFile = Files.createTempFile("test", filePart.filename());
            } catch (IOException e) {
                e.printStackTrace();
            }
            filePart.transferTo(tempFile.toFile());
            return tempFile;
        }).map(Path::toFile)
                .flatMap(fileSinge -> file.map(FilePart::filename)).collectList();
    }

    @RequestMapping(value = "/get",produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public byte[] getImage() throws IOException {
        File file = new File("D:/test.jpg");
        FileInputStream inputStream = new FileInputStream(file);
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes, 0, inputStream.available());
        return bytes;
    }

}