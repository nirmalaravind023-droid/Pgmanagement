package com.pgrental.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreatePgRequest {
    private String name;
    private String address;
    private String description;
    private Double lat;
    private Double lng;
    private Integer rent;
    private Integer deposit;
    private List<String> amenities;
    private List<RoomInventoryRequest> rooms;

    @Data
    public static class RoomInventoryRequest {
        private String type; // SINGLE, DOUBLE, TRIPLE
        private Integer total;
        private Integer price;
    }
}
