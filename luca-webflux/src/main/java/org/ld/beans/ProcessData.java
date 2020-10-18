package org.ld.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessData {

    public String id;
    public String processParam;
    public String executorParam;

    public ProcessData setId(String id) {
        return new ProcessData(id, processParam, executorParam);
    }

}
