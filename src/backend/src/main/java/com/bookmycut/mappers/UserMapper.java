package com.bookmycut.mappers;

import com.bookmycut.dto.UserDTO;
import com.bookmycut.entities.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Component responsible for mapping {@link User} entities to DTOs
 * to facilitate data transfer between application layers.
 */
@Component
public class UserMapper {

    /**
     * Converts a {@link User} entity to a {@link UserDTO}.
     * Does not include sensitive information such as password.
     *
     * @param user The {@link User} entity to map.
     * @return A {@link UserDTO} object with mapped data.
     */
    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setActive(user.getActive());

        return dto;
    }

    /**
     * Converts a list of {@link User} entities to a list of {@link UserDTO}.
     *
     * @param users List of {@link User} entities.
     * @return List of {@link UserDTO}.
     */
    public List<UserDTO> toDTOList(List<User> users) {
        if (users == null) {
            return null;
        }
        return users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
