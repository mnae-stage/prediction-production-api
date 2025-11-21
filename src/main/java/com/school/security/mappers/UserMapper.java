package com.school.security.mappers;

import com.school.security.dtos.requests.UserReqDto;
import com.school.security.dtos.responses.RoleResDto;
import com.school.security.dtos.responses.UserResDto;
import com.school.security.entities.Role;
import com.school.security.entities.User;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements Mapper<UserReqDto, User, UserResDto> {
    private final RoleMapper roleMapper;

    public UserMapper(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Override
    public User fromDto(UserReqDto d) {
        User user = new User();
        user.setEmail(d.email());
        user.setFirstname(d.firstname());
        user.setGender(d.gender());
        user.setPwd(d.password());
        user.setLastname(d.lastname());
        return user;
    }

    @Override
    public UserResDto toDto(User entity) {
        return new UserResDto(
                entity.getUsersId(),
                entity.getFirstname(),
                entity.getLastname(),
                entity.getEmail(),
                entity.getGender(),
                toRoleResDto(entity.getRoles()));
    }

    private List<RoleResDto> toRoleResDto(List<Role> roles) {
        return roles.stream().map(this.roleMapper::toDto).collect(Collectors.toList());
    }
}
