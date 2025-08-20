package io.notfound.counsel_back.user.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
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

	    @Column(nullable = false)
	    private LocalDate lastLoginDate;

	    @Temporal(TemporalType.DATE)
	    @CreationTimestamp
	    @JsonFormat(shape = JsonFormat.Shape.STRING , pattern = "yyyy/MM/dd" , timezone = "Asia/Seoul")
	    @Column(nullable = false)
	    private LocalDate joinDate;

	    public void updateLoginDate() {
	        lastLoginDate = LocalDate.now();
	    }

	    public void updatePassword(String password) {
	        this.password = password;
	    }

	    public boolean checkPassword(String plainPassword, PasswordEncoder passwordEncoder) {
	        return passwordEncoder.matches(plainPassword, this.password);
	    }
}
