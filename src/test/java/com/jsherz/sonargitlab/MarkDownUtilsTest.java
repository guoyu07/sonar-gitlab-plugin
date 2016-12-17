package com.jsherz.sonargitlab;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.synaptix.sonar.plugins.gitlab.MarkDownUtils;
import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.api.rule.Severity;

import java.util.List;
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

    @Test
    public void testKnownSeverityReturnsTheCorrectEmoji() {
        final Map<String, String> examples = ImmutableMap.of(
                Severity.BLOCKER, ":no_entry:",
                Severity.CRITICAL, ":no_entry_sign:",
                Severity.MAJOR, ":warning:",
                Severity.MINOR, ":arrow_down_small:",
                Severity.INFO, ":information_source:"
        );

        examples.forEach((example, expected) ->
                assertThat(MarkDownUtils.getEmojiForSeverity(example))
                        .isEqualTo(expected));
    }

    @Test
    public void testUnknownSeverityReturnsQuestionMarkEmoji() {
        final List<String> examples = ImmutableList.of(
                "hummus", "Shoreditch", "beard", "origin"
        );

        examples.forEach((example) ->
                assertThat(MarkDownUtils.getEmojiForSeverity(example))
                        .isEqualTo(":grey_question:"));
    }

    @Test
    public void testBuildsInlineIssueMarkdownCorrectly() {
        final Settings settings = new Settings();
        settings.setProperty("sonar.core.serverBaseURL", "http://www.jsherz.com/40404");

        final MarkDownUtils markDownUtils = new MarkDownUtils(settings);

        assertThat(markDownUtils.inlineIssue(Severity.INFO, "Your code is bad.", "bad_code"))
                .isEqualTo(":information_source: Your code is bad. [:blue_book:](http://www.jsherz.com/40404/coding_rules#rule_key=bad_code)");
    }

    @Test
    public void testBuildsCorrectRuleLink() {
        final Settings settings = new Settings();
        settings.setProperty("sonar.core.serverBaseURL", "http://www.sonarz.io/kewl-sonar");

        final MarkDownUtils markDownUtils = new MarkDownUtils(settings);

        assertThat(markDownUtils.getRuleLink("rule$_r0ck"))
                .isEqualTo("[:blue_book:](http://www.sonarz.io/kewl-sonar/coding_rules#rule_key=rule%24_r0ck)");
    }

    @Test
    public void testThrowsIllegalArgsIfRuleLinkKeyEmpty() {
        final Settings settings = new Settings();
        settings.setProperty("sonar.core.serverBaseURL", "http://www.ilovebourbons.org/");

        final MarkDownUtils markDownUtils = new MarkDownUtils(settings);

        assertThatThrownBy(() -> markDownUtils.getRuleLink(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ruleKey must not be null");
    }

}
