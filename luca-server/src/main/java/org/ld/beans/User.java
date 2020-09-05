package org.ld.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {

    //用户ID
    private int userId;

    //用户姓名
    private String userName;
}
