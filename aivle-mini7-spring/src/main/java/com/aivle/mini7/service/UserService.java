package com.aivle.mini7.service;

import com.aivle.mini7.dto.UserDto;
import com.aivle.mini7.model.User;
import com.aivle.mini7.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User createUser(UserDto userDto) {
        if (userRepository.existsById(userDto.getId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        User user = User.builder()
                .id(userDto.getId())
                .pw(userDto.getPw())
                .name(userDto.getName())
                .phone(userDto.getPhone())
                .idType(0) // 기본 사용자 유형 설정
                .build();

        return userRepository.save(user);
    }

    public User login(String id, String password) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!password.equals(user.getPw())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    public boolean isIdAvailable(String id) {
        String normalizedId = id.trim().toLowerCase(); // 소문자로 변환
        System.out.println("Normalized ID: " + normalizedId);
        boolean exists = userRepository.existsById(normalizedId);
        System.out.println("Exists in DB: " + exists);
        return !exists;
    }

    public User getUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id));
    }

    @Transactional
    public User updateUser(UserDto userDto) {
        User user = getUser(userDto.getId());
        user.setName(userDto.getName());
        user.setPhone(userDto.getPhone());
        if (userDto.getPw() != null && !userDto.getPw().isEmpty()) {
            user.setPw(userDto.getPw());
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String id) {
        User user = getUser(id);
        userRepository.delete(user);
    }

    // 여기에 recoverPassword 메서드 추가
    public String recoverPassword(String id, String name, String phone) {
        // 사용자 조회
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("아이디가 존재하지 않습니다."));

        // 이름 검증
        if (!user.getName().equals(name)) {
            throw new IllegalArgumentException("이름이 일치하지 않습니다.");
        }

        // 전화번호 검증
        if (!user.getPhone().equals(phone)) {
            throw new IllegalArgumentException("전화번호가 일치하지 않습니다.");
        }

        // 비밀번호 반환
        return user.getPw();
    }
}
