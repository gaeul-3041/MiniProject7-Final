package com.aivle.mini7.dto;

import com.aivle.mini7.model.Board;
import com.aivle.mini7.model.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private String id;
    private String pw;
    private String name;
    private String phone;
    private int idType;

    public UserDto(User user) {
        this.id = user.getId();
        this.pw = user.getPw();
        this.name = user.getName();
        this.phone = user.getPhone();
        this.idType = user.getIdType();
    }
}
