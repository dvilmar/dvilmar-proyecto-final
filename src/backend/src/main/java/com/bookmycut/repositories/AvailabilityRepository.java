package com.bookmycut.repositories;

import com.bookmycut.entities.Availability;
import com.bookmycut.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByStylist(User stylist);
    org.springframework.data.domain.Page<Availability> findByStylist(User stylist, org.springframework.data.domain.Pageable pageable);
    List<Availability> findByStylistAndDayOfWeek(User stylist, DayOfWeek dayOfWeek);
}

