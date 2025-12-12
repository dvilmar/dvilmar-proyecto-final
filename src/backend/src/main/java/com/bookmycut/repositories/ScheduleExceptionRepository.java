package com.bookmycut.repositories;

import com.bookmycut.entities.ScheduleException;
import com.bookmycut.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleExceptionRepository extends JpaRepository<ScheduleException, Long> {
    List<ScheduleException> findByDate(LocalDate date);
    List<ScheduleException> findByStylist(User stylist);
    List<ScheduleException> findByStylistAndDate(User stylist, LocalDate date);
    
    @Query("SELECT e FROM ScheduleException e WHERE e.date = :date AND (e.stylist = :stylist OR e.stylist IS NULL)")
    List<ScheduleException> findByDateAndStylistOrNull(
        @Param("date") LocalDate date,
        @Param("stylist") User stylist
    );
}

