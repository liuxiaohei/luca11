package org.ld.beans;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class OnSuccessResp<T> {
    private T data;

    public OnSuccessResp(T data) {
        this.data = data;
    }

    @SuppressWarnings("unused")
    public Boolean getSuccess() {
        return true;
    }
}
