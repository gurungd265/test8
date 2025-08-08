package com.example.backend.repository.user;

import com.example.backend.entity.user.Address;
import com.example.backend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address,Long> {
    /*
        (deleted_at IS NULL 자동 필터링)
     */
    List<Address> findByUser(User user);
}
