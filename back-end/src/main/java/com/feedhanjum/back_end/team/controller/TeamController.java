package com.feedhanjum.back_end.team.controller;

import com.feedhanjum.back_end.auth.infra.Login;
import com.feedhanjum.back_end.member.controller.dto.MemberDto;
import com.feedhanjum.back_end.member.service.MemberService;
import com.feedhanjum.back_end.team.controller.dto.*;
import com.feedhanjum.back_end.team.domain.Team;
import com.feedhanjum.back_end.team.domain.TeamJoinToken;
import com.feedhanjum.back_end.team.service.TeamService;
import com.feedhanjum.back_end.team.service.dto.TeamCreateDto;
import com.feedhanjum.back_end.team.service.dto.TeamUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;
    private final MemberService memberService;

    @Operation(summary = "팀 생성", description = "로그인한 사용자를 팀장으로 하는 팀을 생성한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "팀 생성에 성공함."),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원이 팀 생성을 시도할 경우", content = @Content)
    })
    @PostMapping("/create")
    public ResponseEntity<TeamResponse> createTeam(@Login Long memberId,
                                                   @Valid @RequestBody TeamCreateRequest request) {
        Team team = teamService.createTeam(memberId,
                new TeamCreateDto(request));

        return new ResponseEntity<>(new TeamResponse(team), HttpStatus.CREATED);
    }

    @Operation(summary = "내가 속한 팀 조회", description = "현재 로그인한 사용자가 가입한 모든 팀을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회에 성공한 경우")
    })
    @GetMapping("/my-teams")
    public ResponseEntity<List<TeamResponse>> getMyTeams(@Login Long memberId) {
        List<TeamResponse> teams = teamService.getMyTeams(memberId)
                .stream()
                .map(TeamResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(teams);
    }

    /**
     * 팀 스페이스 관리 화면에서 쓰일 거 같아서 만들긴 했는데, 확실치 않아 주석 달아둡니다.
     *
     * @return 팀 정보 + 속한 회원 정보를 반환
     */
    @Operation(summary = "팀 상세 정보 조회", description = "특정 팀의 상세 정보를 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "해당 팀의 정보 조회에 성공한 경우"),
            @ApiResponse(responseCode = "404", description = "해당 팀을 조회할 권한이 없는 경우 - 팀 정보 숨김", content = @Content)
    })
    @GetMapping("/{teamId}")
    public ResponseEntity<TeamDetailResponse> getTeamDetail(@Login Long memberId, @PathVariable Long teamId) {
        Team team = teamService.getTeam(teamId);
        List<MemberDto> membersByTeam = memberService.getMembersByTeam(memberId, teamId)
                .stream().map(MemberDto::new).toList();
        TeamDetailResponse teamDetailResponse = new TeamDetailResponse();
        teamDetailResponse.setTeamResponse(new TeamResponse(team));
        teamDetailResponse.setMembers(membersByTeam);
        return ResponseEntity.ok(teamDetailResponse);
    }

    @Operation(summary = "팀원 조회", description = "특정 팀에 존재하는 모든 사용자의 정보를 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공."),
            @ApiResponse(responseCode = "404", description = "해당 팀에 대한 조회 권한이 없을 경우 - 정보 숨김", content = @Content)
    })
    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<MemberDto>> getTeamMembers(@Login Long memberId, @PathVariable Long teamId) {
        List<MemberDto> membersByTeam = memberService.getMembersByTeam(memberId, teamId)
                .stream().map(MemberDto::new).toList();
        return ResponseEntity.ok(membersByTeam);
    }

    @Operation(summary = "팀원 추방", description = "특정 팀에 존재하는 특정 팀원을 제거한다. 팀장만이 사용 가능하다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "해당 팀원을 팀에서 제거하는데 성공"),
            @ApiResponse(responseCode = "404", description = "해당 팀 또는 팀원을 찾을 수 없는 경우"),
            @ApiResponse(responseCode = "403", description = "로그인한 사용자가 팀장이 아닌 경우")
    })
    @DeleteMapping("/{teamId}/member/{removeMemberId}")
    public ResponseEntity<Void> deleteMemberFromTeam(@Login Long memberId, @PathVariable Long teamId, @PathVariable Long removeMemberId) {
        teamService.removeTeamMember(memberId, teamId, removeMemberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "팀 정보 수정", description = "특정 팀의 정보를 수정한다. 탐장만이 사용 가능하다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀 정보를 수정하는데 성공"),
            @ApiResponse(responseCode = "404", description = "해당 팀 또는 팀원을 찾을 수 없는 경우", content = @Content),
            @ApiResponse(responseCode = "403", description = "로그인한 사용자가 팀장이 아닌 경우", content = @Content),
            @ApiResponse(responseCode = "400", description = "요청한 입력값이 잘못되어있을 경우", content = @Content)
    })
    @PostMapping("/{teamId}")
    public ResponseEntity<TeamResponse> updateTeamInfo(@Login Long memberId, @PathVariable Long teamId, @Valid @RequestBody TeamUpdateRequest request) {
        Team team = teamService.updateTeamInfo(memberId, teamId, new TeamUpdateDto(request));
        TeamResponse teamResponse = new TeamResponse(team);
        return ResponseEntity.ok(teamResponse);
    }

    @Operation(summary = "팀장 위임", description = "특정 팀원에게 팀장 권한을 위임한다. 팀장만이 사용 가능하다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "해당 팀원에게 팀장 권한 위임 성공"),
            @ApiResponse(responseCode = "404", description = "해당 팀 또는 팀원을 찾을 수 없는 경우"),
            @ApiResponse(responseCode = "403", description = "로그인한 사용자가 팀장이 아닌 경우")
    })
    @PostMapping("/{teamId}/leader")
    public ResponseEntity<Void> delegateTeamLeader(@Login Long memberId,
                                                   @PathVariable Long teamId,
                                                   @RequestParam Long newLeaderId) {
        teamService.delegateTeamLeader(memberId, teamId, newLeaderId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "팀 탈퇴", description = "로그인한 사용자를 해당 팀에서 탈퇴시킨다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "해당 팀에서 탈퇴하는데 성공한 경우"),
            @ApiResponse(responseCode = "404", description = "해당 팀 또는 팀원을 찾을 수 없는 경우 - 팀 정보 숨김"),
            @ApiResponse(responseCode = "400", description = "로그인한 사용자가 2명 이상 존재하는 팀의 팀장인 경우")
    })
    @DeleteMapping("/{teamId}/leave")
    public ResponseEntity<Void> leaveTeam(@Login Long memberId, @PathVariable Long teamId) {
        teamService.leaveTeam(memberId, teamId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "팀 가입 토큰 발급", description = "특정 팀에 가입할 수 있는 토큰을 발급한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 발급에 성공한 경우"),
            @ApiResponse(responseCode = "404", description = "해당 팀 또는 팀원을 찾을 수 없는 경우", content = @Content),
    })
    @PostMapping("/{teamId}/join-token")
    public ResponseEntity<TeamJoinTokenResponse> getJoinToken(@Login Long memberId, @PathVariable Long teamId) {
        TeamJoinToken joinToken = teamService.createJoinToken(memberId, teamId);
        return ResponseEntity.ok(TeamJoinTokenResponse.from(joinToken));
    }
}
