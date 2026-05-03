package iteration2.api;

import api.generators.RandomData;
import api.models.accounts.*;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import baseTests.BaseTest;
import common.annotations.FraudCheckMock;
import common.annotations.UserApiSession;
import common.extensions.FraudCheckWireMockExtension;
import common.extensions.TimingExtension;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
@DisabledIfSystemProperty(named = "withMock", matches = "false")
public class TransferWithFraudCheckTest extends BaseTest {
    private UserSteps userSteps1;
    private CreateAccountResponse senderAccount;
    private CreateAccountResponse receiverAccount;
    private double depositAmount;

    public void prepareAccounts() {
        userSteps1 = SessionStorage.getSteps();
        senderAccount = userSteps1.createAccount();
        SessionStorage.addUser(AdminSteps.createUser());
        int secondUserId = 1;
        receiverAccount = SessionStorage.getSteps(secondUserId).createAccount();
        depositAmount = RandomData.getDepositAmount();
        userSteps1.depositToAccount(senderAccount.getId(), depositAmount);
    }

    @Test
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = "0.2",
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    @UserApiSession
    public void testTransferRiskFraudCheckWithSuccessStatus() {
        prepareAccounts();
        // Step 6: Send POST to transfer-with-fraud-check endpoint
        // Transfer random amount (less than deposit) from account1 to account2
        double transferAmount = RandomData.getRandomAmount(depositAmount);
        TransferResponseForFraud transferResponse = userSteps1.transferWithFraudCheck(
                senderAccount.getId(),
                receiverAccount.getId(),
                transferAmount
        );

        // Step 7: Assert that POST returned success using model comparison
        softly.assertThat(transferResponse).isNotNull();

        // Create expected response model for comparison
        // Using the same values as configured in @FraudCheckMock annotation
        TransferResponseForFraud expectedResponse = TransferResponseForFraud.builder()
                .status("APPROVED")
                .message("Transfer approved and processed immediately")
                .amount(transferAmount)
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .fraudRiskScore(0.2)
                .fraudReason("Low risk transaction")
                .requiresManualReview(false)
                .requiresVerification(false)
                .build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    @Test
    @FraudCheckMock(
            status = "FAILED",
            decision = "REJECTED",
            riskScore = "0.8",
            reason = "High risk transaction",
            requiresManualReview = true,
            additionalVerificationRequired = true
    )
    @UserApiSession
    public void testTransferWithFraudCheckVerificationsAndReviewRequired() {
        prepareAccounts();
        double transferAmount = depositAmount;
        TransferResponseForFraud transferResponse = userSteps1.transferWithFraudCheck(
                senderAccount.getId(),
                receiverAccount.getId(),
                transferAmount
        );
        softly.assertThat(transferResponse).isNotNull();
        TransferResponseForFraud expectedResponse = TransferResponseForFraud.builder()
                .status("VERIFICATION_REQUIRED")
                .message("Additional verification required")
                .amount(transferAmount)
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .fraudRiskScore(0.8)
                .fraudReason("High risk transaction")
                .requiresManualReview(true)
                .requiresVerification(true)
                .build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    @Test
    @FraudCheckMock(
            status = "FAILED",
            decision = "REJECTED",
            riskScore = "0.5",
            reason = "Middle risk transaction",
            requiresManualReview = true,
            additionalVerificationRequired = false
    )
    @UserApiSession
    public void testTransferWithFraudCheckRequiredManualReview() {
        prepareAccounts();
        double transferAmount = depositAmount;
        TransferResponseForFraud transferResponse = userSteps1.transferWithFraudCheck(
                senderAccount.getId(),
                receiverAccount.getId(),
                transferAmount
        );
        softly.assertThat(transferResponse).isNotNull();
        TransferResponseForFraud expectedResponse = TransferResponseForFraud.builder()
                .status("MANUAL_REVIEW_REQUIRED")
                .message("Transfer requires manual review")
                .amount(transferAmount)
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .fraudRiskScore(0.5)
                .fraudReason("Middle risk transaction")
                .requiresManualReview(true)
                .requiresVerification(false)
                .build();
        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    @Test
    @FraudCheckMock(
            status = "FAILED",
            decision = "REJECTED",
            riskScore = "100.5",
            reason = "High risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = true
    )
    @UserApiSession
    public void testTransferWithFraudCheckVerificationRequired() {
        prepareAccounts();
        double transferAmount = depositAmount;
        TransferResponseForFraud transferResponse = userSteps1.transferWithFraudCheck(
                senderAccount.getId(),
                receiverAccount.getId(),
                transferAmount
        );
        softly.assertThat(transferResponse).isNotNull();
        TransferResponseForFraud expectedResponse = TransferResponseForFraud.builder()
                .status("VERIFICATION_REQUIRED")
                .message("Additional verification required")
                .amount(transferAmount)
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .fraudRiskScore(100.5)
                .fraudReason("High risk transaction")
                .requiresManualReview(false)
                .requiresVerification(true)
                .build();
        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    @Test
    @FraudCheckMock(
            status = "OK",
            decision = "OK",
            riskScore = "0",
            reason = "No risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    @UserApiSession
    public void testTransferWithFraudCheckWithRiskScoreZero() {
        prepareAccounts();
        double transferAmount = depositAmount;
        TransferResponseForFraud transferResponse = userSteps1.transferWithFraudCheck(
                senderAccount.getId(),
                receiverAccount.getId(),
                transferAmount
        );
        softly.assertThat(transferResponse).isNotNull();
        TransferResponseForFraud expectedResponse = TransferResponseForFraud.builder()
                .status("MANUAL_REVIEW_REQUIRED")
                .message("Transfer requires manual review")
                .amount(transferAmount)
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .fraudRiskScore(0.0)
                .fraudReason("No risk transaction")
                .requiresManualReview(false)
                .requiresVerification(false)
                .build();
        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    @Test
    @FraudCheckMock(
            statusCode = 500,
            status = "ERROR",
            decision = "ERROR",
            riskScore = "0",
            reason = "undefined",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )

    @UserApiSession
    public void testTransferWithFraudCheckWithServerError() {
        prepareAccounts();
        double transferAmount = depositAmount;
        TransferResponseForFraud transferResponse = userSteps1.transferWithFraudCheck(
                senderAccount.getId(),
                receiverAccount.getId(),
                transferAmount
        );
        softly.assertThat(transferResponse).isNotNull();
        String fraudReason = "Unexpected error during fraud check: 500 Server Error: \"{<EOL>  \"status\": \"ERROR\",<EOL>  \"decision\": \"ERROR\",<EOL>  \"riskScore\": \"0\",<EOL>  \"reason\": \"undefined\",<EOL>  \"requiresManualReview\": false,<EOL>  \"additionalVerificationRequired\": false<EOL>}\"";
        TransferResponseForFraud expectedResponse = TransferResponseForFraud.builder()
                .status("MANUAL_REVIEW_REQUIRED")
                .message("Transfer requires manual review")
                .amount(transferAmount)
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .fraudRiskScore(0.5)
                .fraudReason(fraudReason)
                .requiresManualReview(true)
                .requiresVerification(false)
                .build();
        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    @Test
    @FraudCheckMock(
            statusCode = 400,
            status = "ERROR",
            decision = "ERROR",
            riskScore = "0",
            reason = "undefined",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )

    @UserApiSession
    public void testTransferWithFraudCheckWithBadRequest() {
        prepareAccounts();
        double transferAmount = depositAmount;
        TransferResponseForFraud transferResponse = userSteps1.transferWithFraudCheck(
                senderAccount.getId(),
                receiverAccount.getId(),
                transferAmount
        );
        softly.assertThat(transferResponse).isNotNull();
        String fraudReason = "Unexpected error during fraud check: 400 Bad Request: \"{<EOL>  \"status\": \"ERROR\",<EOL>  \"decision\": \"ERROR\",<EOL>  \"riskScore\": \"0\",<EOL>  \"reason\": \"undefined\",<EOL>  \"requiresManualReview\": false,<EOL>  \"additionalVerificationRequired\": false<EOL>}\"";
        TransferResponseForFraud expectedResponse = TransferResponseForFraud.builder()
                .status("MANUAL_REVIEW_REQUIRED")
                .message("Transfer requires manual review")
                .amount(transferAmount)
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .fraudRiskScore(0.5)
                .fraudReason(fraudReason)
                .requiresManualReview(true)
                .requiresVerification(false)
                .build();
        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }
}
