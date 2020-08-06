package org.ld.beans;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class SuccessRespBean<T> {
    private T data;

    public SuccessRespBean(T data) {
        this.data = data;
    }

    @SuppressWarnings("unused")
    public Boolean getSuccess() {
        return true;
    }
}
