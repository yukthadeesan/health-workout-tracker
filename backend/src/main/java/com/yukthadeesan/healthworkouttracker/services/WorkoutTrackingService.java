package com.yukthadeesan.healthworkouttracker.services;

import com.yukthadeesan.healthworkouttracker.models.User;
import com.yukthadeesan.healthworkouttracker.models.WorkoutDay;
import com.yukthadeesan.healthworkouttracker.repositories.UserRepository;
import com.yukthadeesan.healthworkouttracker.repositories.WorkoutDayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkoutTrackingService {

    private final WorkoutDayRepository workoutDayRepository;
    private final UserRepository userRepository;

    @Autowired
    public WorkoutTrackingService(WorkoutDayRepository workoutDayRepository, UserRepository userRepository) {
        this.workoutDayRepository = workoutDayRepository;
        this.userRepository = userRepository;
    }

    /**
     * Records a workout for a user on a specific date
     * @param userId the user's ID
     * @param date the date of the workout
     * @param completed whether the workout was completed
     * @return the saved WorkoutDay entity
     */
    public WorkoutDay recordWorkout(Long userId, LocalDate date, boolean completed) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Check if a workout record already exists for this date
        WorkoutDay existingWorkout = workoutDayRepository.findByUserAndDate(user, date);

        if (existingWorkout != null) {
            // Update existing record
            existingWorkout.setCompleted(completed);
            return workoutDayRepository.save(existingWorkout);
        } else {
            // Create new record
            WorkoutDay newWorkout = new WorkoutDay(date, completed, user);
            return workoutDayRepository.save(newWorkout);
        }
    }

    /**
     * Gets all workout days for a user within a date range
     * @param userId the user's ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return a list of workout days
     */
    public List<WorkoutDay> getWorkoutDays(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return workoutDayRepository.findByUserAndDateBetween(user, startDate, endDate);
    }

    /**
     * Gets all workout days for a user in the current month
     * @param userId the user's ID
     * @return a list of workout days in the current month
     */
    public List<WorkoutDay> getCurrentMonthWorkouts(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        return getWorkoutDays(userId, firstDayOfMonth, lastDayOfMonth);
    }

    /**
     * Gets workout days for a specific week
     * @param userId the user's ID
     * @param weekStartDate the start date of the week (usually Sunday or Monday)
     * @return a list of workout days for the week
     */
    public List<WorkoutDay> getWeekWorkouts(Long userId, LocalDate weekStartDate) {
        LocalDate weekEndDate = weekStartDate.plusDays(6); // 7 days in a week
        return getWorkoutDays(userId, weekStartDate, weekEndDate);
    }

    /**
     * Counts the number of completed workouts in a date range
     * @param userId the user's ID
     * @param startDate the start date
     * @param endDate the end date
     * @return the count of completed workouts
     */
    public long countCompletedWorkouts(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return workoutDayRepository.countByUserAndCompletedAndDateBetween(
                user, true, startDate, endDate);
    }

    /**
     * Deletes a workout record for a specific date
     * @param userId the user's ID
     * @param date the date to delete
     */
    public void deleteWorkout(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        WorkoutDay workout = workoutDayRepository.findByUserAndDate(user, date);
        if (workout != null) {
            workoutDayRepository.delete(workout);
        }
    }

    /**
     * Get workout statistics for a user in the current month
     * @param userId the user's ID
     * @return a map of statistics
     */
    public Map<String, Object> getCurrentMonthStats(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        long totalDaysInMonth = today.lengthOfMonth();
        long completedWorkouts = workoutDayRepository.countByUserAndCompletedAndDateBetween(
                user, true, firstDayOfMonth, lastDayOfMonth);

        // Calculate streak (consecutive workout days leading up to today)
        int currentStreak = 0;
        LocalDate checkDate = today;

        while (true) {
            WorkoutDay workout = workoutDayRepository.findByUserAndDate(user, checkDate);
            if (workout != null && workout.getCompleted()) {
                currentStreak++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }

        return Map.of(
                "totalDaysInMonth", totalDaysInMonth,
                "completedWorkouts", completedWorkouts,
                "completionRate", (double) completedWorkouts / totalDaysInMonth,
                "currentStreak", currentStreak
        );
    }
}