package com.school.security.controllers.auth;

import com.school.security.dtos.requests.LoginReqDto;
import com.school.security.dtos.requests.RefreshReqDto;
import com.school.security.dtos.requests.UserReqDto;
import com.school.security.dtos.responses.CodeResDto;
import com.school.security.dtos.responses.LoginResDto;
import com.school.security.dtos.responses.UserResDto;
import com.school.security.securities.services.JwtService;
import com.school.security.services.contracts.UserService;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://192.168.1.133:3000/"})
public class AuthController {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    public AuthController(
            UserService userService,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResDto login(@RequestBody LoginReqDto credential) {
        this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(credential.email(), credential.password()));
        var user = this.userService.findByEmail(credential.email());
        var jwt = this.jwtService.generateToken(user);
        var refreshToken = this.jwtService.generateRefreshToken(new HashMap<>(), user);
        return new LoginResDto(jwt, refreshToken, user.getRoles().getFirst().getName());
    }

    @PostMapping("/register")
    public UserResDto register(@RequestBody UserReqDto userReqDto) {
        return this.userService.createOrUpdate(userReqDto);
    }

    @PostMapping("/refresh")
    public LoginResDto refreshToken(@RequestBody RefreshReqDto refreshReqDto) {
        var username = jwtService.extractUsername(refreshReqDto.refreshToken());
        var user = this.userService.findByEmail(username);
        if (this.jwtService.isTokenValid(refreshReqDto.refreshToken(), user)) {
            var jwt = this.jwtService.generateToken(user);
            return new LoginResDto(
                    jwt, refreshReqDto.refreshToken(), user.getRoles().get(1).getName());
        }
        return null;
    }

    @PostMapping("/code")
    public CodeResDto generateCode(@RequestBody String email) {
        var user = this.userService.findByEmail(email);
        var jwt = this.jwtService.generateToken(user);
        var refreshToken = this.jwtService.generateRefreshToken(new HashMap<>(), user);
        if (user != null) {
            int code = (int) (Math.random() * 900000) + 100000;
            System.out.println(code);
            return new CodeResDto(code, jwt, refreshToken);
        } else {
            throw new RuntimeException("User not defined");
        }
    }
}
