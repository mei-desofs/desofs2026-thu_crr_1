package com.techstore.app.repository;

import com.techstore.app.domain.carrier.Carrier;
import com.techstore.app.domain.carrier.CarrierId;
import com.techstore.app.domain.user.SupabaseUserId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarrierRepository extends JpaRepository<Carrier, CarrierId> {
    Optional<Carrier> findByUserSupabaseUserId(SupabaseUserId supabaseUserId);
}
