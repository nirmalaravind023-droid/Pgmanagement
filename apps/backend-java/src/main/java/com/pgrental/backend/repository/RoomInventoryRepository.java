package com.pgrental.backend.repository;

import com.pgrental.backend.entity.RoomInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface RoomInventoryRepository extends JpaRepository<RoomInventory, String> {

    // Pessimistic write lock prevents double-booking when concurrent requests check availability
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RoomInventory r WHERE r.pg.id = :pgId AND r.type = :type")
    Optional<RoomInventory> findByPgIdAndTypeForUpdate(String pgId, RoomInventory.RoomType type);

    Optional<RoomInventory> findByPgIdAndType(String pgId, RoomInventory.RoomType type);
}
