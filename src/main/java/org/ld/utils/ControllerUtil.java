package org.ld.utils;

import org.ld.beans.ResponseBodyBean;
import org.ld.enums.SystemErrorCodeEnum;
import org.ld.exception.CodeStackException;
import org.ld.exception.ErrorCode;
import org.ld.functions.UCSupplier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.stream.Stream;

public class ControllerUtil {

    private static final Logger LOG = Logger.newInstance();

    /**
     * 转换响应结构体
     */
    public static Object convertResponseBody(String shortUUid,UCSupplier<Object> point) {
        try {
            final ResponseBodyBean<Object> result = new ResponseBodyBean<>();
            result.setData(point.get());
            result.setErrorCode(0);
            result.setMessage("成功");
            LOG.info(() -> shortUUid + ":Response Body : " + JsonUtil.obj2Json(result));
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Throwable e) {
            LOG.printStackTrace(shortUUid,e);
            final CodeStackException se = Optional.of(e)
                    .map(t -> {
                        Throwable t1 = t;
                        while (null != t1) {
                            if (t1 instanceof CodeStackException) return (CodeStackException) t1;//找到第一个CodeException
                            t1 = t1.getCause();
                        }
                        return null;
                    })
                    .orElseGet(() -> SystemErrorCodeEnum.getSystemError(e));
            final ResponseBodyBean<Object> result = new ResponseBodyBean<>();
            result.setErrorCode(Optional.of(se)
                    .map(CodeStackException::getErrorCode)
                    .map(ErrorCode::getCode)
                    .orElseGet(SystemErrorCodeEnum.UNKNOWN::getCode));
            result.setStackTrace(Optional.of(e)
                    .map(error -> {
                        final StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        return Stream.of(sw.toString().split("\n\t"))
                                .skip(1)
                                .map(str -> str.replace("\n", ""))
                                .toArray(String[]::new);
                    }).orElse(null));
            result.setMessage(Optional.of(se)
                    .map(CodeStackException::getErrorCode)
                    .filter(i -> !i.getCode().equals(SystemErrorCodeEnum.UNKNOWN.getCode()))
                    .map(ErrorCode::getMessage)
                    .orElseGet(e::getMessage));
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
