package com.example.ordersystem.member.controller;

import com.example.ordersystem.common.auth.JwtTokenProvider;
import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.dtos.*;
import com.example.ordersystem.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    @Autowired
    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

//    회원가입
    @PostMapping("/create")
    @Operation(
            summary = "회원가입", description = "이메일, 비밀번호를 통한 회원가입"
    )
    public ResponseEntity<?> save(@RequestBody MemberCreateDto dto){
        Member member = memberService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(member.getId());
    }


//    내 정보조회
    @GetMapping("/myinfo")
    public ResponseEntity<?> myInfo(){
        MemberDetailDto dto = memberService.myInfo();
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    //    회원목록조회
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MemberListDto> findAll(){
        return memberService.findAll();
    }

//    회원상세조회
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findById(@PathVariable Long id){
        MemberDetailDto dto = memberService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }


    //    로그인
    @PostMapping("/doLogin")
    public ResponseEntity<?> Login(@RequestBody MemberLoginDto dto){
        Member member = memberService.login(dto);
        String accessToken = jwtTokenProvider.createToken(member);
//        refresh 토큰 생성 및 저장
        String refreshToken = jwtTokenProvider.createRtToken(member);
        MemberLoginResDto memberLoginResDto = MemberLoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(memberLoginResDto);
    }

    @PostMapping("/refresh-at")
    public ResponseEntity<?> refreshAt(@RequestBody RefreshTokenDto dto){
//        rt검증(1. 토큰 자체검증(claims 꺼냄) 2. redis 조회검증)
        Member member = jwtTokenProvider.validateRt(dto.getRefreshToken());

//        at신규생성
        String accessToken = jwtTokenProvider.createToken(member);
//        refresh 토큰 생성 및 저장
        MemberLoginResDto memberLoginResDto = MemberLoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(null)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(memberLoginResDto);
    }
}
