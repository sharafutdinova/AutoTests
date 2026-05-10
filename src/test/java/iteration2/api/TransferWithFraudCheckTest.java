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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Method;

@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
public class TransferWithFraudCheckTest extends BaseTest {
    private UserSteps userSteps1;
    private CreateAccountResponse senderAccount;
    private CreateAccountResponse receiverAccount;
    private double depositAmount;
    private FraudCheckMock fraudCheckMock;

    public void prepareAccounts() {
        userSteps1 = SessionStorage.getSteps();
        senderAccount = userSteps1.createAccount();
        SessionStorage.addUser(AdminSteps.createUser());
        int secondUserId = 1;
        receiverAccount = SessionStorage.getSteps(secondUserId).createAccount();
        depositAmount = RandomData.getDepositAmount();
        userSteps1.depositToAccount(senderAccount.getId(), depositAmount);
    }

    public void getAnnotation(TestInfo testInfo) {
        Method method = testInfo.getTestMethod().orElseThrow();
        fraudCheckMock = method.getAnnotation(FraudCheckMock.class);
    }

    @BeforeEach
    public void setUpTest(TestInfo testInfo) {
        prepareAccounts();
        getAnnotation(testInfo);
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
        double transferAmount = RandomData.getRandomAmount(depositAmount);
        TransferResponseForFraud transferResponse = userSteps1.transferWithFraudCheck(
                senderAccount.getId(),
                receiverAccount.getId(),
                transferAmount
        );

        // Assert that POST returned success using model comparison
        softly.assertThat(transferResponse).isNotNull();

        // Create expected response model for comparison
        // Using the same values as configured in @FraudCheckMock annotation
        TransferResponseForFraud expectedResponse = TransferResponseForFraud.builder()
                .status(TransferStatus.APPROVED.getDescription())
                .message(TransferMessage.TRANSFER_APPROVED_AND_PROCESSED_IMMEDIATELY.getDescription())
                .amount(transferAmount)
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .fraudRiskScore(Double.parseDouble(fraudCheckMock.riskScore()))
                .fraudReason(fraudCheckMock.reason())
                .requiresManualReview(fraudCheckMock.requiresManualReview())
                .requiresVerification(fraudCheckMock.additionalVerificationRequired())
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
        double transferAmount = depositAmount;
        TransferResponseForFraud transferResponse = userSteps1.transferWithFraudCheck(
                senderAccount.getId(),
                receiverAccount.getId(),
                transferAmount
        );
        softly.assertThat(transferResponse).isNotNull();
        TransferResponseForFraud expectedResponse = TransferResponseForFraud.builder()
                .status(TransferStatus.VERIFICATION_REQUIRED.getDescription())
                .message(TransferMessage.ADDITIONAL_VERIFICATION_REQUIRED.getDescription())
                .amount(transferAmount)
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .fraudRiskScore(Double.parseDouble(fraudCheckMock.riskScore()))
                .fraudReason(fraudCheckMock.reason())
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
        double transferAmount = depositAmount;
        TransferResponseForFraud transferResponse = userSteps1.transferWithFraudCheck(
                senderAccount.getId(),
                receiverAccount.getId(),
                transferAmount
        );
        softly.assertThat(transferResponse).isNotNull();
        TransferResponseForFraud expectedResponse = TransferResponseForFraud.builder()
                .status(TransferStatus.MANUAL_REVIEW_REQUIRED.getDescription())
                .message(TransferMessage.TRANSFER_REQUIRES_MANUAL_REVIEW.getDescription())
                .amount(transferAmount)
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .fraudRiskScore(Double.parseDouble(fraudCheckMock.riskScore()))
                .fraudReason(fraudCheckMock.reason())
                .requiresManualReview(fraudCheckMock.requiresManualReview())
                .requiresVerification(fraudCheckMock.additionalVerificationRequired())
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
                .status(TransferStatus.VERIFICATION_REQUIRED.getDescription())
                .message(TransferMessage.ADDITIONAL_VERIFICATION_REQUIRED.getDescription())
                .amount(transferAmount)
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .fraudRiskScore(Double.parseDouble(fraudCheckMock.riskScore()))
                .fraudReason(fraudCheckMock.reason())
                .requiresManualReview(fraudCheckMock.requiresManualReview())
                .requiresVerification(fraudCheckMock.additionalVerificationRequired())
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
                .status(TransferStatus.MANUAL_REVIEW_REQUIRED.getDescription())
                .message(TransferMessage.TRANSFER_REQUIRES_MANUAL_REVIEW.getDescription())
                .amount(transferAmount)
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .fraudRiskScore(Double.parseDouble(fraudCheckMock.riskScore()))
                .fraudReason(fraudCheckMock.reason())
                .requiresManualReview(fraudCheckMock.requiresManualReview())
                .requiresVerification(fraudCheckMock.additionalVerificationRequired())
                .build();
        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    @Test
    @FraudCheckMock(
            statusCode = 500,
            status = "ERROR",
            decision = "ERROR",
            riskScore = "1",
            reason = "undefined",
            requiresManualReview = true,
            additionalVerificationRequired = true
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
        String fraudReason = String.format(FraudReason.UNEXPECTED_ERROR.getDescription(), FraudReason.SERVER_ERROR.getDescription(), fraudCheckMock.status(), fraudCheckMock.decision(),
                fraudCheckMock.riskScore(), fraudCheckMock.reason(), fraudCheckMock.requiresManualReview(), fraudCheckMock.additionalVerificationRequired());
        TransferResponseForFraud expectedResponse = TransferResponseForFraud.builder()
                .status(TransferStatus.MANUAL_REVIEW_REQUIRED.getDescription())
                .message(TransferMessage.TRANSFER_REQUIRES_MANUAL_REVIEW.getDescription())
                .amount(transferAmount)
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .fraudRiskScore(Double.parseDouble(fraudCheckMock.riskScore()))
                .fraudReason(fraudReason)
                .requiresManualReview(fraudCheckMock.requiresManualReview())
                .requiresVerification(fraudCheckMock.additionalVerificationRequired())
                .build();
        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    @Test
    @FraudCheckMock(
            statusCode = 400,
            status = "ERROR",
            decision = "ERROR",
            riskScore = "0.5",
            reason = "undefined",
            requiresManualReview = true,
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
        String fraudReason = String.format(FraudReason.UNEXPECTED_ERROR.getDescription(), FraudReason.BAD_REQUEST.getDescription(), fraudCheckMock.status(), fraudCheckMock.decision(),
                fraudCheckMock.riskScore(), fraudCheckMock.reason(), fraudCheckMock.requiresManualReview(), fraudCheckMock.additionalVerificationRequired());
        TransferResponseForFraud expectedResponse = TransferResponseForFraud.builder()
                .status(TransferStatus.MANUAL_REVIEW_REQUIRED.getDescription())
                .message(TransferMessage.TRANSFER_REQUIRES_MANUAL_REVIEW.getDescription())
                .amount(transferAmount)
                .senderAccountId(senderAccount.getId())
                .receiverAccountId(receiverAccount.getId())
                .fraudRiskScore(Double.parseDouble(fraudCheckMock.riskScore()))
                .fraudReason(fraudReason)
                .requiresManualReview(fraudCheckMock.requiresManualReview())
                .requiresVerification(fraudCheckMock.additionalVerificationRequired())
                .build();
        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }
}
