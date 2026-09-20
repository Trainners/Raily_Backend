package io.trainners.raily_backend.domain.notification.service;

import io.trainners.raily_backend.domain.notification.event.NotificationCreatedEvent;
import io.trainners.raily_backend.domain.notification.model.dto.SeatWatchCreateResponse;
import io.trainners.raily_backend.domain.notification.model.dto.SeatWatchRequest;
import io.trainners.raily_backend.domain.notification.model.dto.SeatWatchStatusResponse;
import io.trainners.raily_backend.domain.notification.model.entity.Notification;
import io.trainners.raily_backend.domain.notification.model.entity.SeatWatch;
import io.trainners.raily_backend.domain.notification.model.entity.SeatWatchStatus;
import io.trainners.raily_backend.domain.notification.model.entity.StopSchedule;
import io.trainners.raily_backend.domain.notification.repository.NotificationRepository;
import io.trainners.raily_backend.domain.notification.repository.SeatWatchRepository;
import io.trainners.raily_backend.domain.trainRunPlan.client.TrainRunPlanClient;
import io.trainners.raily_backend.domain.trainRunPlan.dto.TrainRunInfo;
import io.trainners.raily_backend.domain.trainRunPlan.service.TrainRunPlanService;
import io.trainners.raily_backend.domain.user.model.entity.User;
import io.trainners.raily_backend.domain.user.repository.UserRepository;
import io.trainners.raily_backend.global.exception.BusinessException;
import io.trainners.raily_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatWatchService {

    // 공공데이터가 주는 형식: "2026-09-18 05:30:00.0"
    private static final DateTimeFormatter API_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

    // 우리가 저장하는 형식: "053000"
    private static final DateTimeFormatter HHMMSS =
            DateTimeFormatter.ofPattern("HHmmss");

    private final SeatWatchRepository seatWatchRepository;
    private final UserRepository userRepository;
    // TrainRunPlanService 의 메서드들이 클라이언트를 파라미터로 받는 구조라 함께 주입받는다.
    private final TrainRunPlanService trainRunPlanService;
    private final TrainRunPlanClient trainRunPlanClient;

    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

     // 착석 등록 (공공데이터 API를 1회 호출해 정차역 시각까지 저장해 둔다)
     // 스케줄러가 매 틱마다 외부 API를 부르지 않아도 되도록, 등록 시점에 한 번만 받아온다
    @Transactional
    public SeatWatchCreateResponse create(String email, SeatWatchRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 한 사람이 동시에 두 자리에 앉을 수 없으므로 기존 감시를 먼저 정리한다.
        // 프론트는 "자리 옮김" 시 이전 감시를 따로 취소할 필요가 없다.
        cancelActiveWatches(email);

        // 착석역 ~ 하차역 구간만 시각과 함께 가져온다
        List<TrainRunInfo> infos = trainRunPlanService.getTrainRunInfosBetween(
                request.runDate(),
                request.trainNumber(),
                request.fromStation(),
                request.toStation(),
                trainRunPlanClient
        );

        SeatWatch seatWatch = SeatWatch.builder()
                .user(user)
                .trainNumber(request.trainNumber())
                .carNumber(request.carNumber())
                .seatNumber(request.seatNumber())
                .runDate(request.runDate())
                .fromStation(request.fromStation())
                .toStation(request.toStation())
                .departureTime(request.departureTime())
                .arrivalTime(request.arrivalTime())
                .stops(toStopSchedules(infos))
                .build();

        SeatWatch saved = seatWatchRepository.save(seatWatch);
        return SeatWatchCreateResponse.from(saved);
    }

     // 인앱 폴링용 상태 조회
     // 푸시를 못 받는 사용자(권한 거부, iOS 미설치)에게는 이 경로가 유일한 통지 수단이다
    @Transactional(readOnly = true)
    public SeatWatchStatusResponse getStatus(String email, Long seatWatchId) {
        SeatWatch seatWatch = findMine(email, seatWatchId);
        return SeatWatchStatusResponse.from(seatWatch);
    }

     // 자리 비움 (상태만 CANCELED 로 바꾸고 행은 남긴다)
     // 물리 삭제하지 않는 이유: 이미 발송된 알림이 존재하지 않는 감시건을 가리키게 되고,
     // 나중에 "언제 어떤 자리에 앉았었나"를 되짚을 수 없어진다
    @Transactional
    public void cancel(String email, Long seatWatchId) {
        SeatWatch seatWatch = findMine(email, seatWatchId);
        seatWatch.cancel();   // 더티 체킹으로 UPDATE
    }

    // 판매 감지 처리 (스케줄러가 호출)
    // 상태 변경 / 알림 저장 / 이벤트 발행이 한 트랜잭션에서 일어나고, 실제 푸시는 커밋 이후 리스너가 보냄
    @Transactional
    public void markSold(Long seatWatchId, String soldFromStation) {
        SeatWatch seatWatch = seatWatchRepository.findById(seatWatchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_WATCH_NOT_FOUND));

        // 이미 알림이 나갔거나 취소된 건이면 중복 발송하지 않는다
        if (!seatWatch.isActive()) {
            return;
        }

        seatWatch.markNotified(soldFromStation);

        // Notification.seatSold 가 soldFromStation 을 읽으므로 markNotified 이후에 만들어야 한다
        Notification notification = notificationRepository.save(Notification.seatSold(seatWatch));
        applicationEventPublisher.publishEvent(new NotificationCreatedEvent(notification.getId()));
    }

    // 여정 종료 처리. 더 팔릴 수 없으므로 감시 대상에서 제외한다
    @Transactional
    public void expire(Long seatWatchId) {
        seatWatchRepository.findById(seatWatchId)
                .filter(SeatWatch::isActive)
                .ifPresent(SeatWatch::expire);
    }

    /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ private @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */

    // 내 감시건만 조회 (email 조건을 쿼리에 넣어 조회와 권한 검사를 한 번에 끝냄)
    // 남의 id를 넣으면 403이 아니라 404가 나가므로, 그 id의 존재 여부가 새어나가지 않는다!!
    private SeatWatch findMine(String email, Long seatWatchId) {
        return seatWatchRepository.findByIdAndUserEmail(seatWatchId, email)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_WATCH_NOT_FOUND));
    }

    private void cancelActiveWatches(String email) {
        List<SeatWatch> actives =
                seatWatchRepository.findAllByUserEmailAndStatus(email, SeatWatchStatus.ACTIVE);
        for (SeatWatch active : actives) {
            active.cancel();   // 영속 상태라 save() 없이 반영된다
        }
    }

    private List<StopSchedule> toStopSchedules(List<TrainRunInfo> infos) {
        List<StopSchedule> stops = new ArrayList<>();
        for (TrainRunInfo info : infos) {
            stops.add(new StopSchedule(
                    info.stationName(),
                    toHhmmss(info.arrivalDateTime()),
                    toHhmmss(info.departureDateTime())
            ));
        }
        return stops;
    }

    // "2026-09-18 05:30:00.0" -> "053000"
    // 시발역은 도착 시각이, 종착역은 출발 시각이 null 이므로 null 을 그대로 통과시킨다.
    private String toHhmmss(String apiDateTime) {
        if (apiDateTime == null) {
            return null;
        }
        return LocalDateTime.parse(apiDateTime, API_DATE_TIME).format(HHMMSS);
    }
}