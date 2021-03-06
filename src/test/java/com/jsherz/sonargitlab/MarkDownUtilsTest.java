/*
 * SonarQube :: GitLab Plugin
 * Copyright (C) 2016-2016 James Sherwood-Jones
 * james.sherwoodjones@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
    public void testSetupThrowsExceptionIfSettingIsNull() {
        assertThatThrownBy(() -> new MarkDownUtils(null))
                .isInstanceOf(NullPointerException.class)
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
        final MarkDownUtils markDownUtils = buildUtilsWithBaseUrl("http://www.jsherz.com/40404");

        assertThat(markDownUtils.inlineIssue(Severity.INFO, "Your code is bad.", "bad_code"))
                .isEqualTo(":information_source: Your code is bad. [:blue_book:](http://www.jsherz.com/40404/coding_rules#rule_key=bad_code)");
    }

    @Test
    public void testBuildsInlineThrowsExceptionIfParamNull() {
        final MarkDownUtils markDownUtils = buildUtilsWithBaseUrl("http://imagine.astory.com/SonarQube");

        assertThatThrownBy(() -> markDownUtils.inlineIssue(null, "Exception hidden. HIDDEN! :O", "exception_hider"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("severity must not be null");

        assertThatThrownBy(() -> markDownUtils.inlineIssue(Severity.CRITICAL, null, "exception_hider"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("message must not be null");

        assertThatThrownBy(() -> markDownUtils.inlineIssue(Severity.BLOCKER, "Exception hidden. HIDDEN! :O", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("ruleKey must not be null");
    }

    @Test
    public void testBuildsGlobalMarkdownWithUrlCorrectly() {
        final MarkDownUtils markDownUtils = buildUtilsWithBaseUrl("https://localhost:8443/sonar");

        assertThat(markDownUtils.globalIssue(Severity.MAJOR, "This is a violation.", "vio_lation",
                "https://great.local", "com.major.mess"))
                .isEqualTo(":warning: [This is a violation.](https://great.local) [:blue_book:](https://localhost:8443/sonar/coding_rules#rule_key=vio_lation)");
    }

    @Test
    public void testBuildsGlobalMarkdownWithoutUrlCorrectly() {
        final MarkDownUtils markDownUtils = buildUtilsWithBaseUrl("https://DCONTROL/quality");

        assertThat(markDownUtils.globalIssue(Severity.BLOCKER, "NO COMPUTRONS!!!", "magic_sash",
                null, "com.major.mess"))
                .isEqualTo(":no_entry: NO COMPUTRONS!!! (com.major.mess) [:blue_book:](https://DCONTROL/quality/coding_rules#rule_key=magic_sash)");
    }

    @Test
    public void testThrowsExceptionIfGlobalMarkdownParamNull() {
        final MarkDownUtils markDownUtils = buildUtilsWithBaseUrl("https://open.dns.resolver");

        assertThatThrownBy(() -> markDownUtils.globalIssue(null, "NO COMPUTRONS!!!", "magic_sash",
                null, "com.major.mess"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("severity must not be null");

        assertThatThrownBy(() -> markDownUtils.globalIssue(Severity.BLOCKER, null, "magic_sash",
                null, "com.major.mess"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("message must not be null");

        assertThatThrownBy(() -> markDownUtils.globalIssue(Severity.BLOCKER, "NO COMPUTRONS!!!", null,
                null, "com.major.mess"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("ruleKey must not be null");

        assertThatThrownBy(() -> markDownUtils.globalIssue(Severity.BLOCKER, "NO COMPUTRONS!!!", "magic_sash",
                null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("componentKey must not be null");
    }

    @Test
    public void testBuildsCorrectRuleLink() {
        final MarkDownUtils markDownUtils = buildUtilsWithBaseUrl("http://www.sonarz.io/kewl-sonar");

        assertThat(markDownUtils.getRuleLink("rule$_r0ck"))
                .isEqualTo("[:blue_book:](http://www.sonarz.io/kewl-sonar/coding_rules#rule_key=rule%24_r0ck)");
    }

    @Test
    public void testThrowsExceptionIfRuleLinkKeyEmpty() {
        final MarkDownUtils markDownUtils = buildUtilsWithBaseUrl("http://www.ilovebourbons.org/");

        assertThatThrownBy(() -> markDownUtils.getRuleLink(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("ruleKey must not be null");
    }

    private MarkDownUtils buildUtilsWithBaseUrl(final String baseUrl) {
        final Settings settings = new Settings();
        settings.setProperty("sonar.core.serverBaseURL", baseUrl);

        return new MarkDownUtils(settings);
    }

}
