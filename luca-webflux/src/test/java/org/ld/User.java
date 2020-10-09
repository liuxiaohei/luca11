package org.ld;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashSet;

@AllArgsConstructor
@NoArgsConstructor
public class User {
    public boolean valid;
    public HashSet<String> phones;
    public String[] hobbies;
    public String username;
    public int age;
}
