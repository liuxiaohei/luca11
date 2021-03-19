package org.ld.controller;

import lombok.extern.slf4j.Slf4j;
import org.ld.exception.CodeStackException;
import org.ld.utils.SnowflakeId;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
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
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    SnowflakeId snowflakeId;

    /**
     * 上传文件
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public Object upload(@RequestParam MultipartFile file) throws IOException {
        final var fileName = file.getOriginalFilename();
        final var suffix = Optional.ofNullable(fileName).filter(e -> e.contains(".")).map(e -> e.substring(e.lastIndexOf(".") + 1)).orElse("");
        final var key = snowflakeId.get().toString() + "_" + (file.getSize() / 1024) + Optional.of(suffix).map(e -> "." + e).orElse("");
        final var inputStream = file.getInputStream();
        return null;
    }

    @RequestMapping(value = "/upload/single", method = RequestMethod.POST)
    public Mono<String> single(@RequestPart("file") Mono<FilePart> file) {
        return file.map(filePart -> {
            try {
                var tempFile = Files.createTempFile("test", filePart.filename());
                var channel = AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE);
                DataBufferUtils.write(filePart.content(), channel, 0).doOnComplete(() -> log.info("finish")).subscribe();
                return tempFile;
            } catch (Exception e) {
                throw CodeStackException.of(e);
            }
        })
                .map(Path::toFile)
                .flatMap(fileSinge -> file.map(FilePart::filename));
    }

    @RequestMapping(value = "/upload/numty", method = RequestMethod.POST)
    public Mono<List<String>> more(@RequestPart("file") Flux<FilePart> file) {
        //此时已转换为File类，具体的业务逻辑我就忽略了
        return file.map(filePart -> {
            Path tempFile;
            try {
                tempFile = Files.createTempFile("test", filePart.filename());
            } catch (IOException e) {
                throw CodeStackException.of(e);
            }
            filePart.transferTo(tempFile.toFile());
            return tempFile;
        }).map(Path::toFile)
                .flatMap(fileSinge -> file.map(FilePart::filename)).collectList();
    }

    @GetMapping(value = "/get", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public byte[] getImage() throws IOException {
        File file = new File("D:/test.jpg");
        FileInputStream inputStream = new FileInputStream(file);
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes, 0, inputStream.available());
        return bytes;
    }

}