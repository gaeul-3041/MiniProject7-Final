package com.aivle.mini7.controller;

import com.aivle.mini7.dto.UserDto;
import com.aivle.mini7.model.User;
import com.aivle.mini7.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class UserController {
    private final UserService userService;

    @GetMapping("/recovpassword")
    public String showRecoverPage() {
        return "recovpassword";
    }

    @PostMapping("/recover-password")
    public String recoverPassword(
            @RequestParam String id,
            @RequestParam String name,
            @RequestParam String phone,
            Model model
    ) {
        // 전화번호 형식 검증
        if (!phone.matches("\\d{11}")) { // 숫자만 11자리 허용
            model.addAttribute("error", "전화번호는 11자리 숫자만 입력 가능합니다.");
            return "recovpassword"; // 다시 비밀번호 찾기 페이지로
        }

        try {
            // 비밀번호 찾기 서비스 호출
            String password = userService.recoverPassword(id, name, phone);
            model.addAttribute("password", password);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "recovpassword"; // 동일 페이지로 리다이렉트
    }


    @GetMapping("/agree")
    public String showAgreementPage() {
        return "agree";
    }

    @PostMapping("/agree")
    public String processAgreement(@RequestParam(name = "agreement", required = true) boolean agreed) {
        if (agreed) {
            return "redirect:/signup";
        } else {
            return "redirect:/agree?error=true";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@Valid @ModelAttribute UserDto userDto,
                               BindingResult bindingResult,
                               HttpSession session,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            User user = userService.login(userDto.getId(), userDto.getPw());
            session.setAttribute("user", user);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@Valid @ModelAttribute UserDto userDto,
                                BindingResult bindingResult,
                                Model model) {
        // 유효성 검사: 휴대폰 번호가 11자리 숫자인지 확인
        if (!userDto.getPhone().matches("\\d{11}")) {
            model.addAttribute("error", "휴대폰 번호는 11자리 숫자만 입력 가능합니다.");
            return "signup";
        }

        if (bindingResult.hasErrors()) {
            return "signup";
        }

        try {
            userService.createUser(userDto);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/api/check-id")
    @ResponseBody
    public boolean checkIdAvailability(@RequestParam String id) {
        System.out.println("Received ID: " + id); // 로그: 입력 ID 확인
        boolean isAvailable = userService.isIdAvailable(id);
        System.out.println("ID Available: " + isAvailable); // 로그: 결과 확인
        return isAvailable;
    }


}
