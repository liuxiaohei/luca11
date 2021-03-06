package org.ld.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ld.enums.SystemErrorCodeEnum;
import org.ld.exception.CodeStackException;
import org.ld.exception.ErrorCode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.stream.Stream;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true) // 反序列化时忽略未知属性
public class RespBean<T> {
    private T data;
    private Integer errorCode;
    private String errorMsgDescription;
    private String message;
    private String[] stackTrace;
    private Boolean success;

    public RespBean(T data) {
        this.data = data;
        this.success = true;
    }

    public RespBean(Throwable e) {
        final var se = Optional.of(e)
                .map(t -> {
                    var t1 = t;
                    while (null != t1) {
                        if (t1 instanceof CodeStackException) return (CodeStackException) t1;
                        t1 = t1.getCause();
                    }
                    return null;
                })
                .orElseGet(() -> CodeStackException.of(e));
        this.setErrorCode(Optional.of(se)
                .map(CodeStackException::getErrorCode)
                .map(ErrorCode::getCode)
                .orElseGet(SystemErrorCodeEnum.UNKNOWN::getCode));
        this.setStackTrace(Optional.of(e)
                .map(error -> {
                    final var sw = new StringWriter();
                    final var pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    var strings = Stream.of(sw.toString().split("\n\t"))
                            .skip(1)
                            .map(str -> str.replace("\n", "").replace("\t",""))
                            .toArray(String[]::new);
                    pw.flush();
                    return strings;
                }).orElse(null));
        this.setErrorMsgDescription(Optional.of(se)
                .map(CodeStackException::getErrorCode)
                .filter(i -> !i.getCode().equals(SystemErrorCodeEnum.UNKNOWN.getCode()))
                .map(ErrorCode::getMessage)
                .orElseGet(e::getMessage));
        this.setSuccess(false);
        this.setMessage(e.getMessage());
    }

    public Boolean getSuccess() {
        return success;
    }
}
