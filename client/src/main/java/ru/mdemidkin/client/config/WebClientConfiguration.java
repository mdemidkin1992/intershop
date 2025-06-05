package ru.mdemidkin.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import ru.mdemidkin.intershop.client.ApiClient;
import ru.mdemidkin.intershop.client.api.HealthApi;
import ru.mdemidkin.intershop.client.api.PaymentsApi;

@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient paymentWebClient(PaymentAppProperties properties) {
        return WebClient.builder()
                .baseUrl(getBaseUrl(properties))
                .build();
    }

    @Bean
    public ApiClient paymentApiClient(WebClient paymentWebClient,
                                      PaymentAppProperties properties) {
        ApiClient apiClient = new ApiClient(paymentWebClient);
        apiClient.setBasePath(getBaseUrl(properties));
        return apiClient;
    }

    @Bean
    public PaymentsApi paymentsApi(ApiClient paymentApiClient) {
        return new PaymentsApi(paymentApiClient);
    }

    @Bean
    public HealthApi healthApi(ApiClient paymentApiClient) {
        return new HealthApi(paymentApiClient);
    }

    private String getBaseUrl(PaymentAppProperties properties) {
        return "http://" + properties.getHost() + ":" + properties.getPort();
    }
}
