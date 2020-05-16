package org.ld.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RespBean<T> {
    private Boolean success;
    private T data;
    private Integer errorCode ;
    private String errorMsgDescription;
    private String message;
    private String[] stackTrace;
}
