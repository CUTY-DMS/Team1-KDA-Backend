package com.example.cmd.domain.repository;

import com.example.cmd.domain.entity.Noti;
import com.example.cmd.domain.entity.Notification;
import com.example.cmd.domain.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByMonthAndYear(int month, int year);



}
