package com.feedhanjum.back_end.member.controller;

import com.feedhanjum.back_end.auth.infra.Login;
import com.feedhanjum.back_end.member.controller.dto.MemberDto;
import com.feedhanjum.back_end.member.controller.dto.ProfileChangeRequest;
import com.feedhanjum.back_end.member.domain.ProfileImage;
import com.feedhanjum.back_end.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "특정 회원 정보 조회", description = "특정 회원의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 정보 조회에 성공했을 경우, 해당 회원 정보를 반환합니다."),
            @ApiResponse(responseCode = "404", description = "해당 회원이 존재하지 않을 경우", content = @Content)
    })
    @GetMapping("/member/{id}")
    public ResponseEntity<MemberDto> getMemberById(@PathVariable Long id) {
        MemberDto memberDto = new MemberDto(memberService.getMemberById(id));
        return new ResponseEntity<>(memberDto, HttpStatus.OK);
    }

    @Operation(summary = "회원의 정보를 변경한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경된 회원 정보를 반환한다. " +
                    "세션을 통해 받은 회원의 정보를 수정하기 때문에, 다른 회원의 정보를 수정할 수 없다."),
            @ApiResponse(responseCode = "400", description = "변경할 회원 정보 폼이 잘못됐을 경우, 주로 이름이 제한 범위 밖인 경우", content = @Content)
    })
    @PostMapping("/member")
    public ResponseEntity<MemberDto> changeProfile(@Login Long memberId, @Valid @RequestBody ProfileChangeRequest profileChageRequest) {
        String name = profileChageRequest.name();
        ProfileImage profileImage = profileChageRequest.profileImage();
        MemberDto memberDto = new MemberDto(memberService.changeProfile(memberId, name, profileImage));
        return new ResponseEntity<>(memberDto, HttpStatus.OK);
    }
}
