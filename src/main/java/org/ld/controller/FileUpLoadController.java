package org.ld.controller;

import org.ld.utils.UuidUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * @author ld
 */
@RestController
@RequestMapping("/file")
public class FileUpLoadController {

    /**
     * 上传文件
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public Object upload(@RequestParam MultipartFile file) throws IOException {
        final String fileName = file.getOriginalFilename();
        final String suffix = Optional.ofNullable(fileName).filter(e -> e.contains(".")).map(e -> e.substring(e.lastIndexOf(".") + 1)).orElse("");
        final String key = UuidUtils.getShortUuid() + "_" + (file.getSize() / 1024) + Optional.of(suffix).map(e -> "." + e).orElse("");
        final InputStream inputStream = file.getInputStream();
        return null;
    }

}