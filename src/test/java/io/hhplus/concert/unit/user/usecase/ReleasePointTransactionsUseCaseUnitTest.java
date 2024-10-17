package io.hhplus.concert.unit.user.usecase;

import io.hhplus.concert.payment.domain.Payment;
import io.hhplus.concert.payment.domain.enm.PaymentStatus;
import io.hhplus.concert.payment.port.PaymentPort;
import io.hhplus.concert.user.domain.PointTransaction;
import io.hhplus.concert.user.domain.enm.PointTransactionStatus;
import io.hhplus.concert.user.domain.enm.PointTransactionType;
import io.hhplus.concert.user.port.PointTransactionPort;
import io.hhplus.concert.user.usecase.ReleasePointTransactionsUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReleasePointTransactionsUseCaseUnitTest {

    @Mock
    private PointTransactionPort pointTransactionPort;

    @Mock
    private PaymentPort paymentPort;

    @InjectMocks
    private ReleasePointTransactionsUseCase releasePointTransactionsUseCase;

    private List<PointTransaction> selectedPointTransactions = new ArrayList<>();
    private List<Payment> selectedPayments = new ArrayList<>();
    private List<PointTransaction> savedPointTransactions = new ArrayList<>();
    private List<Payment> savedPayments = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        for (int i = 0; i < 10; i++) {
            PointTransaction pt = new PointTransaction();
            pt.setId(i+1L);
            pt.setType(PointTransactionType.CHARGE);
            pt.setStatus(PointTransactionStatus.PENDING);
            pt.setRemains(0);
            pt.setPaymentId(i+11L);
            pt.setAmount(3000);
            pt.setCreatedAt(LocalDateTime.now());
            pt.setModifiedAt(LocalDateTime.now());
            selectedPointTransactions.add(pt);
        }

        // 앞선 5건만 만료된 결제건으로 간주
        for (int i = 0; i < selectedPointTransactions.size() - 5; i++) {
            Payment p = new Payment();
            p.setId(selectedPointTransactions.get(i).getPaymentId());
            p.setPrice(BigDecimal.valueOf(3000));
            p.setDueAt(LocalDateTime.now().minusDays(1));
            p.setUserId(1L);
            p.setStatus(PaymentStatus.PENDING);
            p.setPaidAt(null);
            selectedPayments.add(p);
        }
    }

    @AfterEach
    public void tearDown() {
        selectedPointTransactions.clear();
        selectedPayments.clear();
        savedPointTransactions.clear();
        savedPayments.clear();
    }

    @Test
    @DisplayName("대기중인 포인트 거래내역이 없으면 다음 스텝으로 넘어가지 않아야 한다")
    public void noAvailablePendingPointTransactions() {
        when(pointTransactionPort.getAllByStatusesWithLock(any(List.class))).thenReturn(List.of());
        lenient().when(paymentPort.getExpiredAllByIdsWithLock(any(List.class))).thenThrow(AssertionError.class);

        assertDoesNotThrow(
                () -> releasePointTransactionsUseCase.execute(new ReleasePointTransactionsUseCase.Input())
        );
    }

    @Test
    @DisplayName("대기중인 포인트 거래내역이 있지만 만료된 결제정보가 존재하지 않으면 다음 스텝으로 넘어가지 않아야 한다")
    public void noAvailableExpiredPayment() {
        when(pointTransactionPort.getAllByStatusesWithLock(any(List.class))).then(r -> selectedPointTransactions);
        when(paymentPort.getExpiredAllByIdsWithLock(any(List.class))).thenReturn(List.of());
        lenient().when(paymentPort.saveAll(any(List.class))).thenThrow(AssertionError.class);

        assertDoesNotThrow(
                () -> releasePointTransactionsUseCase.execute(new ReleasePointTransactionsUseCase.Input())
        );
    }

    @Test
    @DisplayName("만료된 결제 정보가 존재하면 해당 결제 정보에 연결된 포인트 거래내역만 취소 처리 한다.")
    public void cancelExpired() {
        when(pointTransactionPort.getAllByStatusesWithLock(any(List.class))).then(r -> selectedPointTransactions);
        when(paymentPort.getExpiredAllByIdsWithLock(any(List.class))).then(r -> selectedPayments);
        when(pointTransactionPort.saveAll(any(List.class))).then(r -> {
            List<PointTransaction> result = r.getArgument(0);
            savedPointTransactions.addAll(result);
            return result;
        });
        when(paymentPort.saveAll(any(List.class))).then(r -> {
            List<Payment> result = r.getArgument(0);
            savedPayments.addAll(result);
            return result;
        });

        releasePointTransactionsUseCase.execute(new ReleasePointTransactionsUseCase.Input());

        // 본래 길이는 10이지만 payments의 길이 만큼
        assertEquals(5, savedPointTransactions.size());
        assertEquals(5, savedPayments.size());
    }
}
