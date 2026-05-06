package common.extensions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
  private Map<String, Long> startTimes = new HashMap<>();

  @Override
  public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
    String testName =
        extensionContext.getRequiredTestClass().getPackageName()
            + "."
            + extensionContext.getDisplayName();
    startTimes.put(testName, System.currentTimeMillis());
    String info = "Thread " + Thread.currentThread().getName() + ": Test started " + testName;
    appendToFile("reports/result.txt", info);
  }

  @Override
  public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
    String testName =
        extensionContext.getRequiredTestClass().getPackageName()
            + "."
            + extensionContext.getDisplayName();
    Long testDuration = System.currentTimeMillis() - startTimes.get(testName);
    String info =
        "Thread "
            + Thread.currentThread().getName()
            + ": Test finished "
            + testName
            + ", test duration "
            + testDuration
            + " ms";
    String duration = testName + " test duration " + testDuration + " ms";
    appendToFile("reports/result.txt", info);
    appendToFile("reports/durations.txt", duration);
  }

  public synchronized void appendToFile(String fileName, String text) throws IOException {
    try (FileWriter fw = new FileWriter(fileName, true);
        BufferedWriter bw = new BufferedWriter(fw)) {
      bw.write(text);
      bw.newLine();
    }
  }
}
