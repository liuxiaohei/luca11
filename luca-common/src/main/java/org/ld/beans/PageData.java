package org.ld.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel("分页返回实体")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageData<T> {

    @ApiModelProperty("数量")
    private long count = 0;

    @ApiModelProperty("列表")
    private List<T> list;
}
