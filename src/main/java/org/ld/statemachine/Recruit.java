package org.ld.statemachine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ld.enums.States;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Recruit {
    Long id;
    States states;
}
