package org.ld.controller;

import org.ld.utils.SnowflakeId;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

/**
 * @author ld
 */
//@RestController
//@RequestMapping("/file")
public class FileUpLoadController {

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

}