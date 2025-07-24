package com.example.backend.controller;

import com.example.backend.dto.AddressDto;
import com.example.backend.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // 내 주소 목록 조회
    @GetMapping
    public ResponseEntity<List<AddressDto>> getMyAddresses() {
        List<AddressDto> addresses = addressService.getAddressesForCurrentUser();
        return ResponseEntity.ok(addresses);
    }

    // 내 주소 생성
    @PostMapping
    public ResponseEntity<AddressDto> createAddress(@Valid @RequestBody AddressDto dto) {
        AddressDto created = addressService.createAddress(dto);
        return ResponseEntity.ok(created);
    }

    // 내 주소 수정
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressDto dto) {
        dto.setId(id);  // URL id를 DTO에 세팅
        addressService.updateAddress(dto);
        return ResponseEntity.noContent().build();
    }

    // 내 주소 삭제 (소프트 삭제)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.softDeleteAddress(id);
        return ResponseEntity.noContent().build();
    }
}