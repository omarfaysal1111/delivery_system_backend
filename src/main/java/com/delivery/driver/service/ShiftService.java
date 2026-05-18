package com.delivery.driver.service;

import com.delivery.common.exception.ConflictException;
import com.delivery.common.exception.ResourceNotFoundException;
import com.delivery.driver.domain.Shift;
import com.delivery.driver.domain.ShiftBooking;
import com.delivery.driver.dto.ShiftBookingDto;
import com.delivery.driver.dto.ShiftDto;
import com.delivery.driver.repository.ShiftBookingRepository;
import com.delivery.driver.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftBookingRepository shiftBookingRepository;

    public List<ShiftDto> getAvailableShifts(UUID driverId) {
        return shiftRepository.findAllByIsActiveTrueAndDateGreaterThanEqualOrderByDateAsc(LocalDate.now())
                .stream()
                .map(shift -> {
                    long count = shiftBookingRepository.countByShiftId(shift.getId());
                    boolean bookedByMe = shiftBookingRepository.existsByDriverIdAndShiftId(driverId, shift.getId());
                    return ShiftDto.from(shift, count, bookedByMe);
                })
                .filter(dto -> dto.getBookedCount() < dto.getMaxDrivers() || dto.isBookedByMe())
                .toList();
    }

    @Transactional
    public ShiftBookingDto bookShift(UUID driverId, UUID shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found"));

        if (shiftBookingRepository.existsByDriverIdAndShiftId(driverId, shiftId)) {
            throw new ConflictException("Already booked this shift");
        }

        long count = shiftBookingRepository.countByShiftId(shiftId);
        if (count >= shift.getMaxDrivers()) {
            throw new ConflictException("Shift is fully booked");
        }

        ShiftBooking booking = ShiftBooking.builder()
                .driverId(driverId).shiftId(shiftId).build();
        return ShiftBookingDto.from(shiftBookingRepository.save(booking));
    }

    @Transactional
    public ShiftDto createShift(Shift shift) {
        Shift saved = shiftRepository.save(shift);
        return ShiftDto.from(saved, 0, false);
    }
}
