package com.bookmycut.repositories;

import com.bookmycut.entities.ServiceOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceOfferRepository extends JpaRepository<ServiceOffer, Long> {
}







