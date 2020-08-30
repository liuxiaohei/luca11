package org.ld.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class JobQuery {
    public String jobName;
    public Integer jobId;
    public String serviceName;
    public Integer offSet;
    public Integer limit;
}
