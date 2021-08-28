package com.weiheng.secondkill.auth.api;

import com.weiheng.secondkill.auth.cache.UserRepository;
import com.weiheng.secondkill.entity.User;
import com.weiheng.secondkill.enums.StatusCode;
import com.weiheng.secondkill.auth.model.request.LoginParams;
import com.weiheng.secondkill.auth.domain.UserDetail;
import com.weiheng.secondkill.response.BaseResponse;
import com.weiheng.secondkill.auth.jwt.utils.JwtTokenUtil;
import com.weiheng.secondkill.auth.model.response.UserLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class LoginController {

    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserRepository userRepository;


    @PostMapping(value = "/login/")
    public BaseResponse<UserLoginVO> login(@RequestBody LoginParams request) {

        // Spring Security执行身份认证的组件
        Authentication authenticate = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        BaseResponse<UserLoginVO> response = new BaseResponse<UserLoginVO>(StatusCode.SUCCESS);
        UserDetail userDetail = (UserDetail) authenticate.getPrincipal();
        UserLoginVO userLoginVO = new UserLoginVO();
        User user = userDetail.getUser();
        userLoginVO.setUsername(user.getUserName());
        userLoginVO.setToken(jwtTokenUtil.generateAccessToken(user));
        response.setData(userLoginVO);
        userRepository.insert(userDetail);
        return response;
    }
}
