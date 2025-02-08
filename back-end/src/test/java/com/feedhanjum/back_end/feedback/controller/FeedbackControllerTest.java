package com.feedhanjum.back_end.feedback.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedhanjum.back_end.auth.infra.SessionConst;
import com.feedhanjum.back_end.core.dto.Paged;
import com.feedhanjum.back_end.feedback.controller.dto.request.FrequentFeedbackRequestForApiRequest;
import com.feedhanjum.back_end.feedback.controller.dto.request.FrequentFeedbackSendRequest;
import com.feedhanjum.back_end.feedback.controller.dto.request.RegularFeedbackSendRequest;
import com.feedhanjum.back_end.feedback.controller.dto.response.FrequentFeedbackRequestForApiResponse;
import com.feedhanjum.back_end.feedback.controller.dto.response.RegularFeedbackRequestForApiResponse;
import com.feedhanjum.back_end.feedback.domain.Feedback;
import com.feedhanjum.back_end.feedback.domain.FeedbackFeeling;
import com.feedhanjum.back_end.feedback.domain.FeedbackType;
import com.feedhanjum.back_end.feedback.repository.FeedbackRepository;
import com.feedhanjum.back_end.feedback.service.dto.ReceivedFeedbackDto;
import com.feedhanjum.back_end.feedback.service.dto.SentFeedbackDto;
import com.feedhanjum.back_end.member.domain.Member;
import com.feedhanjum.back_end.member.domain.ProfileImage;
import com.feedhanjum.back_end.member.repository.MemberRepository;
import com.feedhanjum.back_end.schedule.domain.RegularFeedbackRequest;
import com.feedhanjum.back_end.schedule.domain.Schedule;
import com.feedhanjum.back_end.schedule.domain.ScheduleMember;
import com.feedhanjum.back_end.schedule.repository.RegularFeedbackRequestRepository;
import com.feedhanjum.back_end.schedule.repository.ScheduleMemberRepository;
import com.feedhanjum.back_end.schedule.repository.ScheduleRepository;
import com.feedhanjum.back_end.team.domain.Team;
import com.feedhanjum.back_end.team.repository.FrequentFeedbackRequestRepository;
import com.feedhanjum.back_end.team.repository.TeamMemberRepository;
import com.feedhanjum.back_end.team.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
@ActiveProfiles("test")
class FeedbackControllerTest {

    @Autowired
    private MockMvcTester mvc;
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private ScheduleMemberRepository scheduleMemberRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private RegularFeedbackRequestRepository regularFeedbackRequestRepository;
    @Autowired
    private FrequentFeedbackRequestRepository frequentFeedbackRequestRepository;
    private final Clock clock = Clock.fixed(Instant.parse("2025-01-10T12:00:00Z"), ZoneId.systemDefault());

    private Member createMember(String name) {
        return new Member(name, name + "@test.com", new ProfileImage("bg-" + name, "profile-" + name));
    }

    private Team createTeam(String name, Member leader) {
        return new Team(name, leader, LocalDate.now(clock).minusDays(1), LocalDate.now(clock).plusDays(1), FeedbackType.ANONYMOUS);
    }

    private Schedule createSchedule(String name, Team team, Member leader, boolean isEnd) {
        LocalDateTime start, end;
        if (isEnd) {
            start = LocalDateTime.now(clock).minusHours(1);
            end = LocalDateTime.now(clock).minusMinutes(10);
        } else {
            start = LocalDateTime.now(clock);
            end = LocalDateTime.now(clock).plusHours(1);
        }
        return new Schedule(name, start, end, team, leader);
    }

    private Feedback createFeedback(Member sender, Member receiver, Team team) {
        return createFeedback(sender, receiver, team, false, false);
    }

    private Feedback createFeedback(Member sender, Member receiver, Team team, boolean isAnonymous, boolean isLiked) {
        Feedback feedback = Feedback.builder()
                .sender(sender)
                .receiver(receiver)
                .team(team)
                .feedbackType(isAnonymous ? FeedbackType.ANONYMOUS : FeedbackType.IDENTIFIED)
                .feedbackFeeling(FeedbackFeeling.POSITIVE)
                .objectiveFeedbacks(FeedbackFeeling.POSITIVE.getObjectiveFeedbacks().subList(0, 2))
                .subjectiveFeedback("좋아요")
                .build();
        if (isLiked)
            feedback.like(receiver);
        return feedback;
    }

    private Member member1;
    private Member member2;
    private Member member3;
    private Team team1;
    private Team team2;
    private Schedule schedule1;
    private ScheduleMember scheduleMember1;
    private ScheduleMember scheduleMember2;
    private ScheduleMember scheduleMember3;

    @BeforeEach
    void setUp() {
        member1 = createMember("member1");
        member2 = createMember("member2");
        member3 = createMember("member3");
        memberRepository.saveAll(List.of(member1, member2, member3));

        team1 = createTeam("team1", member1);
        team1.join(member2);
        team1.join(member3);
        team2 = createTeam("team2", member2);
        team2.join(member1);
        team2.join(member3);
        teamRepository.saveAll(List.of(team1, team2));

        schedule1 = createSchedule("schedule1", team1, member1, false);
        scheduleRepository.save(schedule1);

        scheduleMember1 = new ScheduleMember(schedule1, member1);
        scheduleMember2 = new ScheduleMember(schedule1, member2);
        scheduleMember3 = new ScheduleMember(schedule1, member3);
        scheduleMemberRepository.saveAll(List.of(scheduleMember1, scheduleMember2, scheduleMember3));

    }


    MockHttpSession withLoginUser(Member member) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.MEMBER_ID, member.getId());
        return session;
    }

    @Nested
    @DisplayName("수시 피드백 전송 테스트")
    class SendFrequentFeedback {

        @Test
        @DisplayName("성공 시 204")
        void test1() throws Exception {
            // given
            Member sender = member1;
            Member receiver = member2;
            Team team = team1;
            FrequentFeedbackSendRequest request = new FrequentFeedbackSendRequest(
                    receiver.getId(),
                    team.getId(),
                    FeedbackFeeling.CONSTRUCTIVE,
                    FeedbackFeeling.CONSTRUCTIVE.getObjectiveFeedbacks().subList(1, 3),
                    "테스트 내용",
                    true
            );

            // when
            assertThat(mvc.post()
                    .uri("/api/feedbacks/frequent")
                    .contentType(MediaType.APPLICATION_JSON)
                    .session(withLoginUser(sender))
                    .content(mapper.writeValueAsString(request))
            ).hasStatus(HttpStatus.NO_CONTENT);


            List<Feedback> feedbacks = feedbackRepository.findAll();
            assertThat(feedbacks).hasSize(1);
            Feedback feedback = feedbacks.get(0);
            assertThat(feedback.getSender()).isEqualTo(sender);
            assertThat(feedback.getReceiver()).isEqualTo(receiver);
            assertThat(feedback.getTeam()).isEqualTo(team);
            assertThat(feedback.getFeedbackType()).isEqualTo(FeedbackType.ANONYMOUS);
            assertThat(feedback.getFeedbackFeeling()).isEqualTo(FeedbackFeeling.CONSTRUCTIVE);
            assertThat(feedback.getObjectiveFeedbacks()).containsExactlyInAnyOrderElementsOf(FeedbackFeeling.CONSTRUCTIVE.getObjectiveFeedbacks().subList(1, 3));
            assertThat(feedback.getSubjectiveFeedback()).isEqualTo("테스트 내용");
            assertThat(feedback.isLiked()).isFalse();
        }

        @Test
        @DisplayName("요청 데이터가 잘못된 경우 400")
        void test2() throws Exception {
            // given
            Member sender = member1;
            Member receiver = member2;
            Team team = team1;
            FrequentFeedbackSendRequest request = new FrequentFeedbackSendRequest(
                    receiver.getId(),
                    team.getId(),
                    FeedbackFeeling.CONSTRUCTIVE,
                    FeedbackFeeling.CONSTRUCTIVE.getObjectiveFeedbacks().subList(0, 6),
                    "테스트 내용",
                    true
            );

            // when
            assertThat(mvc.post()
                    .uri("/api/feedbacks/frequent")
                    .contentType(MediaType.APPLICATION_JSON)
                    .session(withLoginUser(sender))
                    .content(mapper.writeValueAsString(request))
            ).hasStatus(HttpStatus.BAD_REQUEST);

            List<Feedback> feedbacks = feedbackRepository.findAll();
            assertThat(feedbacks).isEmpty();
        }
    }


    @Nested
    @DisplayName("정기 피드백 전송 테스트")
    class SendRegularFeedback {


        @Test
        @DisplayName("성공 시 204")
        void test1() throws Exception {
            // given
            Member sender = member1;
            Member receiver = member2;
            Team team = team1;
            Schedule schedule = schedule1;
            regularFeedbackRequestRepository.save(
                    new RegularFeedbackRequest(LocalDateTime.now(), scheduleMember1, receiver
                    ));
            RegularFeedbackSendRequest request = new RegularFeedbackSendRequest(
                    receiver.getId(),
                    schedule.getId(),
                    FeedbackFeeling.CONSTRUCTIVE,
                    FeedbackFeeling.CONSTRUCTIVE.getObjectiveFeedbacks().subList(1, 3),
                    "테스트 내용",
                    true
            );

            // when
            assertThat(mvc.post()
                    .uri("/api/feedbacks/regular")
                    .contentType(MediaType.APPLICATION_JSON)
                    .session(withLoginUser(sender))
                    .content(mapper.writeValueAsString(request))
            ).hasStatus(HttpStatus.NO_CONTENT);


            List<Feedback> feedbacks = feedbackRepository.findAll();
            assertThat(feedbacks).hasSize(1);
            Feedback feedback = feedbacks.get(0);
            assertThat(feedback.getSender()).isEqualTo(sender);
            assertThat(feedback.getReceiver()).isEqualTo(receiver);
            assertThat(feedback.getTeam()).isEqualTo(team);
            assertThat(feedback.getFeedbackType()).isEqualTo(FeedbackType.ANONYMOUS);
            assertThat(feedback.getFeedbackFeeling()).isEqualTo(FeedbackFeeling.CONSTRUCTIVE);
            assertThat(feedback.getObjectiveFeedbacks()).containsExactlyInAnyOrderElementsOf(FeedbackFeeling.CONSTRUCTIVE.getObjectiveFeedbacks().subList(1, 3));
            assertThat(feedback.getSubjectiveFeedback()).isEqualTo("테스트 내용");
            assertThat(feedback.isLiked()).isFalse();
        }

        @Test
        @DisplayName("요청 데이터가 잘못된 경우 400")
        void test2() throws Exception {
            // given
            Member sender = member1;
            Member receiver = member2;
            Team team = team1;
            FrequentFeedbackSendRequest request = new FrequentFeedbackSendRequest(
                    receiver.getId(),
                    team.getId(),
                    FeedbackFeeling.CONSTRUCTIVE,
                    FeedbackFeeling.CONSTRUCTIVE.getObjectiveFeedbacks().subList(0, 6),
                    "테스트 내용",
                    true
            );

            // when
            assertThat(mvc.post()
                    .uri("/api/feedbacks/regular")
                    .contentType(MediaType.APPLICATION_JSON)
                    .session(withLoginUser(sender))
                    .content(mapper.writeValueAsString(request))
            ).hasStatus(HttpStatus.BAD_REQUEST);

            List<Feedback> feedbacks = feedbackRepository.findAll();
            assertThat(feedbacks).isEmpty();
        }

        @Test
        @DisplayName("수시 피드백 요청이 없을 시 400")
        void test3() throws Exception {
            // given
            Member sender = member1;
            Member receiver = member2;
            Schedule schedule = schedule1;
            RegularFeedbackSendRequest body = new RegularFeedbackSendRequest(
                    receiver.getId(),
                    schedule.getId(),
                    FeedbackFeeling.CONSTRUCTIVE,
                    FeedbackFeeling.CONSTRUCTIVE.getObjectiveFeedbacks().subList(1, 3),
                    "테스트 내용",
                    true
            );

            // when
            assertThat(mvc.post()
                    .uri("/api/feedbacks/regular")
                    .contentType(MediaType.APPLICATION_JSON)
                    .session(withLoginUser(sender))
                    .content(mapper.writeValueAsString(body))
            ).hasStatus(HttpStatus.BAD_REQUEST);


            List<Feedback> feedbacks = feedbackRepository.findAll();
            assertThat(feedbacks).isEmpty();
        }
    }

    @Nested
    @DisplayName("수시 피드백 요청 테스트")
    class RequestFrequentFeedback {

        @Test
        @DisplayName("성공 시 202")
        void test1() throws Exception {
            // given
            Member sender = member1;
            Member receiver = member2;
            Team team = team1;
            String requestedContent = "내용";

            var body = new FrequentFeedbackRequestForApiRequest(receiver.getId(), team.getId(), requestedContent);

            // when
            assertThat(mvc.post()
                    .uri("/api/feedbacks/frequent/request")
                    .session(withLoginUser(sender))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(body))
            ).hasStatus(HttpStatus.ACCEPTED);

            var requests = frequentFeedbackRequestRepository.findAll();

            assertThat(requests).hasSize(1);
            var request = requests.get(0);
            assertThat(request.getSender()).isEqualTo(sender);
            assertThat(request.getReceiver()).isEqualTo(receiver);
            assertThat(request.getTeam()).isEqualTo(team);
            assertThat(request.getRequestedContent()).isEqualTo(requestedContent);
            assertThat(request.getCreatedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        }

    }

    @Nested
    @DisplayName("수시 피드백 요청 조회 테스트")
    class GetFrequentFeedbackRequest {

        @Test
        @DisplayName("성공 시 200")
        void test1() {
            // given
            Member sender1 = member1;
            Member sender2 = member3;
            Team team = team1;
            Member receiver = member2;
            team.requestFeedback(sender1, receiver, "내용 1");
            team.requestFeedback(sender2, receiver, "내용 2");
            teamRepository.save(team);


            // when
            assertThat(mvc.get()
                    .uri("/api/feedbacks/frequent/request")
                    .queryParam("teamId", team.getId().toString())
                    .session(withLoginUser(receiver))
                    .contentType(MediaType.APPLICATION_JSON)
            ).hasStatus(HttpStatus.OK)
                    .body()
                    .satisfies(result -> {
                        List<FrequentFeedbackRequestForApiResponse> requests = mapper.readValue(result, new TypeReference<>() {
                        });
                        assertThat(requests).hasSize(2);
                        assertThat(requests).extracting(FrequentFeedbackRequestForApiResponse::requestedContent)
                                .containsExactlyInAnyOrder("내용 1", "내용 2");
                        assertThat(requests).extracting(req -> req.requester().email())
                                .containsExactlyInAnyOrder(sender1.getEmail(), sender2.getEmail());
                    });

        }
    }

    @Nested
    @DisplayName("정기 피드백 요청 조회 테스트")
    class GetRegularFeedbackRequest {

        @Test
        @DisplayName("성공 시 200")
        void test1() {
            // given
            Member sender1 = member1;
            Member sender2 = member3;
            ScheduleMember scheduleMember = scheduleMember2;
            Member receiver = scheduleMember.getMember();
            regularFeedbackRequestRepository.save(new RegularFeedbackRequest(LocalDateTime.now(), scheduleMember, sender1));
            regularFeedbackRequestRepository.save(new RegularFeedbackRequest(LocalDateTime.now(), scheduleMember, sender2));

            // when
            assertThat(mvc.get()
                    .uri("/api/feedbacks/regular/request")
                    .queryParam("scheduleId", scheduleMember.getSchedule().getId().toString())
                    .session(withLoginUser(receiver))
                    .contentType(MediaType.APPLICATION_JSON)
            ).hasStatus(HttpStatus.OK)
                    .body()
                    .satisfies(result -> {
                        List<RegularFeedbackRequestForApiResponse> requests = mapper.readValue(result, new TypeReference<List<RegularFeedbackRequestForApiResponse>>() {
                        });
                        assertThat(requests).hasSize(2);
                        assertThat(requests).extracting(req -> req.requester().email())
                                .containsExactlyInAnyOrder(sender1.getEmail(), sender2.getEmail());
                    });

        }
    }

    @Nested
    @DisplayName("피드백 좋아요 테스트")
    class LikeFeedback {

        @Test
        @DisplayName("성공 시 204")
        void test1() {
            // given
            Member sender = member1;
            Member receiver = member2;
            Feedback feedback = createFeedback(sender, receiver, team1);
            feedbackRepository.save(feedback);

            // when
            assertThat(mvc.post()
                    .uri("/api/member/{memberId}/feedbacks/{feedbackId}/liked", receiver.getId(), feedback.getId())
                    .session(withLoginUser(receiver))
            ).hasStatus(HttpStatus.NO_CONTENT);

            var likedFeedback = feedbackRepository.findById(feedback.getId()).orElseThrow();
            assertThat(likedFeedback.isLiked()).isTrue();
        }

        @Test
        @DisplayName("본인이 아닌 경우 403")
        void test2() {
            // given
            Member sender = member1;
            Member receiver = member2;
            Member notReceiver = member3;
            Feedback feedback = createFeedback(sender, receiver, team1);
            feedbackRepository.save(feedback);

            // when
            assertThat(mvc.post()
                    .uri("/api/member/{memberId}/feedbacks/{feedbackId}/liked", receiver.getId(), feedback.getId())
                    .session(withLoginUser(notReceiver))
            ).hasStatus(HttpStatus.FORBIDDEN);

            var likedFeedback = feedbackRepository.findById(feedback.getId()).orElseThrow();
            assertThat(likedFeedback.isLiked()).isFalse();
        }
    }

    @Nested
    @DisplayName("피드백 좋아요 취소 테스트")
    class UnlikeFeedback {

        @Test
        @DisplayName("성공 시 204")
        void test1() {
            // given
            Member sender = member1;
            Member receiver = member2;
            Feedback feedback = createFeedback(sender, receiver, team1, true, true);
            feedbackRepository.save(feedback);

            // when
            assertThat(mvc.delete()
                    .uri("/api/member/{memberId}/feedbacks/{feedbackId}/liked", receiver.getId(), feedback.getId())
                    .session(withLoginUser(receiver))
            ).hasStatus(HttpStatus.NO_CONTENT);

            var likedFeedback = feedbackRepository.findById(feedback.getId()).orElseThrow();
            assertThat(likedFeedback.isLiked()).isFalse();
        }

        @Test
        @DisplayName("본인이 아닌 경우 403")
        void test2() {
            // given
            Member sender = member1;
            Member receiver = member2;
            Member notReceiver = member3;
            Feedback feedback = createFeedback(sender, receiver, team1, false, true);
            feedbackRepository.save(feedback);

            // when
            assertThat(mvc.delete()
                    .uri("/api/member/{memberId}/feedbacks/{feedbackId}/liked", receiver.getId(), feedback.getId())
                    .session(withLoginUser(notReceiver))
            ).hasStatus(HttpStatus.FORBIDDEN);

            var likedFeedback = feedbackRepository.findById(feedback.getId()).orElseThrow();
            assertThat(likedFeedback.isLiked()).isTrue();
        }
    }

    @Nested
    @DisplayName("정기 피드백 건너뛰기 테스트")
    class SkipRegularFeedbackRequest {

        @Test
        @DisplayName("성공 시 204")
        void test1() {
            // given
            Member receiver = member1;
            ScheduleMember scheduleMember = scheduleMember1;
            Member sender = member2;
            Schedule schedule = schedule1;
            regularFeedbackRequestRepository.save(new RegularFeedbackRequest(LocalDateTime.now(), scheduleMember, sender));

            // when
            assertThat(mvc.delete()
                    .uri("/api/feedbacks/regular/request")
                    .queryParam("scheduleId", schedule.getId().toString())
                    .session(withLoginUser(receiver))
            ).hasStatus(HttpStatus.NO_CONTENT);

            var requests = regularFeedbackRequestRepository.findAll();
            assertThat(requests).isEmpty();
        }
    }

    @Nested
    @DisplayName("보낸 피드백 조회 테스트")
    class GetSentFeedbacks {

        @Test
        @DisplayName("성공 시 200")
        void test1() {
            // given
            Member sender = member1;
            Member receiver = member2;
            Feedback feedback1 = createFeedback(sender, receiver, team1);
            Feedback feedback2 = createFeedback(sender, receiver, team1);
            feedbackRepository.saveAll(List.of(feedback1, feedback2));

            // when
            assertThat(mvc.get()
                    .uri("/api/feedbacks/sender/{senderId}", sender.getId())
                    .session(withLoginUser(sender))
            ).hasStatus(HttpStatus.OK);
        }

        @Test
        @DisplayName("팀으로 조회")
        void test2() {
            // given
            Member sender = member1;
            Member receiver = member2;
            Team team = team2;
            feedbackRepository.saveAll(List.of(
                    createFeedback(sender, receiver, team1),
                    createFeedback(sender, receiver, team2),
                    createFeedback(sender, receiver, team2)));

            // when & then
            assertThat(mvc.get()
                    .uri("/api/feedbacks/sender/{senderId}", sender.getId())
                    .queryParam("teamId", team.getId().toString())
                    .session(withLoginUser(sender))
            ).hasStatus(HttpStatus.OK)
                    .body()
                    .satisfies(result -> {
                        Paged<SentFeedbackDto> requests = mapper.readValue(result, new TypeReference<>() {
                        });
                        assertThat(requests.content()).hasSize(2);
                        assertThat(requests.content()).extracting(SentFeedbackDto::teamName)
                                .containsOnly(team.getName());
                    });

        }

        @Test
        @DisplayName("도움 여부로 조회")
        void test3() {
            // given
            Member sender = member1;
            Member receiver = member2;
            feedbackRepository.saveAll(List.of(
                    createFeedback(sender, receiver, team1, false, true),
                    createFeedback(sender, receiver, team2, false, false),
                    createFeedback(sender, receiver, team2, false, true)));

            // when & then
            assertThat(mvc.get()
                    .uri("/api/feedbacks/sender/{senderId}", sender.getId())
                    .queryParam("filterHelpful", "true")
                    .session(withLoginUser(sender))
            ).hasStatus(HttpStatus.OK)
                    .body()
                    .satisfies(result -> {
                        Paged<SentFeedbackDto> requests = mapper.readValue(result, new TypeReference<>() {
                        });
                        assertThat(requests.content()).extracting(SentFeedbackDto::liked)
                                .containsOnly(true);
                    });

        }

        @Test
        @DisplayName("페이지로 조회")
        void test4() {
            // given
            Member sender = member1;
            Member receiver = member2;
            for (int i = 0; i < 20; i++) {
                feedbackRepository.save(createFeedback(sender, receiver, team1));
            }

            // when & then
            assertThat(mvc.get()
                    .uri("/api/feedbacks/sender/{senderId}", sender.getId())
                    .queryParam("page", "1")
                    .session(withLoginUser(sender))
            ).hasStatus(HttpStatus.OK)
                    .body()
                    .satisfies(result -> {
                        Paged<SentFeedbackDto> requests = mapper.readValue(result, new TypeReference<>() {
                        });
                        assertThat(requests.page()).isEqualTo(1);
                        assertThat(requests.hasNext()).isEqualTo(false);
                        assertThat(requests.content()).hasSize(10);
                    });
        }

        @Test
        @DisplayName("정렬로 조회")
        void test5() {
            // given
            Member sender = member1;
            Member receiver = member2;
            for (int i = 0; i < 20; i++) {
                feedbackRepository.save(createFeedback(sender, receiver, team1));
            }
            // when & then
            assertThat(mvc.get()
                    .uri("/api/feedbacks/sender/{senderId}", sender.getId())
                    .queryParam("sortOrder", "ASC")
                    .session(withLoginUser(sender))
            ).hasStatus(HttpStatus.OK)
                    .body()
                    .satisfies(result -> {
                        Paged<SentFeedbackDto> requests = mapper.readValue(result, new TypeReference<>() {
                        });
                        assertThat(requests.content()).extracting(SentFeedbackDto::createdAt)
                                .isSortedAccordingTo(Comparator.naturalOrder());
                    });
        }

        @Test
        @DisplayName("본인이 아닌 경우 403")
        void test6() {
            // given
            Member sender = member1;
            Member notSender = member3;
            Member receiver = member2;
            Feedback feedback = createFeedback(sender, receiver, team1);
            feedbackRepository.save(feedback);

            // when
            assertThat(mvc.get()
                    .uri("/api/feedbacks/sender/{senderId}", sender.getId())
                    .session(withLoginUser(notSender))
            ).hasStatus(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("받은 피드백 조회 테스트")
    class GetReceivedFeedbacks {

        @Test
        @DisplayName("성공 시 200")
        void test1() {
            // given
            Member sender = member1;
            Member receiver = member2;
            Feedback feedback1 = createFeedback(sender, receiver, team1);
            Feedback feedback2 = createFeedback(sender, receiver, team1);
            feedbackRepository.saveAll(List.of(feedback1, feedback2));

            // when
            assertThat(mvc.get()
                    .uri("/api/feedbacks/receiver/{receiverId}", receiver.getId())
                    .session(withLoginUser(receiver))
            ).hasStatus(HttpStatus.OK);
        }

        @Test
        @DisplayName("팀으로 조회")
        void test2() {
            // given
            Member sender = member1;
            Member receiver = member2;
            Team team = team2;
            feedbackRepository.saveAll(List.of(
                    createFeedback(sender, receiver, team1),
                    createFeedback(sender, receiver, team2),
                    createFeedback(sender, receiver, team2)));

            // when & then
            assertThat(mvc.get()
                    .uri("/api/feedbacks/receiver/{receiverId}", receiver.getId())
                    .queryParam("teamId", team.getId().toString())
                    .session(withLoginUser(receiver))
            ).hasStatus(HttpStatus.OK)
                    .body()
                    .satisfies(result -> {
                        Paged<ReceivedFeedbackDto> requests = mapper.readValue(result, new TypeReference<>() {
                        });
                        assertThat(requests.content()).hasSize(2);
                        assertThat(requests.content()).extracting(ReceivedFeedbackDto::teamName)
                                .containsOnly(team.getName());
                    });

        }

        @Test
        @DisplayName("도움 여부로 조회")
        void test3() {
            // given
            Member sender = member1;
            Member receiver = member2;
            feedbackRepository.saveAll(List.of(
                    createFeedback(sender, receiver, team1, false, true),
                    createFeedback(sender, receiver, team2, false, false),
                    createFeedback(sender, receiver, team2, false, true)));

            // when & then
            assertThat(mvc.get()
                    .uri("/api/feedbacks/receiver/{receiverId}", receiver.getId())
                    .queryParam("filterHelpful", "true")
                    .session(withLoginUser(receiver))
            ).hasStatus(HttpStatus.OK)
                    .body()
                    .satisfies(result -> {
                        Paged<ReceivedFeedbackDto> requests = mapper.readValue(result, new TypeReference<>() {
                        });
                        assertThat(requests.content()).extracting(ReceivedFeedbackDto::liked)
                                .containsOnly(true);
                    });

        }

        @Test
        @DisplayName("페이지로 조회")
        void test4() {
            // given
            Member sender = member1;
            Member receiver = member2;
            for (int i = 0; i < 20; i++) {
                feedbackRepository.save(createFeedback(sender, receiver, team1));
            }

            // when & then
            assertThat(mvc.get()
                    .uri("/api/feedbacks/receiver/{receiverId}", receiver.getId())
                    .queryParam("page", "1")
                    .session(withLoginUser(receiver))
            ).hasStatus(HttpStatus.OK)
                    .body()
                    .satisfies(result -> {
                        Paged<ReceivedFeedbackDto> requests = mapper.readValue(result, new TypeReference<>() {
                        });
                        assertThat(requests.page()).isEqualTo(1);
                        assertThat(requests.hasNext()).isEqualTo(false);
                        assertThat(requests.content()).hasSize(10);
                    });
        }

        @Test
        @DisplayName("정렬로 조회")
        void test5() {
            // given
            Member sender = member1;
            Member receiver = member2;
            for (int i = 0; i < 20; i++) {
                feedbackRepository.save(createFeedback(sender, receiver, team1));
            }
            // when & then
            assertThat(mvc.get()
                    .uri("/api/feedbacks/receiver/{receiverId}", receiver.getId())
                    .queryParam("sortOrder", "ASC")
                    .session(withLoginUser(receiver))
            ).hasStatus(HttpStatus.OK)
                    .body()
                    .satisfies(result -> {
                        Paged<ReceivedFeedbackDto> requests = mapper.readValue(result, new TypeReference<>() {
                        });
                        assertThat(requests.content()).extracting(ReceivedFeedbackDto::createdAt)
                                .isSortedAccordingTo(Comparator.naturalOrder());
                    });
        }

        @Test
        @DisplayName("본인이 아닌 경우 403")
        void test6() {
            // given
            Member sender = member1;
            Member notReceiver = member3;
            Member receiver = member2;
            Feedback feedback = createFeedback(sender, receiver, team1);
            feedbackRepository.save(feedback);

            // when
            assertThat(mvc.get()
                    .uri("/api/feedbacks/receiver/{receiverId}", receiver.getId())
                    .session(withLoginUser(notReceiver))
            ).hasStatus(HttpStatus.FORBIDDEN);
        }
    }
}