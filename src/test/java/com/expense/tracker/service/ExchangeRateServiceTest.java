package com.expense.tracker.service;

import com.expense.tracker.dto.ExchangeRateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private CacheManager cacheManager;
    private ExchangeRateService service;

    @BeforeEach
    void setUp() {
        cacheManager = new ConcurrentMapCacheManager("exchange-rates");
        service = new ExchangeRateService(restTemplate, cacheManager);
    }

    @Test
    void getRates_validResponse_returnsParsedRates() {
        Map<String, Object> body = Map.of(
                "base", "TRY",
                "date", "2026-05-25",
                "rates", Map.of("EUR", 0.026, "USD", 0.029)
        );
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(body));

        ExchangeRateResponse result = service.getRates("TRY");

        assertThat(result.base()).isEqualTo("TRY");
        assertThat(result.date()).isEqualTo("2026-05-25");
        assertThat(result.rates()).containsKeys("EUR", "USD");
        assertThat(result.rates().get("EUR")).isEqualByComparingTo(new BigDecimal("0.026"));
    }

    @Test
    void getRates_apiFailure_returnsFallbackWithEmptyRates() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("Connection refused"));

        ExchangeRateResponse result = service.getRates("EUR");

        assertThat(result.base()).isEqualTo("EUR");
        assertThat(result.rates()).isEmpty();
    }

    @Test
    void getRates_nullBody_returnsFallback() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(null));

        ExchangeRateResponse result = service.getRates("USD");

        assertThat(result.base()).isEqualTo("USD");
        assertThat(result.rates()).isEmpty();
    }
}
