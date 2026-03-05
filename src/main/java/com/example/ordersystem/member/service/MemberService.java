package com.example.ordersystem.member.service;

import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.dtos.MemberCreateDto;
import com.example.ordersystem.member.dtos.MemberDetailDto;
import com.example.ordersystem.member.dtos.MemberListDto;
import com.example.ordersystem.member.dtos.MemberLoginDto;
import com.example.ordersystem.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Member save(MemberCreateDto dto){
        if(memberRepository.findByEmail(dto.getEmail()).isPresent()){
            throw new IllegalArgumentException("이메일이 중복입니다.");
        }
        System.out.println(passwordEncoder.encode(dto.getPassword()));
        Member member = dto.toEntity(passwordEncoder.encode(dto.getPassword()));
        return memberRepository.save(member);

    }
    public Member login(MemberLoginDto dto){
        Optional<Member> optMember = memberRepository.findByEmail(dto.getEmail());
        boolean check = true;
        if(!optMember.isPresent()){
            check = false;
        }else{
            if(!passwordEncoder.matches(dto.getPassword(), optMember.get().getPassword())){
                check = false;
            }
        }
        if(!check){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return optMember.get();
    }

    @Transactional(readOnly = true)
    public List<MemberListDto> findAll(){
        List<Member> memberList = memberRepository.findAll();
        List<MemberListDto> memberListDto = new ArrayList<>();
        for(Member m : memberList){
            MemberListDto dto = MemberListDto.fromEntity(m);
            memberListDto.add(dto);
        }
        return memberListDto;
    }

    @Transactional(readOnly = true)
    public MemberDetailDto findById(Long id){
        Optional<Member> optMember = memberRepository.findById(id);
        Member member = optMember.orElseThrow(()-> new EntityNotFoundException("없는 아이디 입니다."));
        MemberDetailDto dto = MemberDetailDto.fromEntity(member);
        return dto;
    }

    @Transactional(readOnly = true)
    public MemberDetailDto myInfo(){
       String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
       Member member = memberRepository.findByEmail(email)
               .orElseThrow(()-> new EntityNotFoundException("없는 이메일 입니다."));
       MemberDetailDto dto = MemberDetailDto.fromEntity(member);
       return dto;
    }

}
