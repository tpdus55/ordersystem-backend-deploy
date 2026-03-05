package com.example.ordersystem.common.time;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public class BaseTime {
    @CreationTimestamp
    private LocalDateTime createdTime;
}
