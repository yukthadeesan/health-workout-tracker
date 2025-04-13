package com.yukthadeesan.healthworkouttracker.controllers;

import com.yukthadeesan.healthworkouttracker.models.User;
import com.yukthadeesan.healthworkouttracker.models.WorkoutDay;
import com.yukthadeesan.healthworkouttracker.services.WorkoutTrackingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutTrackingController {

    private final WorkoutTrackingService workoutTrackingService;

    @Autowired
    public WorkoutTrackingController(WorkoutTrackingService workoutTrackingService) {
        this.workoutTrackingService = workoutTrackingService;
    }

    /**
     * Get the authenticated user's ID
     * Note: This assumes your User entity has an 'id' field and your
     * authentication principal can be cast to User or contains the user ID.
     * Adjust according to your actual authentication setup.
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        // Try to get user ID from session
        HttpSession session = request.getSession(false);
        if (session != null) {
            Long userId = (Long) session.getAttribute("USER_ID");
            if (userId != null) {
                System.out.println("Found user ID in session: " + userId);
                return userId;
            }
        }

        // If no user ID in session, try other methods (fallback)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            System.out.println("Auth Principal type: " + (auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "null"));
            System.out.println("Auth Name: " + auth.getName());

            try {
                return Long.parseLong(auth.getName());
            } catch (NumberFormatException e) {
                // Log the issue
                System.err.println("Could not parse user ID from auth name: " + auth.getName());
            }
        }

        throw new RuntimeException("User not authenticated");
    }

    /**
     * Record a workout for the current day
     * @param completed whether the workout was completed
     * @return the saved workout day
     */
    @PostMapping("/record")
    public ResponseEntity<?> recordWorkout(
            HttpServletRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam boolean completed) {

        try {
            Long userId = getCurrentUserId(request);
            System.out.println("Recording workout for user ID: " + userId + ", completed: " + completed);

            if (date == null) {
                date = LocalDate.now();
            }

            WorkoutDay workout = workoutTrackingService.recordWorkout(userId, date, completed);
            System.out.println("Workout recorded successfully: " + workout.getId());

            return ResponseEntity.ok(convertWorkoutToMap(workout));
        } catch (Exception e) {
            System.err.println("Error recording workout: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get workout days for a specified range
     */
    @GetMapping("/range")
    public ResponseEntity<?> getWorkoutsInRange(
            HttpServletRequest request,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long userId = getCurrentUserId(request);
        List<WorkoutDay> workouts = workoutTrackingService.getWorkoutDays(userId, startDate, endDate);

        List<Map<String, Object>> workoutData = workouts.stream()
                .map(this::convertWorkoutToMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(workoutData);
    }

    /**
     * Get workout days for the current week
     */
    @GetMapping("/week")
    public ResponseEntity<?> getCurrentWeekWorkouts(
            HttpServletRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {

        Long userId = getCurrentUserId(request);
        if (weekStart == null) {
            // Default to current week starting from Sunday
            LocalDate today = LocalDate.now();
            weekStart = today.minusDays(today.getDayOfWeek().getValue() % 7);
        }

        List<WorkoutDay> workouts = workoutTrackingService.getWeekWorkouts(userId, weekStart);

        List<Map<String, Object>> workoutData = workouts.stream()
                .map(this::convertWorkoutToMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(workoutData);
    }

    /**
     * Get workout days for the current month
     */
    @GetMapping("/month")
    public ResponseEntity<?> getCurrentMonthWorkouts(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<WorkoutDay> workouts = workoutTrackingService.getCurrentMonthWorkouts(userId);

        List<Map<String, Object>> workoutData = workouts.stream()
                .map(this::convertWorkoutToMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(workoutData);
    }

    /**
     * Get workout statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getWorkoutStats(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Map<String, Object> stats = workoutTrackingService.getCurrentMonthStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Delete a workout record
     */
    @DeleteMapping("/{date}")
    public ResponseEntity<?> deleteWorkout(HttpServletRequest request,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long userId = getCurrentUserId(request);
        workoutTrackingService.deleteWorkout(userId, date);

        return ResponseEntity.ok(Map.of("message", "Workout deleted successfully"));
    }

    @GetMapping("/debug")
    public ResponseEntity<?> debugAuth(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            Long userIdFromSession = session != null ? (Long) session.getAttribute("USER_ID") : null;

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Map<String, Object> debug = new HashMap<>();
            debug.put("isAuthenticated", auth.isAuthenticated());
            debug.put("principalType", auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "null");
            debug.put("name", auth.getName());
            debug.put("authorities", auth.getAuthorities().toString());
            debug.put("userIdFromSession", userIdFromSession);

            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Add this to your WorkoutTrackingController
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok(Map.of("message", "API is working!"));
    }

    /**
     * Convert WorkoutDay entity to a map for JSON response
     */
    private Map<String, Object> convertWorkoutToMap(WorkoutDay workout) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", workout.getId());
        map.put("date", workout.getDate());
        map.put("completed", workout.getCompleted());
        return map;
    }
}