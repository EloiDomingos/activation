package com.deroyque.activation.client;

import com.deroyque.activation.exception.ProviderIntegrationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class ProviderClient {

    private final RestTemplate restTemplate;
    private final String providerUrl;

    public ProviderClient(RestTemplate restTemplate, @Value("${app.provider.url}") String providerUrl) {
        this.restTemplate = restTemplate;
        this.providerUrl = providerUrl;
    }

    public void activate(String externalId, String customerId, String productCode) {
        String url = providerUrl + "/provider/v1/activate";
        Map<String, String> request = Map.of(
                "externalId", externalId,
                "customerId", customerId,
                "productCode", productCode
        );

        try {
            restTemplate.postForEntity(url, request, Void.class);
        } catch (ResourceAccessException e) {
            throw new ProviderIntegrationException("Timeout ou falha de conexão com o provedor", e);
        } catch (RestClientException e) {
            throw new ProviderIntegrationException("Resposta de erro do provedor", e);
        } catch (Exception e) {
            throw new ProviderIntegrationException("Erro inesperado durante a integração com o provedor", e);
        }
    }
}
