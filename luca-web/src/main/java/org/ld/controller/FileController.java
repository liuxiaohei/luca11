package org.ld.controller;

import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ld.beans.RespBean;
import org.ld.utils.JsonUtil;
import org.ld.utils.StringUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @author ld
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @ApiOperation(value = "上传文件", produces = "application/json")
    @SneakyThrows
    @ResponseBody
    @RequestMapping(value = "/upload/stream", method = RequestMethod.POST, headers = "content-type=multipart/form-data", produces = "application/json")
    public void uploadFilesWhithStream(HttpServletRequest request, HttpServletResponse response, @RequestPart MultipartFile file) {
//        String location = parseRequestParam("location=", request.getQueryString());
        try {
            log.info(StringUtil.stream2String(file.getInputStream()));
            response.setStatus(HttpStatus.OK.value());
            response.getWriter().write(JsonUtil.obj2Json(new RespBean<>("OK")));
        } catch (Throwable e) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().write(JsonUtil.obj2Json(new RespBean<>(e)));
        }
    }

    @SneakyThrows
    private String parseRequestParam(String paramName, String url) {
        String finalParam = null;
        String parsedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        String[] params = parsedUrl.split("&");
        for (String param : params) {
            if (param.contains(paramName)) {
                finalParam = param.substring(param.indexOf(paramName) + paramName.length());
            }
        }
        return finalParam;
    }

}
