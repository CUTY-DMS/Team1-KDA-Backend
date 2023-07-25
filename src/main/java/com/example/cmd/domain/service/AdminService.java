package com.example.cmd.domain.service;

import com.example.cmd.domain.controller.dto.request.*;
import com.example.cmd.domain.controller.dto.response.UserListResponse;
import com.example.cmd.domain.entity.Notification;
import com.example.cmd.domain.entity.Admin;
import com.example.cmd.domain.entity.Role;
import com.example.cmd.domain.entity.User;
import com.example.cmd.domain.repository.NotificationRepository;
import com.example.cmd.domain.repository.AdminRepository;
import com.example.cmd.domain.repository.UserRepository;
import com.example.cmd.domain.service.exception.admin.AdminNotFoundException;
import com.example.cmd.domain.service.exception.admin.CodeMismatchException;
import com.example.cmd.domain.service.exception.admin.PasswordMismatch;
import com.example.cmd.domain.service.exception.notification.NotificationNotFoundException;
import com.example.cmd.domain.service.exception.user.EmailAlreadyExistException;
import com.example.cmd.domain.service.exception.user.UserNotFoundException;
import com.example.cmd.domain.service.facade.AdminFacade;
import com.example.cmd.domain.controller.dto.response.TokenResponse;
import com.example.cmd.global.security.jwt.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final NotificationRepository notificationRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AdminFacade adminFacade;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    //private JavaMail javaMailService;
    @Transactional
    public void write(NotificationWriteRequest notificationWriteRequest) {
        Admin currentAdmin = adminFacade.getCurrentAdmin();

        Optional<Admin> userOptional = adminRepository.findByEmail(currentAdmin.getEmail());
        if (userOptional.isPresent()) {
            Admin admin = userOptional.get();
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss");
            String formattedDateTime = now.format(formatter);
            if (notificationRepository.findByAdminAndDateTime(admin, formattedDateTime).isPresent()) {
                System.out.println("fast");
                throw new InputMismatchException("Too fast click");
            }
            notificationRepository.save(
                    Notification.builder()
                            .title(notificationWriteRequest.getTitle())
                            .contents(notificationWriteRequest.getContents())
                            .admin(admin)
                            .classes(notificationWriteRequest.getClasses())
                            .grade(notificationWriteRequest.getGrade())
                            .dateTime(formattedDateTime)
                            .noti(notificationWriteRequest.getNoti())
                            .build()
            );
        } else {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다.");
        }
    }

    @Transactional
    public void delete(NotificationDeleteRequest notificationDeleteRequest) {
        Admin currentAdmin = adminFacade.getCurrentAdmin();
        Optional<Notification> optionalNotification = notificationRepository.findByAdminAndDateTime(currentAdmin, notificationDeleteRequest.getDateTime());
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            notificationRepository.deleteById(notification.getId());

        } else {
            throw NotificationNotFoundException.EXCEPTION;
        }
    }

    @Transactional
    public void fix(NotificationFixRequest notificationFixRequest) {
        Admin currentAdmin = adminFacade.getCurrentAdmin();
        Optional<Notification> optionalNotification = notificationRepository.findByAdminAndDateTime(currentAdmin, notificationFixRequest.getDateTime());
        if (optionalNotification.isPresent()) {
            Notification updatedNotification = Notification.builder()
                    .id(optionalNotification.get().getId())
                    .title(notificationFixRequest.getTitle())
                    .contents(notificationFixRequest.getContents())
                    .admin(optionalNotification.get().getAdmin())
                    .dateTime(optionalNotification.get().getDateTime())
                    .build();

            notificationRepository.save(updatedNotification);
            // 저장

        }
    }

    @Transactional
    public void adminSignup(AdminSignupRequest adminSignupRequest) {
        System.out.println("signupRequest = " + adminSignupRequest);
        if (adminRepository.existsByEmail(adminSignupRequest.getEmail())) {
            System.out.println("중복");
            throw EmailAlreadyExistException.EXCEPTION;
        }
        if (!Objects.equals(adminSignupRequest.getCode(), "abcd1234")) {
            throw CodeMismatchException.EXCEPTION;
        }
        System.out.println("signupRequest.getUsername() = " + adminSignupRequest.getName());
        adminRepository.save(
                Admin.builder()
                        .email(adminSignupRequest.getEmail())
                        .name(adminSignupRequest.getName())
                        .birth(adminSignupRequest.getBirth())
                        .teachClass(adminSignupRequest.getTeachClass())
                        .teachGrade(adminSignupRequest.getTeachGrade())
                        .password(adminSignupRequest.getPassword())
                        .role(Role.ROLE_ADMIN)
                        .build()
        );

    }

    @Transactional
    public TokenResponse adminLogin(LoginRequest loginRequest) {
        Optional<Admin> admin = adminRepository.findByEmail(loginRequest.getEmail());
        if (admin.isPresent()
                && isPasswordMatching(loginRequest.getPassword(), admin.get().getPassword())) {
            TokenResponse token = jwtTokenProvider.createAccessToken(admin.get().getEmail(), admin.get().getRole());
            System.out.println("user.get().getEmail() = " + admin.get().getEmail());
            System.out.println("login success");
            return token;
        } else {
            throw AdminNotFoundException.EXCEPTION;
        }
    }

    private boolean isPasswordMatching(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public List<UserListResponse> getStudentList(StudentListRequest studentListRequest) {
        Long grade = studentListRequest.getGradeClass() / 10;
        Long classes = studentListRequest.getGradeClass() % 10;
        List<UserListResponse> users = userRepository.findAllByGradeAndClasses(grade, classes);
        if (users.isEmpty()) {
            throw UserNotFoundException.EXCEPTION;
        }

        return users;
    }


    @Transactional
    public void adminInfoChange(AdminInfoChangeRequest adminInfoChangeRequest) {
        Admin currentAdmin = adminFacade.getCurrentAdmin();
/*
        if (isPasswordMatching(adminInfoChangeRequest.getPassword(), currentAdmin.getPassword())) {
            throw PasswordMismatch.EXCEPTION;
        }*/

        Optional<Admin> adminList = adminRepository.findByEmail(currentAdmin.getEmail());
        if (adminList.isEmpty()) {
            throw AdminNotFoundException.EXCEPTION;
        }

        Admin admin = adminList.get();

        String name = adminInfoChangeRequest.getName();
        Long birth = adminInfoChangeRequest.getBirth();
        Long teachClass = adminInfoChangeRequest.getTeachClass();
        Long teachGrade = adminInfoChangeRequest.getTeachGrade();


        admin.modifyAdminInfo(name, birth, teachClass, teachGrade);
        adminRepository.save(admin);
    }
@Transactional
    public void passwordChange(PasswordChangeRequest passwordChangeRequest) {

        Admin currentAdmin = adminFacade.getCurrentAdmin();
        if (isPasswordMatching(passwordChangeRequest.getOldPassword(), currentAdmin.getPassword())) {
            throw PasswordMismatch.EXCEPTION;
        }
        if (!Objects.equals(passwordChangeRequest.getNewPassword(), passwordChangeRequest.getReNewPassword())) {
            throw PasswordMismatch.EXCEPTION;
        }
        adminRepository.save(
                Admin.builder()
                        .password(passwordChangeRequest.getNewPassword())
                        .build()
        );
    }

    public Admin adminInfo() {//나중에 어드민인포에 뭐 필요한지 보고 그거만 보내도록 수정할듯?지금은 뭐만 보내는지 몰라서
        return adminFacade.getCurrentAdmin();
    }

    public void findPassword(String email){
        Admin currentAdmin = adminFacade.getCurrentAdmin();
    Optional<Admin> admin = adminRepository.findByEmail(email);

    }
}