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
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
public class TransferWithFraudCheckTest extends BaseTest {
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
    public void testTransferWithFraudCheck() {
        UserSteps userSteps1 = SessionStorage.getSteps();
        CreateAccountResponse senderAccount = userSteps1.createAccount();

        double depositAmount = RandomData.getDepositAmount();
        System.out.println("💰 Random deposit amount: " + depositAmount);
        userSteps1.depositToAccount(senderAccount.getId(), depositAmount);

        SessionStorage.addUser(AdminSteps.createUser());
        int secondUserId = 1;
        CreateAccountResponse receiverAccount = SessionStorage.getSteps(secondUserId).createAccount();
        // Step 6: Send POST to transfer-with-fraud-check endpoint
        // Transfer random amount (less than deposit) from account1 to account2
        double transferAmount = RandomData.getRandomAmount(depositAmount);
        System.out.println("💸 Random transfer amount: " + transferAmount);
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
}
