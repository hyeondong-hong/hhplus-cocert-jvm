package io.hhplus.concert.apps.unit.concert.usecase;

import io.hhplus.concert.app.concert.domain.ConcertSeat;
import io.hhplus.concert.app.concert.domain.Reservation;
import io.hhplus.concert.app.concert.domain.enm.ReservationStatus;
import io.hhplus.concert.app.concert.port.ConcertPort;
import io.hhplus.concert.app.concert.port.ConcertSchedulePort;
import io.hhplus.concert.app.concert.port.ConcertSeatPort;
import io.hhplus.concert.app.concert.port.ReservationPort;
import io.hhplus.concert.app.concert.usecase.ReservationSeatUseCase;
import io.hhplus.concert.app.payment.domain.Payment;
import io.hhplus.concert.app.payment.port.PaymentPort;
import io.hhplus.concert.app.user.domain.Token;
import io.hhplus.concert.app.user.port.TokenPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationSeatUseCaseUnitTest {

    @Mock
    private ConcertPort concertPort;

    @Mock
    private ConcertSchedulePort concertSchedulePort;

    @Mock
    private ConcertSeatPort concertSeatPort;

    @Mock
    private ReservationPort reservationPort;

    @Mock
    private PaymentPort paymentPort;

    @Mock
    private TokenPort tokenPort;

    @InjectMocks
    private ReservationSeatUseCase reservationSeatUseCase;

    private String keyUuid;

    private Token token;

    private ConcertSeat concertSeat;

    @BeforeEach
    public void setUp() {
        keyUuid = UUID.randomUUID().toString();
        token = Token.builder()
                .id(3840L)
                .keyUuid(keyUuid)
                .userId(64L)
                .createdAt(LocalDateTime.now())
                .build();

        concertSeat = ConcertSeat.builder()
                .id(3L)
                .concertScheduleId(2L)
                .label("일반석")
                .price(BigDecimal.valueOf(12000))
                .seatNumber(53)
                .isActive(true)
                .build();

        lenient().when(tokenPort.getByKey(eq(keyUuid))).thenReturn(token);
        lenient().when(concertPort.existsById(any(Long.class))).thenReturn(true);
        lenient().when(concertSchedulePort.existsById(any(Long.class))).thenReturn(true);
        lenient().when(concertSeatPort.getWithLock(any(Long.class))).thenReturn(concertSeat);
        lenient().when(concertSeatPort.save(any(ConcertSeat.class))).thenReturn(concertSeat);

        lenient().when(paymentPort.save(any(Payment.class))).then(r -> {
            Payment origin = r.getArgument(0);
            return Payment.builder()
                    .id(2560L)
                    .userId(origin.getUserId())
                    .paymentKey(origin.getPaymentKey())
                    .price(origin.getPrice())
                    .status(origin.getStatus())
                    .dueAt(origin.getDueAt())
                    .paidAt(origin.getPaidAt())
                    .build();
        });

        lenient().when(reservationPort.save(any(Reservation.class))).then(r -> {
            Reservation origin = r.getArgument(0);
            return Reservation.builder()
                    .id(1280L)
                    .userId(origin.getUserId())
                    .concertSeatId(origin.getConcertSeatId())
                    .status(origin.getStatus())
                    .paymentId(origin.getPaymentId())
                    .build();
        });
    }

    @Test
    @DisplayName("토큰이 존재하지 않으면 예외를 발생한다")
    public void tokenNotFound() {
        String unknownToken = UUID.randomUUID().toString();
        // 본래 Security Config에서 Authorization 인증을 통해 이미 검증되므로 발생하지 않는 케이스임
        BadCredentialsException e = new BadCredentialsException("존재하지 않는 토큰: keyUuid = " + unknownToken);
        when(tokenPort.getByKey(eq(unknownToken))).thenThrow(e);

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> reservationSeatUseCase.execute(
                        new ReservationSeatUseCase.Input(
                                unknownToken,
                                1L,
                                2L,
                                3L
                        )
                ));

        assertEquals(e.getMessage(), ex.getMessage());
    }

    @Test
    @DisplayName("콘서트가 없으면 예외가 발생한다")
    public void noConcert() {
        doThrow(new NoSuchElementException("Concert not found: " + 1L)).when(concertPort).existsOrThrow(any(Long.class));

        NoSuchElementException e = assertThrows(
                NoSuchElementException.class, () -> reservationSeatUseCase.execute(
                        new ReservationSeatUseCase.Input(
                                keyUuid,
                                1L,
                                2L,
                                3L
                        )
                ));

        assertEquals("Concert not found: " + 1L, e.getMessage());
    }

    @Test
    @DisplayName("스케줄이 없으면 예외가 발생한다")
    public void noSchedule() {
        doThrow(new NoSuchElementException("Concert Schedule not found: " + 2L)).when(concertSchedulePort).existsOrThrow(any(Long.class));

        NoSuchElementException e = assertThrows(
                NoSuchElementException.class, () -> reservationSeatUseCase.execute(
                        new ReservationSeatUseCase.Input(
                                keyUuid,
                                1L,
                                2L,
                                3L
                        )
                ));

        assertEquals("Concert Schedule not found: " + 2L, e.getMessage());
    }

    @Test
    @DisplayName("좌석이 없으면 예외가 발생한다")
    public void noSeat() {
        when(concertSeatPort.getWithLock(any(Long.class))).thenThrow(new NoSuchElementException("No value present"));

        NoSuchElementException e = assertThrows(
                NoSuchElementException.class, () -> reservationSeatUseCase.execute(
                        new ReservationSeatUseCase.Input(
                                keyUuid,
                                1L,
                                2L,
                                3L
                        )
                ));

        assertEquals("No value present", e.getMessage());
    }

    @Test
    @DisplayName("좌석이 비활성화 상태면 예외가 발생한다")
    public void noActivatedSeat() {
        concertSeat.close();

        IllegalStateException e = assertThrows(
                IllegalStateException.class, () -> reservationSeatUseCase.execute(
                        new ReservationSeatUseCase.Input(
                                keyUuid,
                                1L,
                                2L,
                                3L
                        )
                ));

        assertEquals("이미 예약된 좌석: " + concertSeat.getId(), e.getMessage());
    }

    @Test
    @DisplayName("좌석을 임시로 예약한다")
    public void tempReserveSeat() {
        ReservationSeatUseCase.Output output = reservationSeatUseCase.execute(
                new ReservationSeatUseCase.Input(
                        keyUuid,
                        1L,
                        2L,
                        3L
                )
        );

        assertEquals(ReservationStatus.PENDING, output.reservationResult().status());
    }
}
