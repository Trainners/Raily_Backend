package io.trainners.raily_backend.domain.seat.model;

import lombok.Getter;

import java.util.List;

@Getter
public class SeatOption {
    private final String carNumber;
    private final String seatNumber;
    private final List<Boolean> availabilityBySegment;

    public SeatOption(String carNumber, String seatNumber, List<Boolean> availabilityBySegment){
        this.carNumber = carNumber;
        this.seatNumber = seatNumber;
        this.availabilityBySegment = availabilityBySegment;
    }

    public int getInitialContiguousRun(){
        int run = 0;
        for(boolean available : availabilityBySegment){
            if(!available){
                break;
            }
            run++;
        }
        return run;
    }

    public int getLongestContiguousRun(){
        int longest = 0;
        int current = 0;
        for(boolean available : availabilityBySegment){
            current = available ? current + 1 : 0;
            longest = Math.max(longest, current);
        }
        return longest;
    }

    public int getAvailableSegmentCount(){
        return (int) availabilityBySegment.stream().filter(Boolean::booleanValue).count();
    }
}
