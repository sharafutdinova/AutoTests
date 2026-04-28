package baseTests;

import common.extensions.UserApiSessionExtension;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(UserApiSessionExtension.class)
public class BaseTest {
    protected SoftAssertions softly;

    @BeforeEach
    public void setUp() {
        softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }
}
