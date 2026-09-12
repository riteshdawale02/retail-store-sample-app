package com.amazon.sample.ui.web;

import com.amazon.sample.ui.auth.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

/**
 * Note: there is no POST /login handler here on purpose. Spring Security's
 * formLogin(loginPage("/login")) intercepts POST /login itself, as long as
 * the login form posts to /login with fields named "username" and
 * "password". Only the GET page and signup flow are ours to handle.
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public Mono<String> signup(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {
        return authService.signup(username, email, password)
                .thenReturn("redirect:/login")
                .onErrorResume(e -> {
                    model.addAttribute("error", e.getMessage());
                    return Mono.just("signup");
                });
    }
}