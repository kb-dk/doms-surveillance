package dk.statsbiblioteket.doms.surveillance.rest.log4jappender;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import dk.statsbiblioteket.doms.surveillance.rest.StatusMessage;

/** Test the Log4JAppender */
public class LogSurveyAppenderTest extends TestCase {
    public void testAppend() {
        Logger logger = Logger.getLogger(LogSurveyAppenderTest.class);
        logger.log(Priority.WARN, "Oh noes!");
        LogSurvey ls = LogSurveyFactory.getLogSurvey();
        assertEquals("Should have one logged message", 1, ls.getMessages().getMessages().size());
        assertTrue("Should be a log message", ls.getMessages().getMessages().get(0).isLogMessage());
        assertTrue("Should contain the log statement", ls.getMessages().getMessages().get(0).getMessage().contains("Oh noes!"));
        assertEquals("Should be yellow", StatusMessage.Severity.YELLOW, ls.getMessages().getMessages().get(0).getSeverity());
    }
}
