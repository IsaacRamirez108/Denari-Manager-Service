package com.denari.manager.services;

import com.denari.manager.models.entity.User.User;
import com.denari.manager.models.entity.ExternalAccount.Counterparty;
import com.denari.manager.models.entity.VirtualAccount.AccountDetail;
import com.denari.manager.models.entity.VirtualAccount.BankAddress;
import com.denari.manager.models.entity.VirtualAccount.RoutingDetail;
import com.denari.manager.models.entity.VirtualAccount.VirtualAccount;
import com.denari.manager.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ModernTreasuryService {

    @Value("${moderntreasury.apiKey}")
    private String apiKey;

    @Value("${moderntreasury.organizationId}")
    private String organizationId;

    @Value("${moderntreasury.apiUrl}")
    private String apiUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CounterpartyRepository counterpartyRepository;

    @Autowired
    private VirtualAccountRepository virtualAccountRepository;

    @Autowired
    private AccountDetailRepository accountDetailRepository;

    @Autowired
    private RoutingDetailRepository routingDetailRepository;

    @Autowired
    private BankAddressRepository bankAddressRepository;

    /**
     * Create counterparty in Modern Treasury - CORRECTED VERSION
     */
    public String createCounterparty(User user) {
        try {
            log.info("Creating Modern Treasury counterparty for user: {}", user.getId());

            // ✅ CORRECTED: Use exact URL format that works
            String url = apiUrl + "/counterparties";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", user.getFirstName() + " " + user.getLastName());
            requestBody.put("email", user.getEmail());

            if (user.getUserIdentity() != null && user.getUserIdentity().getPhoneNumber() != null) {
                requestBody.put("phone_number", user.getUserIdentity().getPhoneNumber());
            }

            // ✅ CORRECTED: Use EXACT format from your working project
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.info("Making request to: {}", url);
            log.info("Request headers: {}", headers);
            log.info("Request body: {}", requestBody);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String counterpartyId = (String) responseBody.get("id");

                // Save counterparty to database
                saveCounterparty(user, counterpartyId, responseBody);

                log.info("Counterparty created successfully: {}", counterpartyId);
                return counterpartyId;
            } else {
                throw new RuntimeException("Failed to create counterparty: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error creating counterparty for user {}: {}", user.getId(), e.getMessage());
            throw new RuntimeException("Failed to create counterparty: " + e.getMessage());
        }
    }

    /**
     * Create virtual account in Modern Treasury - CORRECTED VERSION
     */
    public String createVirtualAccount(User user, String counterpartyId) {
        try {
            log.info("Creating Modern Treasury virtual account for user: {}", user.getId());

            // ✅ CORRECTED: Use exact URL format that works
            String url = apiUrl + "/virtual_accounts";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", "Funds on behalf of " + user.getFirstName() + " " + user.getLastName());
            requestBody.put("counterparty_id", counterpartyId);

            // ✅ Use the internal account ID from your working project
            requestBody.put("internal_account_id", "5ff7e431-ee04-4c88-9ad3-f5256de2ae88");

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.info("Making virtual account request to: {}", url);
            log.info("Request body: {}", requestBody);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String virtualAccountId = (String) responseBody.get("id");

                // Save virtual account to database
                saveVirtualAccount(user, virtualAccountId, responseBody);

                log.info("Virtual account created successfully: {}", virtualAccountId);
                return virtualAccountId;
            } else {
                throw new RuntimeException("Failed to create virtual account: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error creating virtual account for user {}: {}", user.getId(), e.getMessage());
            throw new RuntimeException("Failed to create virtual account: " + e.getMessage());
        }
    }

    /**
     * Create account collection flow for bank account verification - CORRECTED VERSION
     */
    public String createAccountCollectionFlow(Long userId) {
        try {
            log.info("Creating account collection flow for user: {}", userId);

            // ✅ CORRECTED: Use exact URL format that works
            String url = apiUrl + "/account_collection_flows";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("counterparty_id", getCounterpartyIdByUserId(userId));
            requestBody.put("payment_types", new String[]{"ach"});

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String clientToken = (String) responseBody.get("client_token");

                log.info("Account collection flow created successfully for user: {}", userId);
                return clientToken;
            } else {
                throw new RuntimeException("Failed to create account collection flow: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error creating account collection flow for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to create account collection flow: " + e.getMessage());
        }
    }

    /**
     * Get virtual account details - CORRECTED VERSION
     */
    public Map<String, Object> getVirtualAccountDetails(Long userId) {
        try {
            VirtualAccount virtualAccount = virtualAccountRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Virtual account not found for user: " + userId));

            // ✅ CORRECTED: Use exact URL format that works
            String url = apiUrl + "/virtual_accounts/" + virtualAccount.getMtId();

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to get virtual account details: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error getting virtual account details for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get virtual account details: " + e.getMessage());
        }
    }

    /**
     * Create authentication headers for Modern Treasury API - CORRECTED VERSION
     * ✅ FIXED: Use setBasicAuth() like your working project
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // ✅ CORRECTED: Use the EXACT same format as your working CounterpartyService
        // organizationId as username, apiKey as password
        headers.setBasicAuth(organizationId, apiKey);

        return headers;
    }

    /**
     * Save counterparty to database
     */
    private void saveCounterparty(User user, String mtCounterpartyId, Map<String, Object> responseData) {
        try {
            Counterparty counterparty = new Counterparty();
            counterparty.setUser(user);
            counterparty.setMtCounterpartyId(mtCounterpartyId);
            counterparty.setName((String) responseData.get("name"));
            counterparty.setEmail((String) responseData.get("email"));
            counterparty.setPhoneNumber((String) responseData.get("phone_number"));
            counterparty.setCreatedAt(LocalDateTime.now());
            counterparty.setUpdatedAt(LocalDateTime.now());

            counterpartyRepository.save(counterparty);
            log.info("Counterparty saved to database: {}", mtCounterpartyId);

        } catch (Exception e) {
            log.error("Error saving counterparty to database: {}", e.getMessage());
        }
    }

    /**
     * Save virtual account to database - SIMPLIFIED VERSION
     */
    private void saveVirtualAccount(User user, String mtVirtualAccountId, Map<String, Object> responseData) {
        try {
            VirtualAccount virtualAccount = new VirtualAccount();
            virtualAccount.setUser(user);
            virtualAccount.setMtId(mtVirtualAccountId);
            virtualAccount.setName((String) responseData.get("name"));
            virtualAccount.setObject((String) responseData.get("object"));
            virtualAccount.setActive(true);
            virtualAccount.setLiveMode((Boolean) responseData.getOrDefault("live_mode", false));

            // Only set if not null
            virtualAccount.setCounterpartyId((String) responseData.get("counterparty_id"));
            virtualAccount.setInternalAccountId((String) responseData.get("internal_account_id"));
            virtualAccount.setDebitLedgerAccountId((String) responseData.get("debit_ledger_account_id"));
            virtualAccount.setCreditLedgerAccountId((String) responseData.get("credit_ledger_account_id"));
            virtualAccount.setDescription((String) responseData.get("description"));

            // Parse timestamps if present
            if (responseData.get("created_at") != null) {
                virtualAccount.setCreatedAt(OffsetDateTime.parse((String) responseData.get("created_at")));
            }
            if (responseData.get("updated_at") != null) {
                virtualAccount.setUpdatedAt(OffsetDateTime.parse((String) responseData.get("updated_at")));
            }

            VirtualAccount savedAccount = virtualAccountRepository.save(virtualAccount);
            log.info("Virtual account saved to database: {}", mtVirtualAccountId);

            // Save account details if present
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> accountDetails = (List<Map<String, Object>>) responseData.get("account_details");
            if (accountDetails != null && !accountDetails.isEmpty()) {
                for (Map<String, Object> detailData : accountDetails) {
                    saveAccountDetail(savedAccount, detailData);
                }
            }

            // Save routing details if present
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> routingDetails = (List<Map<String, Object>>) responseData.get("routing_details");
            if (routingDetails != null && !routingDetails.isEmpty()) {
                for (Map<String, Object> routingData : routingDetails) {
                    saveRoutingDetail(savedAccount, routingData);
                }
            }

        } catch (Exception e) {
            log.error("Error saving virtual account to database: {}", e.getMessage());
            // Don't throw - log and continue so onboarding doesn't fail
        }
    }

    /**
     * Save account detail to database
     */
    private void saveAccountDetail(VirtualAccount virtualAccount, Map<String, Object> detailData) {
        try {
            AccountDetail accountDetail = new AccountDetail();
            accountDetail.setVirtualAccount(virtualAccount);
            accountDetail.setExternalId((String) detailData.get("id"));
            accountDetail.setObject((String) detailData.get("object"));
            accountDetail.setLiveMode((Boolean) detailData.getOrDefault("live_mode", false));
            accountDetail.setAccountNumberSafe((String) detailData.get("account_number_safe"));
            accountDetail.setAccountNumberType((String) detailData.get("account_number_type"));

            if (detailData.get("created_at") != null) {
                accountDetail.setCreatedAt(OffsetDateTime.parse((String) detailData.get("created_at")));
            }
            if (detailData.get("updated_at") != null) {
                accountDetail.setUpdatedAt(OffsetDateTime.parse((String) detailData.get("updated_at")));
            }

            accountDetailRepository.save(accountDetail);
            log.debug("Account detail saved for virtual account: {}", virtualAccount.getMtId());

        } catch (Exception e) {
            log.warn("Error saving account detail: {}", e.getMessage());
        }
    }

    /**
     * Save routing detail to database
     */
    private void saveRoutingDetail(VirtualAccount virtualAccount, Map<String, Object> routingData) {
        try {
            RoutingDetail routingDetail = new RoutingDetail();
            routingDetail.setVirtualAccount(virtualAccount);
            routingDetail.setExternalId((String) routingData.get("id"));
            routingDetail.setObject((String) routingData.get("object"));
            routingDetail.setBankName((String) routingData.get("bank_name"));
            routingDetail.setLiveMode((Boolean) routingData.getOrDefault("live_mode", false));
            routingDetail.setPaymentType((String) routingData.get("payment_type"));
            routingDetail.setRoutingNumber((String) routingData.get("routing_number"));
            routingDetail.setRoutingNumberType((String) routingData.get("routing_number_type"));

            if (routingData.get("created_at") != null) {
                routingDetail.setCreatedAt(OffsetDateTime.parse((String) routingData.get("created_at")));
            }
            if (routingData.get("updated_at") != null) {
                routingDetail.setUpdatedAt(OffsetDateTime.parse((String) routingData.get("updated_at")));
            }

            RoutingDetail savedRoutingDetail = routingDetailRepository.save(routingDetail);

            // Save bank address if present
            @SuppressWarnings("unchecked")
            Map<String, Object> bankAddressData = (Map<String, Object>) routingData.get("bank_address");
            if (bankAddressData != null) {
                saveBankAddress(savedRoutingDetail, bankAddressData);
            }

            log.debug("Routing detail saved for virtual account: {}", virtualAccount.getMtId());

        } catch (Exception e) {
            log.warn("Error saving routing detail: {}", e.getMessage());
        }
    }

    /**
     * Save bank address to database
     */
    private void saveBankAddress(RoutingDetail routingDetail, Map<String, Object> addressData) {
        try {
            BankAddress bankAddress = new BankAddress();
            bankAddress.setRoutingDetail(routingDetail);
            bankAddress.setExternalId((String) addressData.get("id"));
            bankAddress.setObject((String) addressData.get("object"));
            bankAddress.setLiveMode((Boolean) addressData.getOrDefault("live_mode", false));
            bankAddress.setLine1((String) addressData.get("line1"));
            bankAddress.setLine2((String) addressData.get("line2"));
            bankAddress.setLocality((String) addressData.get("locality"));
            bankAddress.setRegion((String) addressData.get("region"));
            bankAddress.setPostalCode((String) addressData.get("postal_code"));
            bankAddress.setCountry((String) addressData.get("country"));

            if (addressData.get("created_at") != null) {
                bankAddress.setCreatedAt(OffsetDateTime.parse((String) addressData.get("created_at")));
            }
            if (addressData.get("updated_at") != null) {
                bankAddress.setUpdatedAt(OffsetDateTime.parse((String) addressData.get("updated_at")));
            }

            bankAddressRepository.save(bankAddress);
            log.debug("Bank address saved for routing detail: {}", routingDetail.getExternalId());

        } catch (Exception e) {
            log.warn("Error saving bank address: {}", e.getMessage());
        }
    }

    /**
     * Get counterparty ID by user ID - CORRECTED VERSION
     */
    private String getCounterpartyIdByUserId(Long userId) {
        try {
            // ✅ CORRECTED: Actually look up the counterparty from database
            return counterpartyRepository.findByUser_Id(userId)
                    .map(Counterparty::getMtCounterpartyId)
                    .orElseThrow(() -> new RuntimeException("Counterparty not found for user: " + userId));
        } catch (Exception e) {
            log.error("Error finding counterparty for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Counterparty not found for user: " + userId);
        }
    }

    /**
     * Get counterparty ID by user - Helper method for BankAccountProxyService
     */
    public String getCounterpartyIdByUser(User user) {
        try {
            return counterpartyRepository.findByUser_Id(user.getId())
                    .map(Counterparty::getMtCounterpartyId)
                    .orElseThrow(() -> new RuntimeException("Counterparty not found for user: " + user.getId()));
        } catch (Exception e) {
            log.error("Error finding counterparty for user {}: {}", user.getId(), e.getMessage());
            throw new RuntimeException("Counterparty not found for user: " + user.getId());
        }
    }

    /**
     * Retry mechanism for failed API calls
     */
    private <T> T retryApiCall(java.util.function.Supplier<T> apiCall, int maxRetries) {
        Exception lastException = null;

        for (int i = 0; i < maxRetries; i++) {
            try {
                return apiCall.get();
            } catch (Exception e) {
                lastException = e;
                log.warn("API call failed, attempt {} of {}: {}", i + 1, maxRetries, e.getMessage());

                if (i < maxRetries - 1) {
                    try {
                        Thread.sleep(1000 * (i + 1)); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry", ie);
                    }
                }
            }
        }

        throw new RuntimeException("API call failed after " + maxRetries + " attempts", lastException);
    }

    /**
     * Create ACH payment order via Modern Treasury API
     * PRODUCTION-READY: Full error handling and validation
     */
    public String createPaymentOrder(Map<String, Object> paymentOrderRequest) {
        try {
            log.info("💳 Creating payment order via Modern Treasury");
            log.debug("💳 Request: {}", paymentOrderRequest);

            // Validate required fields
            validatePaymentOrderRequest(paymentOrderRequest);

            String url = apiUrl + "/payment_orders";

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(paymentOrderRequest, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String paymentOrderId = (String) responseBody.get("id");
                String status = (String) responseBody.get("status");

                log.info("✅ Payment order created successfully:");
                log.info("✅ - ID: {}", paymentOrderId);
                log.info("✅ - Status: {}", status);
                log.info("✅ - Amount: ${}", (Long) paymentOrderRequest.get("amount") / 100.0);

                return paymentOrderId;
            } else {
                String errorMsg = "Failed to create payment order: " + response.getStatusCode();
                log.error("❌ {}", errorMsg);
                throw new RuntimeException(errorMsg);
            }

        } catch (HttpClientErrorException e) {
            String errorMsg = "Modern Treasury API error: " + e.getResponseBodyAsString();
            log.error("❌ {}", errorMsg);
            throw new RuntimeException(errorMsg);
        } catch (Exception e) {
            String errorMsg = "Failed to create payment order: " + e.getMessage();
            log.error("❌ {}", errorMsg, e);
            throw new RuntimeException(errorMsg);
        }
    }

    /**
     * Validate payment order request before sending to Modern Treasury
     */
    private void validatePaymentOrderRequest(Map<String, Object> request) {
        String[] requiredFields = {
                "type", "amount", "direction", "currency",
                "originating_account_id", "receiving_account_id"
        };

        for (String field : requiredFields) {
            if (!request.containsKey(field) || request.get(field) == null) {
                throw new IllegalArgumentException("Missing required field: " + field);
            }
        }

        // Validate amount is positive
        Long amount = (Long) request.get("amount");
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive, got: " + amount);
        }

        // Validate currency
        String currency = (String) request.get("currency");
        if (!"USD".equals(currency)) {
            throw new IllegalArgumentException("Only USD currency supported, got: " + currency);
        }

        // Validate direction for ACH debits
        String direction = (String) request.get("direction");
        if (!"debit".equals(direction)) {
            throw new IllegalArgumentException("Expected debit direction, got: " + direction);
        }

        log.debug("✅ Payment order request validation passed");
    }

    /**
     * Get payment order status from Modern Treasury
     */
    public Map<String, Object> getPaymentOrderStatus(String paymentOrderId) {
        try {
            String url = apiUrl + "/payment_orders/" + paymentOrderId;

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to get payment order status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ Error getting payment order status for {}: {}", paymentOrderId, e.getMessage());
            throw new RuntimeException("Failed to get payment order status: " + e.getMessage());
        }
    }

    /**
     * Cancel payment order (if not yet processed)
     */
    public boolean cancelPaymentOrder(String paymentOrderId) {
        try {
            log.info("🚫 Cancelling payment order: {}", paymentOrderId);

            String url = apiUrl + "/payment_orders/" + paymentOrderId + "/cancel";

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Payment order cancelled successfully: {}", paymentOrderId);
                return true;
            } else {
                log.error("❌ Failed to cancel payment order: {}", response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("❌ Error cancelling payment order {}: {}", paymentOrderId, e.getMessage());
            return false;
        }
    }
}
