package org.ld.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResponseBodyBean<T> {

    private Integer errorCode ;
    private String message;
    private T data;
    private String[] stackTrace;
}
