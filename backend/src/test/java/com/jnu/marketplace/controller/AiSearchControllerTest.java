package com.jnu.marketplace.controller;

import com.jnu.marketplace.ai.AiUnavailableException;
import com.jnu.marketplace.ai.search.AiSearchResponse;
import com.jnu.marketplace.ai.search.AiSearchService;
import com.jnu.marketplace.dto.SearchRequest;
import com.jnu.marketplace.model.Listing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AiSearchControllerTest {

    private AiSearchService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(AiSearchService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AiSearchController(service)).build();
    }

    @Test
    void validRequestReturnsPageShapedResponseAndPassesFiltersThrough() throws Exception {
        when(service.search(any(SearchRequest.class), eq(1), eq(40))).thenReturn(
                new AiSearchResponse(List.of(), 1, 40, 0, 0, AiSearchResponse.SEMANTIC));

        mvc.perform(post("/api/ai/search")
                        .param("page", "1")
                        .param("size", "40")
                        .contentType("application/json")
                        .content("{\"keyword\":\"bike\",\"category\":\"VEHICLES\",\"condition\":\"GOOD\","
                                + "\"minPrice\":10,\"maxPrice\":500}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.searchMode").value("SEMANTIC"))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(service).search(captor.capture(), eq(1), eq(40));
        SearchRequest sent = captor.getValue();
        assertThat(sent.getKeyword()).isEqualTo("bike");
        assertThat(sent.getCategory()).isEqualTo("VEHICLES");
        assertThat(sent.getCondition()).isEqualTo(Listing.Condition.GOOD);
        assertThat(sent.getMinPrice()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(sent.getMaxPrice()).isEqualByComparingTo(new BigDecimal("500"));
    }

    @Test
    void invalidSizeFallsBackToTheDefaultPageSize() throws Exception {
        when(service.search(any(SearchRequest.class), anyInt(), anyInt())).thenReturn(
                new AiSearchResponse(List.of(), 0, 20, 0, 0, AiSearchResponse.KEYWORD));

        mvc.perform(post("/api/ai/search").param("size", "500")
                        .contentType("application/json").content("{\"keyword\":\"bike\"}"))
                .andExpect(status().isOk());

        verify(service).search(any(SearchRequest.class), eq(0), eq(20));
    }

    @Test
    void negativePageIsClampedToZero() throws Exception {
        when(service.search(any(SearchRequest.class), anyInt(), anyInt())).thenReturn(
                new AiSearchResponse(List.of(), 0, 20, 0, 0, AiSearchResponse.KEYWORD));

        mvc.perform(post("/api/ai/search").param("page", "-3")
                        .contentType("application/json").content("{\"keyword\":\"bike\"}"))
                .andExpect(status().isOk());

        verify(service).search(any(SearchRequest.class), eq(0), anyInt());
    }

    @Test
    void infrastructureErrorsAreNotLeakedToTheClient() throws Exception {
        when(service.search(any(SearchRequest.class), anyInt(), anyInt()))
                .thenThrow(new AiUnavailableException("Qdrant at 10.1.2.3:6333 refused connection"));

        mvc.perform(post("/api/ai/search").contentType("application/json").content("{\"keyword\":\"bike\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Search is temporarily unavailable. Please try again."))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("6333"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Qdrant"))));
    }

    @Test
    void invalidUserInputGetsAShortMessage() throws Exception {
        when(service.search(any(SearchRequest.class), anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException("Invalid category: nope"));

        mvc.perform(post("/api/ai/search").contentType("application/json").content("{\"keyword\":\"bike\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid search filter"));
    }
}
