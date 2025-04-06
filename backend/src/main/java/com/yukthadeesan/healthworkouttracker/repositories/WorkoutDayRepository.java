package com.yukthadeesan.healthworkouttracker.repositories;

import com.yukthadeesan.healthworkouttracker.models.WorkoutDay;
import com.yukthadeesan.healthworkouttracker.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutDayRepository extends JpaRepository<WorkoutDay, Long> {
    WorkoutDay findByUserAndDate(User user, LocalDate date);
    List<WorkoutDay> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
    long countByUserAndCompletedAndDateBetween(User user, Boolean completed, LocalDate startDate, LocalDate endDate);
}