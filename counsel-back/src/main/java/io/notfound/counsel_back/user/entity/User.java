package io.notfound.counsel_back.user.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class User {

	    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(nullable = false, unique = true)
	    private String email;

	    @Column
	    private String password;

	    private LocalDate suspensionDate;

	    private String suspensionReason;

	    @Column(nullable = false)
	    private LocalDate lastLoginDate;
	    
	    private boolean isSuspension;

	    @Temporal(TemporalType.DATE)
	    @CreationTimestamp
	    @JsonFormat(shape = JsonFormat.Shape.STRING , pattern = "yyyy/MM/dd" , timezone = "Asia/Seoul")
	    @Column(nullable = false)
	    private LocalDate joinDate;
	    
	    private String profileImage;
	    
	    private String userKey;


	    public void updateLoginDate() {
	        lastLoginDate = LocalDate.now();
	    }

	    public void updatePassword(String password) {
	        this.password = password;
	    }

	    public boolean checkPassword(String plainPassword, PasswordEncoder passwordEncoder) {
	        return passwordEncoder.matches(plainPassword, this.password);
	    }

	    public void addSuspensionDate(int date, String reason) {
	        
	    }

	    public void minusSuspensionDate(int date) {
	        if(LocalDate.now().compareTo(suspensionDate.minusDays(date)) > 0) {
	            isSuspension = false;
	            suspensionDate = LocalDate.now();
	        }
	        suspensionDate = suspensionDate.minusDays(date);
	    }

	    // 여기 부터 UserDetails 구현 메서드
//	    @Override
//	    public Collection<? extends GrantedAuthority> getAuthorities() {
//	        return Collections.singletonList(new SimpleGrantedAuthority(role.getRole()));
//	    }
//	    @Override
//	    public String getUsername() { return email; }
//	    @Override
//	    public String getPassword() { return password; }
//	    @Override
//	    public boolean isAccountNonExpired() { return true; }
//	    @Override
//	    public boolean isAccountNonLocked() { return true; }
//	    @Override
//	    public boolean isCredentialsNonExpired() { return true; }
//	    @Override
//	    public boolean isEnabled() { return true; }
}
