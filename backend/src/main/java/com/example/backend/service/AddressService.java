package com.example.backend.service;

import com.example.backend.dto.AddressDto;
import com.example.backend.entity.Address;
import com.example.backend.entity.AddressType;
import com.example.backend.entity.User;
import com.example.backend.repository.AddressRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    // Spring Security 설정 참고
    // 현재 로그인한 사용자 정보 조회
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("認証されていません。");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        throw new RuntimeException("ユーザー情報が不正です。");
    }

    // 현재 로그인한 사용자의 주소 목록 조회
    public List<AddressDto> getAddressesForCurrentUser() {
        User user = getCurrentUser();
        List<Address> addresses = addressRepository.findByUser(user);
        return addresses.stream().map(this::toDto).collect(Collectors.toList());
    }

    // 현재 로그인한 사용자의 주소 생성
    @Transactional
    public AddressDto createAddress(AddressDto dto) {
        User user = getCurrentUser();
        Address address = new Address();
        address.setUser(user);

        if (dto.getAddressType() != null) {
            address.setAddressType(AddressType.valueOf(dto.getAddressType()));
        }

        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(dto.getCountry());

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            clearOldDefault(user, null); // 새 주소이므로 제외 ID 없음
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }

        return toDto(addressRepository.save(address));
    }

    // 현재 로그인한 사용자의 주소 수정
    @Transactional
    public void updateAddress(AddressDto dto) {
        User currentUser = getCurrentUser();
        Address address = addressRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("住所が見つかりません。"));

        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("権限がありません。");
        }

        if (dto.getAddressType() != null) {
            address.setAddressType(AddressType.valueOf(dto.getAddressType()));
        }

        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(dto.getCountry());

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            clearOldDefault(currentUser, address.getId());
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }

        addressRepository.save(address);
    }

    // 현재 로그인한 사용자의 주소 소프트 삭제
    @Transactional
    public void softDeleteAddress(Long addressId) {
        User currentUser = getCurrentUser();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("住所が見つかりません。"));

        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("権限がありません。");
        }

        address.setDeletedAt(LocalDateTime.now());
        addressRepository.save(address);
    }

    // 편의 메소드 =====================================================================================================

    // 기본 주소 초기화 (isDefault를 false로 변경)
    private void clearOldDefault(User user, Long excludeId) {
        List<Address> addresses = addressRepository.findByUser(user);
        for (Address a : addresses) {
            if (Boolean.TRUE.equals(a.getIsDefault()) &&
                    (excludeId == null || !a.getId().equals(excludeId))) {
                a.setIsDefault(false);
                addressRepository.save(a);
            }
        }
    }

    // Entity -> DTO 변환
    private AddressDto toDto(Address address) {
        return new AddressDto(
                address.getId(),
                address.getAddressType() != null ? address.getAddressType().name() : null,
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.getIsDefault()
        );
    }

}
