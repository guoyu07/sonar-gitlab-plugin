package com.jsherz.sonargitlab;

import com.google.common.collect.ImmutableMap;
import com.synaptix.sonar.plugins.gitlab.MarkDownUtils;
import org.junit.Test;
import org.sonar.api.config.Settings;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Test
    public void testSetupThrowsIllegalArgsIfSettingIsNull() {
        assertThatThrownBy(() -> new MarkDownUtils(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("settings must not be null");
    }

    @Test
    public void testThrowsIllegalArgsIfNoBaseUrlProvided() {
        final Settings settings = new Settings();

        assertThatThrownBy(() -> new MarkDownUtils(settings))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A base URL must be provided with the setting sonar.core.serverBaseURL or sonar.host.url");
    }

    @Test
    public void testUrlEncodingWorksCorrectly() {
        final Map<String, String> examples = ImmutableMap.of(
                "£", "%C2%A3",
                "யாமறிந்த", "%E0%AE%AF%E0%AE%BE%E0%AE%AE%E0%AE%B1%E0%AE%BF%E0%AE%A8%E0%AF%8D%E0%AE%A4",
                "turtle_neck_jeans", "turtle_neck_jeans"
        );

        examples.forEach((before, expectedAfter) ->
                assertThat(MarkDownUtils.encodeForUrl(before))
                        .isEqualTo(expectedAfter));
    }

}
