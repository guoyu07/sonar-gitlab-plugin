package com.jsherz.sonargitlab;

import com.synaptix.sonar.plugins.gitlab.MarkDownUtils;
import org.junit.Test;
import org.sonar.api.config.Settings;

import static org.junit.Assert.assertEquals;


public class MarkDownUtilsTest {

    @Test
    public void testSetupUsesServerBaseUrlIfProvided() {
        final Settings settings = new Settings();
        settings.setProperty("sonar.core.serverBaseURL", "http://www.example.com/sonar/url");
        settings.setProperty("sonar.host.url", "http://www.badurl.org");

        final MarkDownUtils markDownUtils = new MarkDownUtils(settings);

        assertEquals("serverBaseURL should override host URL",
                "[:blue_book:](http://www.example.com/sonar/url/coding_rules#rule_key=blah)",
                markDownUtils.getRuleLink("blah"));
    }

    @Test
    public void testSetupUsesSonarHostUrlIfNoServerBaseUrlIfProvided() {
        final Settings settings = new Settings();
        settings.setProperty("sonar.host.url", "http://www.badurl.org");

        final MarkDownUtils markDownUtils = new MarkDownUtils(settings);

        assertEquals("host URL should be used if no serverBaseURL given",
                "[:blue_book:](http://www.badurl.org/coding_rules#rule_key=foobar)",
                markDownUtils.getRuleLink("foobar"));
    }

}
