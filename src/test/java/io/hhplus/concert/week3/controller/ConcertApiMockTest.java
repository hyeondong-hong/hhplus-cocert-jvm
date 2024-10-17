package io.hhplus.concert.week3.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ConcertApiMockTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp(
            RestDocumentationContextProvider restDocumentation
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(this.context)
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    @DisplayName("콘서트를 조회한다")
    public void searchConcerts() throws Exception {
        RestDocumentationResultHandler handler = MockMvcRestDocumentation.document(
                "concertResultPage",
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("[].concertId").description("콘서트 ID"),
                        PayloadDocumentation.fieldWithPath("[].title").description("콘서트명")
                )
        );

        mockMvc.perform(
                get("/a/concertResultPage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(handler);
    }

    @Test
    @DisplayName("콘서트 일정을 조회한다")
    public void searchConcertSchedules() throws Exception {
        Long concertId = 1L;
        RestDocumentationResultHandler handler = MockMvcRestDocumentation.document(
                "concertResultPage/schedules",
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("concertId").description("콘서트 ID")
                ),
                PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("[].concertId").description("콘서트 ID"),
                        PayloadDocumentation.fieldWithPath("[].concertScheduleId").description("콘서트 일정 ID"),
                        PayloadDocumentation.fieldWithPath("[].scheduledAt").description("콘서트 일정")
                )
        );

        mockMvc.perform(
                get("/a/concertResultPage/{concertId}/schedules", concertId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(handler);
    }

    @Test
    @DisplayName("콘서트 일정 내에서 좌석을 조회한다")
    public void searchConcertSeats() throws Exception {
        Long concertId = 1L;
        Long concertScheduleId = 1L;
        RestDocumentationResultHandler handler = MockMvcRestDocumentation.document(
                "concertResultPage/schedules/seatResults",
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("concertId").description("콘서트 ID"),
                        RequestDocumentation.parameterWithName("concertScheduleId").description("콘서트 일정 ID")
                ),
                PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("concertScheduleId").description("콘서트 일정 ID"),
                        PayloadDocumentation.fieldWithPath("seatNumbers[]").description("좌석 번호")
                )
        );

        mockMvc.perform(
                get("/a/concertResultPage/{concertId}/schedules/{concertScheduleId}/seatResults", concertId, concertScheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(handler);
    }

    @Test
    @DisplayName("콘서트 좌석을 예약한다")
    public void reservation() throws Exception {
        Long concertId = 1L;
        Long concertScheduleId = 1L;
        Integer seatNumber = 1;
        RestDocumentationResultHandler handler = MockMvcRestDocumentation.document(
                "concertResultPage/schedules/reservations",
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("concertId").description("콘서트 ID"),
                        RequestDocumentation.parameterWithName("concertScheduleId").description("콘서트 일정 ID")
                ),
                PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("reservationId").description("예약 ID"),
                        PayloadDocumentation.fieldWithPath("concertScheduleId").description("콘서트 일정 ID"),
                        PayloadDocumentation.fieldWithPath("seatNumber").description("좌석 번호"),
                        PayloadDocumentation.fieldWithPath("reservationStatus").description("예약 상태"),
                        PayloadDocumentation.fieldWithPath("paymentId").description("결제 ID"),
                        PayloadDocumentation.fieldWithPath("paymentStatus").description("결제 상태")
                )
        );

        String requestBody = """
        {"userId": 1, "token": "token-info-long-hashed-text"}
        """;
        mockMvc.perform(
                post("/a/concertResultPage/{concertId}/schedules/{concertScheduleId}/reservations", concertId, concertScheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(handler);
    }

    @Test
    @DisplayName("예약 결제를 처리한다")
    public void purchaseReservation() throws Exception {
        Long concertId = 1L;
        Long concertScheduleId = 1L;
        Long reservationId = 1L;

        RestDocumentationResultHandler handler = MockMvcRestDocumentation.document(
                "concertResultPage/schedules/reservations/purchase",
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("concertId").description("콘서트 ID"),
                        RequestDocumentation.parameterWithName("concertScheduleId").description("콘서트 일정 ID"),
                        RequestDocumentation.parameterWithName("reservationId").description("예약 ID")
                ),
                PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("reservationId").description("예약 ID"),
                        PayloadDocumentation.fieldWithPath("userId").description("유저 ID"),
                        PayloadDocumentation.fieldWithPath("paymentId").description("결제 ID"),
                        PayloadDocumentation.fieldWithPath("paymentStatus").description("결제 상태")
                )
        );

        String requestBody = """
        {"userId": 1, "token": "token-info-long-hashed-text"}
        """;
        mockMvc.perform(
                patch("/a/concertResultPage/{concertId}/schedules/{concertScheduleId}/reservations/{reservationId}", concertId, concertScheduleId, reservationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(handler);
    }
}
