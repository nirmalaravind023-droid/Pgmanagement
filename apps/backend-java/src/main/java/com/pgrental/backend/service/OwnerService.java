package com.pgrental.backend.service;

import com.pgrental.backend.dto.CreatePgRequest;
import com.pgrental.backend.entity.Booking;
import com.pgrental.backend.entity.Pg;
import com.pgrental.backend.entity.RoomInventory;
import com.pgrental.backend.entity.User;
import com.pgrental.backend.repository.BookingRepository;
import com.pgrental.backend.repository.PgRepository;
import com.pgrental.backend.repository.RoomInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerService {
    private final PgRepository pgRepository;
    private final RoomInventoryRepository roomInventoryRepository;
    private final BookingRepository bookingRepository;

    private void assertOwner(User user) {
        if (user.getRole() != User.Role.OWNER) {
            throw new RuntimeException("Forbidden: only PG owners can perform this action");
        }
    }

    @Transactional(readOnly = true)
    public List<Pg> getOwnerPgs(User owner) {
        assertOwner(owner);
        return pgRepository.findByOwnerId(owner.getId());
    }

    @Transactional
    public Pg createPg(User owner, CreatePgRequest req) {
        assertOwner(owner);
        Pg pg = Pg.builder()
                .name(req.getName())
                .address(req.getAddress())
                .description(req.getDescription())
                .lat(req.getLat())
                .lng(req.getLng())
                .owner(owner)
                .ownerName(owner.getName())
                .rent(req.getRent())
                .deposit(req.getDeposit())
                .amenities(req.getAmenities())
                .verified(false)
                .build();
        pg = pgRepository.save(pg);

        if (req.getRooms() != null) {
            final Pg savedPg = pg;
            List<RoomInventory> rooms = req.getRooms().stream().map(r -> {
                RoomInventory inv = new RoomInventory();
                inv.setPg(savedPg);
                inv.setType(RoomInventory.RoomType.valueOf(r.getType().toUpperCase()));
                inv.setTotal(r.getTotal());
                inv.setOccupied(0);
                inv.setRent(r.getPrice());
                return inv;
            }).collect(Collectors.toList());
            roomInventoryRepository.saveAll(rooms);
        }
        return pg;
    }

    @Transactional
    public Pg updatePg(String pgId, User owner, CreatePgRequest req) {
        assertOwner(owner);
        Pg pg = pgRepository.findById(pgId)
                .orElseThrow(() -> new RuntimeException("PG not found"));
        if (!pg.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Forbidden: not your PG");
        }
        pg.setName(req.getName());
        pg.setAddress(req.getAddress());
        pg.setDescription(req.getDescription());
        pg.setLat(req.getLat());
        pg.setLng(req.getLng());
        pg.setRent(req.getRent());
        pg.setDeposit(req.getDeposit());
        pg.setAmenities(req.getAmenities());
        return pgRepository.save(pg);
    }

    @Transactional
    public void deletePg(String pgId, User owner) {
        assertOwner(owner);
        Pg pg = pgRepository.findById(pgId)
                .orElseThrow(() -> new RuntimeException("PG not found"));
        if (!pg.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Forbidden: not your PG");
        }
        pgRepository.delete(pg);
    }

    @Transactional(readOnly = true)
    public List<Booking> getOwnerBookings(User owner) {
        assertOwner(owner);
        List<Pg> pgs = pgRepository.findByOwnerId(owner.getId());
        return pgs.stream()
                .flatMap(pg -> pg.getBookings().stream())
                .collect(Collectors.toList());
    }
}
