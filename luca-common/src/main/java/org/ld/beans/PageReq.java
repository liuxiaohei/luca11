package org.ld.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@AllArgsConstructor
@NoArgsConstructor
@With // https://www.jianshu.com/p/6660142f70c7
@ApiModel("分页请求体")
public class PageReq {

    @ApiModelProperty("页数")
    private Long page;

    @ApiModelProperty("每页大小")
    private Long size;

    @JsonIgnore
    public Long getLimit() {
        return getSize();
    }

    @JsonIgnore
    public Long getOffset() {
        return (Math.max((getPage() - 1), 0)) * getSize();
    }
}
