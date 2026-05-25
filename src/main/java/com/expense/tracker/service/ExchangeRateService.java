package com.expense.tracker.service;

import com.expense.tracker.dto.ExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateService {

    private static final String BASE_URL = "https://api.frankfurter.app";
    private static final Set<String> SUPPORTED = Set.of("TRY", "EUR", "USD");

    private final RestTemplate restTemplate;
    private final CacheManager cacheManager;

    @Cacheable(value = "exchange-rates", key = "#from")
    public ExchangeRateResponse getRates(String from) {
        List<String> targets = SUPPORTED.stream()
                .filter(c -> !c.equals(from))
                .toList();
        String url = BASE_URL + "/latest?from=" + from + "&to=" + String.join(",", targets);

        try {
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();
            if (body == null) {
                return fallback(from);
            }

            String date = (String) body.get("date");
            @SuppressWarnings("unchecked")
            Map<String, Number> rawRates = (Map<String, Number>) body.get("rates");

            Map<String, BigDecimal> rates = rawRates.entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            e -> new BigDecimal(e.getValue().toString())
                    ));

            return new ExchangeRateResponse(from, date, rates);
        } catch (Exception e) {
            log.warn("Failed to fetch exchange rates from Frankfurter API: {}", e.getMessage());
            return fallback(from);
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void evictCache() {
        var cache = cacheManager.getCache("exchange-rates");
        if (cache != null) {
            cache.clear();
            log.debug("Exchange rate cache evicted");
        }
    }

    private ExchangeRateResponse fallback(String from) {
        return new ExchangeRateResponse(from, LocalDate.now().toString(), Map.of());
    }
}
