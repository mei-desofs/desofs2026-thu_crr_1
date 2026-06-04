package com.techstore.app.repository;

import com.techstore.app.domain.carrier.Carrier;
import com.techstore.app.domain.carrier.CarrierId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarrierRepository extends JpaRepository<Carrier, CarrierId> {

}
