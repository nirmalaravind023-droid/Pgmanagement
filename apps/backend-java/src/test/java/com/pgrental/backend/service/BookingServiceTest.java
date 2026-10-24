package com.pgrental.backend.service;

import com.pgrental.backend.entity.*;
import com.pgrental.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock BookingRepository bookingRepository;
    @Mock PgRepository pgRepository;
    @Mock RoomInventoryRepository roomInventoryRepository;
    @Mock UserRepository userRepository;

    @InjectMocks BookingService bookingService;

    private User testUser() {
        return User.builder().id("u1").phone("+91999").role(User.Role.STUDENT).build();
    }

    private Pg testPg() {
        return Pg.builder().id("pg1").name("Test PG").rent(10000).deposit(20000).build();
    }

    private RoomInventory testInventory(Pg pg) {
        RoomInventory inv = new RoomInventory();
        inv.setId("inv1");
        inv.setPg(pg);
        inv.setType(RoomInventory.RoomType.SINGLE);
        inv.setTotal(5);
        inv.setOccupied(2);
        return inv;
    }

    @Test
    void createBooking_success() {
        User user = testUser();
        Pg pg = testPg();
        RoomInventory inv = testInventory(pg);

        when(pgRepository.findById("pg1")).thenReturn(Optional.of(pg));
        when(roomInventoryRepository.findByPgIdAndType("pg1", RoomInventory.RoomType.SINGLE))
                .thenReturn(Optional.of(inv));
        when(bookingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Booking result = bookingService.createBooking(user, "pg1", RoomInventory.RoomType.SINGLE);

        assertThat(result.getStatus()).isEqualTo(Booking.BookingStatus.CONFIRMED);
        assertThat(inv.getOccupied()).isEqualTo(3);
    }

    @Test
    void createBooking_pgNotFound_throws() {
        when(pgRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(testUser(), "bad-id", RoomInventory.RoomType.SINGLE))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("PG not found");
    }

    @Test
    void createBooking_roomTypeMissing_throws() {
        Pg pg = testPg();
        when(pgRepository.findById("pg1")).thenReturn(Optional.of(pg));
        when(roomInventoryRepository.findByPgIdAndType("pg1", RoomInventory.RoomType.SINGLE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(testUser(), "pg1", RoomInventory.RoomType.SINGLE))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void createBooking_noRoomsAvailable_throws() {
        Pg pg = testPg();
        RoomInventory inv = testInventory(pg);
        inv.setOccupied(5); // full

        when(pgRepository.findById("pg1")).thenReturn(Optional.of(pg));
        when(roomInventoryRepository.findByPgIdAndType("pg1", RoomInventory.RoomType.SINGLE))
                .thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> bookingService.createBooking(testUser(), "pg1", RoomInventory.RoomType.SINGLE))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No rooms available");
    }

    @Test
    void cancelBooking_success_restoresInventory() {
        User user = testUser();
        Pg pg = testPg();
        RoomInventory inv = testInventory(pg);
        inv.setOccupied(3);

        Booking booking = Booking.builder()
                .id("b1").user(user).pg(pg).roomType(RoomInventory.RoomType.SINGLE)
                .status(Booking.BookingStatus.CONFIRMED).build();

        when(userRepository.findByPhone("+91999")).thenReturn(Optional.of(user));
        when(bookingRepository.findByIdAndUserId("b1", "u1")).thenReturn(Optional.of(booking));
        when(roomInventoryRepository.findByPgIdAndType("pg1", RoomInventory.RoomType.SINGLE))
                .thenReturn(Optional.of(inv));
        when(bookingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Booking result = bookingService.cancelBooking("b1", "+91999");

        assertThat(result.getStatus()).isEqualTo(Booking.BookingStatus.CANCELLED);
        assertThat(inv.getOccupied()).isEqualTo(2);
    }

    @Test
    void cancelBooking_notFound_throws() {
        User user = testUser();
        when(userRepository.findByPhone("+91999")).thenReturn(Optional.of(user));
        when(bookingRepository.findByIdAndUserId("bad", "u1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking("bad", "+91999"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getUserBookings_returnsList() {
        User user = testUser();
        when(userRepository.findByPhone("+91999")).thenReturn(Optional.of(user));
        when(bookingRepository.findByUserId("u1")).thenReturn(List.of());

        List<Booking> result = bookingService.getUserBookings("+91999");

        assertThat(result).isEmpty();
    }
}
