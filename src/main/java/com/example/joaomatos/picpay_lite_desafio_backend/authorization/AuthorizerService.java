package com.example.joaomatos.picpay_lite_desafio_backend.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.joaomatos.picpay_lite_desafio_backend.transation.Transaction;
import com.example.joaomatos.picpay_lite_desafio_backend.transation.UnauthorizedTransactionException;

@Service
public class AuthorizerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizerService.class);
    private RestClient restClient;

    public AuthorizerService(RestClient.Builder builder) {
        this.restClient = builder.baseUrl(
                "https://run.mocky.io/v3/5794d450-d2e2-4412-8131-73d0293ac1cc").build();
    }

    @SuppressWarnings("null")
    public void authorize(Transaction transaction) {
        LOGGER.info("Authorizing transaction ", transaction);

        var res = restClient.get().retrieve().toEntity(Authorization.class);
        if (res.getStatusCode().isError() || !res.getBody().isAuthorized()) {
            throw new UnauthorizedTransactionException("Unauthorized!");
        }
    }

    record Authorization(String message) {
        public boolean isAuthorized() {
            return message.equals("Autorizado");
        }
    }
}
