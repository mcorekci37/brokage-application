package com.emce.brokage.auth;

import com.emce.brokage.auth.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByEmail(String email);
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.assets WHERE c.id = :customerId")
    Optional<Customer> findCustomerWithAssetsById(@Param("customerId") Integer customerId);

}
