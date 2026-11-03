package com.example.gamecompanion.controller;

import com.example.gamecompanion.model.User;
import com.example.gamecompanion.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * 登录 / 退出控制器。
 */
@Controller
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** 打开登录页；如果已经登录则直接回到首页 */
    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("loginUser") != null) {
            return "redirect:/";
        }
        return "login";
    }

    /**
     * 处理登录表单：
     * 1. 根据用户名查询账号；
     * 2. 校验密码与账号状态；
     * 3. 登录成功把用户对象存入 Session（键名 loginUser），跳转首页；
     * 4. 失败则回到登录页并提示错误。
     */
    @PostMapping("/login")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          HttpSession session,
                          Model model) {
        String name = username == null ? "" : username.trim();
        Optional<User> userOpt = userRepository.findByUsername(name);

        if (userOpt.isEmpty()) {
            model.addAttribute("error", "账号不存在，请检查用户名");
            model.addAttribute("username", name);
            return "login";
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(password)) {
            model.addAttribute("error", "密码错误，请重新输入");
            model.addAttribute("username", name);
            return "login";
        }
        if (Boolean.FALSE.equals(user.getEnabled())) {
            model.addAttribute("error", "该账号已被禁用，请联系管理员");
            model.addAttribute("username", name);
            return "login";
        }

        session.setAttribute("loginUser", user);
        return "redirect:/";
    }

    /** 退出登录：销毁 Session 并回到登录页 */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
