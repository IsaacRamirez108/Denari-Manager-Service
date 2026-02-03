package com.denari.manager.services;

import com.denari.manager.enums.PaymentType;
import com.denari.manager.models.entity.Payments.PaymentRecord;
import com.denari.manager.models.entity.User.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    // TODO: Inject actual notification providers (email, SMS, push)
    // private final EmailService emailService;
    // private final SmsService smsService;
    // private final PushNotificationService pushService;

    /**
     * Send confirmation when rent payment is received
     */
    public void sendRentReceivedConfirmation(User user, Long rentAmountCents) {
        try {
            log.info("📧 Sending rent received confirmation to user: {}", user.getEmail());

            String subject = "Rent Payment Received - Denari";
            String message = buildRentReceivedMessage(user, rentAmountCents);

            // TODO: Send actual email
            // emailService.sendEmail(user.getEmail(), subject, message);

            log.info("✅ Rent received confirmation sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send rent received confirmation: {}", e.getMessage(), e);
            // Don't throw - notifications are not critical for core business logic
        }
    }

    /**
     * Send notification when ACH payment succeeds
     */
    public void sendPaymentSuccessNotification(User user, PaymentRecord payment) {
        try {
            log.info("📧 Sending payment success notification to user: {}", user.getEmail());

            String subject = getPaymentSuccessSubject(payment.getPaymentType());
            String message = buildPaymentSuccessMessage(user, payment);

            // TODO: Send actual email
            // emailService.sendEmail(user.getEmail(), subject, message);

            // TODO: Send SMS for important payments
            // if (payment.getPaymentType() == PaymentRecord.PaymentType.SECOND_PAYMENT) {
            //     smsService.sendSms(user.getPhoneNumber(), buildPaymentSuccessSms(payment));
            // }

            log.info("✅ Payment success notification sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send payment success notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send notification when ACH payment fails
     */
    public void sendPaymentFailureNotification(User user, PaymentRecord payment, String reason) {
        try {
            log.info("📧 Sending payment failure notification to user: {}", user.getEmail());

            String subject = "Payment Failed - Action Required - Denari";
            String message = buildPaymentFailureMessage(user, payment, reason);

            // TODO: Send actual email with high priority
            // emailService.sendHighPriorityEmail(user.getEmail(), subject, message);

            // TODO: Send SMS for critical failures
            // String smsMessage = buildPaymentFailureSms(payment, reason);
            // smsService.sendSms(user.getPhoneNumber(), smsMessage);

            // TODO: Send push notification if mobile app exists
            // pushService.sendPush(user.getId(), "Payment Failed", "Your rent payment failed. Please check your account.");

            log.info("✅ Payment failure notification sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send payment failure notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send notification when payment is returned by bank
     */
    public void sendPaymentReturnedNotification(User user, PaymentRecord payment, String reason) {
        try {
            log.info("📧 Sending payment returned notification to user: {}", user.getEmail());

            String subject = "Payment Returned - Denari";
            String message = buildPaymentReturnedMessage(user, payment, reason);

            // TODO: Send actual email
            // emailService.sendEmail(user.getEmail(), subject, message);

            log.info("✅ Payment returned notification sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send payment returned notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send notification when rent payment is returned by property manager
     */
    public void sendPaymentReturnedNotification(User user, String reason) {
        try {
            log.info("📧 Sending rent payment returned notification to user: {}", user.getEmail());

            String subject = "Rent Payment Returned - Denari";
            String message = buildRentReturnedMessage(user, reason);

            // TODO: Send actual email
            // emailService.sendEmail(user.getEmail(), subject, message);

            log.info("✅ Rent payment returned notification sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send rent payment returned notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send notification when full rent cycle is completed
     */
    public void sendRentCycleCompletedNotification(User user, String rentMonth) {
        try {
            log.info("📧 Sending rent cycle completed notification to user: {}", user.getEmail());

            String subject = "Rent Payments Complete - " + rentMonth + " - Denari";
            String message = buildRentCycleCompletedMessage(user, rentMonth);

            // TODO: Send actual email
            // emailService.sendEmail(user.getEmail(), subject, message);

            log.info("✅ Rent cycle completed notification sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send rent cycle completed notification: {}", e.getMessage(), e);
        }
    }

    // ============== MESSAGE BUILDERS ==============

    private String buildRentReceivedMessage(User user, Long rentAmountCents) {
        return String.format("""
            Hi %s,
            
            Great news! We've received your rent payment of $%.2f and have scheduled your split payments:
            
            • First Payment (50%%): Will be charged on the 1st
            • Second Payment (50%% + $15 service fee): Will be charged on your selected date
            
            You'll receive confirmations when each payment is processed.
            
            Questions? Reply to this email or contact support@denari.com
            
            Best regards,
            The Denari Team
            """,
                user.getFirstName(),
                rentAmountCents / 100.0);
    }

    private String buildPaymentSuccessMessage(User user, PaymentRecord payment) {
        String paymentDescription = payment.getPaymentType() == PaymentType.FIRST_PAYMENT
                ? "first rent payment (50%)"
                : "second rent payment (50% + $15 service fee)";

        return String.format("""
            Hi %s,
            
            Your %s of $%.2f has been successfully processed.
            
            Payment Details:
            • Amount: $%.2f
            • Date: %s
            • Type: %s
            
            Your account is up to date. Thank you for using Denari!
            
            Best regards,
            The Denari Team
            """,
                user.getFirstName(),
                paymentDescription,
                payment.getAmount(),
                payment.getAmount(),
                payment.getEffectiveDate(),
                payment.getPaymentType());
    }

    private String buildPaymentFailureMessage(User user, PaymentRecord payment, String reason) {
        return String.format("""
            Hi %s,
            
            We encountered an issue processing your rent payment:
            
            Payment Details:
            • Amount: $%.2f
            • Type: %s
            • Reason: %s
            
            What's Next:
            %s
            
            If you need assistance, please contact us at support@denari.com or log into your account.
            
            Best regards,
            The Denari Team
            """,
                user.getFirstName(),
                payment.getAmount(),
                payment.getPaymentType(),
                reason,
                getFailureNextSteps(reason));
    }

    private String buildPaymentReturnedMessage(User user, PaymentRecord payment, String reason) {
        return String.format("""
            Hi %s,
            
            Your payment of $%.2f was returned by your bank:
            
            Return Reason: %s
            
            This means the payment was initially processed but then returned. We'll be reaching out to help resolve this.
            
            Please contact support@denari.com if you have any questions.
            
            Best regards,
            The Denari Team
            """,
                user.getFirstName(),
                payment.getAmount(),
                reason);
    }

    private String buildRentReturnedMessage(User user, String reason) {
        return String.format("""
            Hi %s,
            
            Your rent payment was returned by your property manager:
            
            Return Reason: %s
            
            We've cancelled any scheduled payments related to this rent payment. Please contact your property manager to resolve this issue.
            
            Questions? Contact us at support@denari.com
            
            Best regards,
            The Denari Team
            """,
                user.getFirstName(),
                reason);
    }

    private String buildRentCycleCompletedMessage(User user, String rentMonth) {
        return String.format("""
            Hi %s,
            
            🎉 Great news! Your rent payments for %s are complete.
            
            Both of your split payments have been successfully processed:
            • First payment (50%%) ✅
            • Second payment (50%% + service fee) ✅
            
            Your rent is fully paid and your account is up to date.
            
            Keep building your credit history with Denari!
            
            Best regards,
            The Denari Team
            """,
                user.getFirstName(),
                rentMonth);
    }

    // ============== HELPER METHODS ==============

    private String getPaymentSuccessSubject(PaymentType paymentType) {
        return paymentType == PaymentType.FIRST_PAYMENT
                ? "First Rent Payment Processed - Denari"
                : "Second Rent Payment Processed - Denari";
    }

    private String getFailureNextSteps(String reason) {
        if (reason.toLowerCase().contains("insufficient")) {
            return "• Please ensure sufficient funds are available in your account\n• We'll retry this payment in 3 business days";
        } else if (reason.toLowerCase().contains("invalid") || reason.toLowerCase().contains("closed")) {
            return "• Please update your bank account information in your Denari account\n• Contact support if you need assistance";
        } else {
            return "• We'll investigate this issue and may retry the payment\n• Please ensure your account information is current";
        }
    }

    // TODO: Implement these SMS methods when SMS service is ready
    private String buildPaymentSuccessSms(PaymentRecord payment) {
        return String.format("Denari: Your $%.2f rent payment was processed successfully. Account up to date!",
                payment.getAmount());
    }

    private String buildPaymentFailureSms(PaymentRecord payment, String reason) {
        return String.format("Denari: Your $%.2f payment failed (%s). Please check your account. Support: support@denari.com",
                payment.getAmount(), reason);
    }
}
