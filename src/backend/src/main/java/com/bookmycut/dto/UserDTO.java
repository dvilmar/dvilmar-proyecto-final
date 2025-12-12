package com.bookmycut.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for transferring user information from server to client.
 * Does not include sensitive information such as password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private Boolean active;
}







