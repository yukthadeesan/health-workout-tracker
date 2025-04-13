package com.yukthadeesan.healthworkouttracker.controllers;

import com.yukthadeesan.healthworkouttracker.models.User;
import com.yukthadeesan.healthworkouttracker.repositories.UserRepository;
import com.yukthadeesan.healthworkouttracker.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000") // For React frontend
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("userId", registeredUser.getId());
            response.put("username", registeredUser.getUsername());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> credentials, HttpSession session) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        System.out.println("Login attempt for user: " + username);

        User user = userService.authenticateUser(username, password);

        if (user != null) {
            // Store user ID in session
            session.setAttribute("USER_ID", user.getId());
            System.out.println("User ID " + user.getId() + " stored in session");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());

            System.out.println("Login successful for user: " + username + " with ID: " + user.getId());
            return ResponseEntity.ok(response);
        } else {
            System.out.println("Login failed for user: " + username);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Invalid username or password");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpSession session) {
        session.invalidate();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logged out successfully");

        return ResponseEntity.ok(response);
    }
}