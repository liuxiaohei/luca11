package org.ld.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConditionRule {
    private  String name;
    private  String rule;

    public int getParamCount() {
        String strRes = "%s";
        int n = 0;
        int index;
        index = rule.indexOf(strRes);
        while (index != -1) {
            n++;
            index = rule.indexOf(strRes, index + 1);
        }
        return n;
    }
}