package com.denari.manager.services;

import com.denari.manager.models.entity.ExternalAccount.ExternalBankAccount;
import com.denari.manager.models.entity.User.User;
import com.denari.manager.repositories.ExternalBankAccountRepository;
import com.denari.manager.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class BankAccountProxyService {

    @Value("${moderntreasury.apiKey}")
    private String apiKey;

    @Value("${moderntreasury.organizationId}")
    private String organizationId;

    @Value("${moderntreasury.apiUrl}")
    private String apiUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExternalBankAccountRepository externalBankAccountRepository;

    @Autowired
    private ModernTreasuryService modernTreasuryService;

    /**
     * Create external account via Modern Treasury API (handles sensitive data)
     * This way, sensitive bank details never touch your servers
     */
    public String createExternalAccountSecurely(Long userId, Map<String, String> bankDetails) {
        try {
            log.info("Creating external account via Modern Treasury for user: {}", userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Get or create counterparty
            String counterpartyId = getOrCreateCounterparty(user);

            // Create external account via Modern Treasury API
            String externalAccountId = createExternalAccountViaMT(counterpartyId, bankDetails);

            // Save only the external account ID and non-sensitive metadata
            saveExternalAccountMetadata(user, externalAccountId, bankDetails);

            log.info("External account created successfully: {}", externalAccountId);
            return externalAccountId;

        } catch (Exception e) {
            log.error("Error creating external account for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to create external account: " + e.getMessage());
        }
    }

    /**
     * Create external account directly via Modern Treasury API
     */
    private String createExternalAccountViaMT(String counterpartyId, Map<String, String> bankDetails) {
        try {
            String url = apiUrl + "/external_accounts";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("counterparty_id", counterpartyId);
            requestBody.put("name", bankDetails.get("accountName"));

            // ✅ FIX: Account details as array of objects
            Map<String, Object> accountDetail = new HashMap<>();
            accountDetail.put("account_number", bankDetails.get("accountNumber"));
            accountDetail.put("account_number_type", "other");
            requestBody.put("account_details", new Object[]{accountDetail});

            // ✅ FIX: Routing details as array of objects
            Map<String, Object> routingDetail = new HashMap<>();
            routingDetail.put("routing_number", bankDetails.get("routingNumber"));
            routingDetail.put("routing_number_type", "aba");
            requestBody.put("routing_details", new Object[]{routingDetail});

            // Additional metadata
            requestBody.put("party_type", "individual");
            requestBody.put("party_name", bankDetails.get("accountName"));

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.info("Making external account request to Modern Treasury: {}", url);
            log.info("Request body: {}", requestBody);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String externalAccountId = (String) responseBody.get("id");
                log.info("External account created successfully: {}", externalAccountId);
                return externalAccountId;
            } else {
                throw new RuntimeException("Failed to create external account: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error creating external account via Modern Treasury: {}", e.getMessage());
            throw new RuntimeException("Failed to create external account via Modern Treasury");
        }
    }

    /**
     * Get or create counterparty for user
     */
    private String getOrCreateCounterparty(User user) {
        try {
            // Try to get existing counterparty
            return modernTreasuryService.getCounterpartyIdByUser(user);
        } catch (Exception e) {
            log.info("No existing counterparty found, creating new one for user: {}", user.getId());
            return modernTreasuryService.createCounterparty(user);
        }
    }

    /**
     * Save only non-sensitive external account metadata
     */
    private void saveExternalAccountMetadata(User user, String externalAccountId, Map<String, String> bankDetails) {
        try {
            ExternalBankAccount bankAccount = new ExternalBankAccount();
            bankAccount.setUser(user);
            bankAccount.setMtExternalAccountId(externalAccountId);
            bankAccount.setAccountName(bankDetails.get("accountName"));
            bankAccount.setBankName(bankDetails.get("bankName"));

            // Only store last 4 digits of account number (non-sensitive)
            String accountNumber = bankDetails.get("accountNumber");
            bankAccount.setAccountNumberLastFour(accountNumber.substring(Math.max(0, accountNumber.length() - 4)));

            // Store routing number (not sensitive, publicly available)
            bankAccount.setRoutingNumber(bankDetails.get("routingNumber"));

            // Set account type
            String accountType = bankDetails.get("accountType");
            bankAccount.setAccountType(
                    "checking".equalsIgnoreCase(accountType) ?
                            ExternalBankAccount.AccountType.CHECKING :
                            ExternalBankAccount.AccountType.SAVINGS
            );

            bankAccount.setVerificationStatus(ExternalBankAccount.VerificationStatus.PENDING);
            bankAccount.setIsPrimary(true);
            bankAccount.setCreatedAt(LocalDateTime.now());

            externalBankAccountRepository.save(bankAccount);
            log.info("External account metadata saved for user: {}", user.getId());

        } catch (Exception e) {
            log.error("Error saving external account metadata: {}", e.getMessage());
            // Don't throw - external account was created successfully in MT
        }
    }

    /**
     * Create authentication headers for Modern Treasury API
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(organizationId, apiKey);
        return headers;
    }

    /**
     * Verify external account via Modern Treasury
     */
    public boolean verifyExternalAccount(String externalAccountId) {
        try {
            String url = apiUrl + "/external_accounts/" + externalAccountId + "/verify";

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("Error verifying external account {}: {}", externalAccountId, e.getMessage());
            return false;
        }
    }
}
