package org.ld.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.ld.beans.RespBean;
import org.ld.utils.JsonUtil;
import org.ld.utils.StringUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @author ld
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @ApiOperation(value = "上传文件", produces = "application/json")
    @SneakyThrows
    @ResponseBody
    @RequestMapping(value = "/upload/stream", method = RequestMethod.POST, headers = "content-type=multipart/form-data", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件", dataType = "MultipartFile", paramType = "query"),
    })
    public void uploadFilesWhithStream(HttpServletRequest request, HttpServletResponse response) {
        var sis = request.getInputStream();
        String location = parseRequestParam("location=", request.getQueryString());
        try {
            response.setStatus(HttpStatus.OK.value());
            response.getWriter().write(JsonUtil.obj2Json(new RespBean<>("OK")));
            System.out.println(StringUtil.stream2String(new BufferedInputStream(sis)));
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
