package com.pgrental.backend.service;

import com.pgrental.backend.entity.Booking;
import com.pgrental.backend.entity.Pg;
import com.pgrental.backend.entity.RoomInventory;
import com.pgrental.backend.entity.User;
import com.pgrental.backend.repository.BookingRepository;
import com.pgrental.backend.repository.PgRepository;
import com.pgrental.backend.repository.RoomInventoryRepository;
import com.pgrental.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final PgRepository pgRepository;
    private final RoomInventoryRepository roomInventoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public Booking createBooking(User user, String pgId, RoomInventory.RoomType roomType) {
        Pg pg = pgRepository.findById(pgId).orElseThrow(() -> new RuntimeException("PG not found"));
        RoomInventory inventory = roomInventoryRepository.findByPgIdAndTypeForUpdate(pgId, roomType)
                .orElseThrow(() -> new RuntimeException("Room type not available for this PG"));

        if (inventory.getOccupied() >= inventory.getTotal()) {
            throw new RuntimeException("No rooms available for this type");
        }

        inventory.setOccupied(inventory.getOccupied() + 1);
        roomInventoryRepository.save(inventory);

        Booking booking = Booking.builder()
                .user(user)
                .pg(pg)
                .roomType(roomType)
                .status(Booking.BookingStatus.CONFIRMED)
                .build();

        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public List<Booking> getUserBookings(String userPhone) {
        User user = userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bookingRepository.findByUserId(user.getId());
    }

    @Transactional
    public Booking cancelBooking(String bookingId, String userPhone) {
        User user = userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new RuntimeException("Booking not found or not yours"));
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking already cancelled");
        }
        RoomInventory inventory = roomInventoryRepository.findByPgIdAndType(
                booking.getPg().getId(), booking.getRoomType())
                .orElse(null);
        if (inventory != null) {
            inventory.setOccupied(Math.max(0, inventory.getOccupied() - 1));
            roomInventoryRepository.save(inventory);
        }
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }
}
