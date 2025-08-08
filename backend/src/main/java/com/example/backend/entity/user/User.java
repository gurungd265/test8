package com.example.backend.entity.user;

import com.example.backend.entity.cart.Cart;
import com.example.backend.entity.product.ProductReview;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name="users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL") //기본 조회 시 자동으로 삭제된 데이터는 제외
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false) //비밀번호를 암호화한 값 저장
    private String passwordHash;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = true, updatable = true)
    private Gender gender;

    @Column(name = "phone", nullable = true, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, updatable = true)
    private UserType userType = UserType.USER; // or ADMIN

    @CreationTimestamp //생성 시 자동 기록 (수정 불가) (not null)
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp //수정 시 자동 기록 (not null)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at") //soft delete용 (null)
    private LocalDateTime deletedAt;

    // ================================================= ProductReview =================================================
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ProductReview> reviews;

    // ===================================================== Cart =====================================================
    // User 객체를 저장 또는 삭제할 때 연관된 Cart도 자동으로 함께 처리
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Cart> carts = new ArrayList<>();

    public void addCart(Cart cart) {
        if (!carts.contains(cart)) { // 중복 추가 방지
            carts.add(cart);
            cart.setUser(this); // Cart 쪽 User 설정
        }
    }

    public void removeCart(Cart cart) {
        if (carts.remove(cart)) {
            cart.setUser(null); // Cart 쪽 User 초기화
        }
    }

    // ================================================ Spring Security ================================================
    @Builder
    public User(
            String email, String passwordHash,
            String firstName, String lastName,
            Gender gender, String phoneNumber,
            UserType userType){
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.userType = userType != null ? userType : UserType.USER;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.userType.name()));
    }

    @Override
    public String getPassword(){
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    @Override
    public boolean isAccountNonLocked(){
        return true;
    }

    @Override
    public boolean isEnabled(){
        return deletedAt == null;
    } // deletedAt == null일 때만 true

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호가 만료되지 않았음을 나타냅니다.
    }

    
}
