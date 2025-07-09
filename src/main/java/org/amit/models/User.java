package org.amit.models;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class User {
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
}
