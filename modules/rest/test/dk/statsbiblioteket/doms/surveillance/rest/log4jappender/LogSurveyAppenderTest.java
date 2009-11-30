package dk.statsbiblioteket.doms.surveillance.rest.log4jappender;

import dk.statsbiblioteket.doms.surveillance.rest.StatusMessage;
import junit.framework.TestCase;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/** Test the Log4JAppender */
public class LogSurveyAppenderTest extends TestCase {
    public void testAppend() {
        Logger logger = Logger.getLogger(LogSurveyAppenderTest.class);
        logger.log(Level.WARN, "Oh noes!");
        LogSurvey ls = LogSurveyFactory.getLogSurvey();
        assertEquals(
                "Should have one logged message", 1,
                ls.getStatus().getMessages().size());
        assertTrue(
                "Should be a log message",
                ls.getStatus().getMessages().get(0).isLogMessage());
        assertTrue(
                "Should contain the log statement",
                ls.getStatus().getMessages().get(0).getMessage().contains(
                        "Oh noes!"));
        assertEquals(
                "Should be yellow", StatusMessage.Severity.YELLOW,
                ls.getStatus().getMessages().get(0).getSeverity());
    }
}
