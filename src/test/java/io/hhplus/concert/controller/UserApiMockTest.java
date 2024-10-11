package io.hhplus.concert.controller;

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
public class UserApiMockTest {

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
    @DisplayName("토큰을 발행한다")
    public void issueToken() throws Exception {
        Long userId = 1L;
        String token = "token-info-long-hashed-text";

        RestDocumentationResultHandler handler = MockMvcRestDocumentation.document(
                "users/tokens",
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("userId").description("유저 ID")
                ),
                PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("token").description("대기열 토큰")
                )
        );

        mockMvc.perform(
                        post("/a/users/{userId}/tokens", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(handler);
    }

    @Test
    @DisplayName("유저 포인트를 조회한다")
    public void retrieveUserPoint() throws Exception {
        Long userId = 1L;

        RestDocumentationResultHandler handler = MockMvcRestDocumentation.document(
                "users/point",
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("userId").description("유저 ID")
                ),
                PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("userId").description("유저 ID"),
                        PayloadDocumentation.fieldWithPath("remains").description("잔여 포인트")
                )
        );

        mockMvc.perform(
                        get("/a/users/{userId}/point", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(handler);
    }

    @Test
    @DisplayName("유저 포인트를 충전한다")
    public void chargeUserPoint() throws Exception {
        Long userId = 1L;

        RestDocumentationResultHandler handler = MockMvcRestDocumentation.document(
                "users/point",
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                RequestDocumentation.pathParameters(
                        RequestDocumentation.parameterWithName("userId").description("유저 ID")
                ),
                PayloadDocumentation.requestFields(
                        PayloadDocumentation.fieldWithPath("amount").description("포인트를 충전할 값")
                ),
                PayloadDocumentation.responseFields(
                        PayloadDocumentation.fieldWithPath("userId").description("유저 ID"),
                        PayloadDocumentation.fieldWithPath("remains").description("잔여 포인트"),
                        PayloadDocumentation.fieldWithPath("charged").description("충전한 포인트")
                )
        );

        String requestBody = """
        {"amount": 1000}
        """;
        mockMvc.perform(
                        patch("/a/users/{userId}/point", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(handler);
    }
}
