/*
 * SonarQube :: GitLab Plugin
 * Copyright (C) 2016-2016 Talanlabs
 * gabriel.allaigre@talanlabs.com
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
package com.synaptix.sonar.plugins.gitlab;

import org.sonar.api.CoreProperties;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.config.Settings;
import org.sonar.api.internal.google.common.collect.ImmutableMap;
import org.sonar.api.rule.Severity;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;


@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
@BatchSide
public class MarkDownUtils {

    private static final String SONAR_HOST_URL = "sonar.host.url";

    private static final Map<String, String> SEVERITY_EMOJI_MAPPINGS = ImmutableMap.of(
            Severity.BLOCKER, ":no_entry:",
            Severity.CRITICAL, ":no_entry_sign:",
            Severity.MAJOR, ":warning:",
            Severity.MINOR, ":arrow_down_small:",
            Severity.INFO, ":information_source:"
    );

    private final String ruleUrlPrefix;

    /**
     * Sets up these utilities.
     * <p>
     * If sonar.core.serverBaseURL is configured, it will be used over the sonar host URL.
     *
     * @param settings
     */
    public MarkDownUtils(final Settings settings) {
        checkNotNull(settings, "settings must not be null");

        // If server base URL was not configured in SQ server then is is better to take URL configured on batch side
        final String baseUrl = settings.hasKey(CoreProperties.SERVER_BASE_URL) ?
                settings.getString(CoreProperties.SERVER_BASE_URL) :
                settings.getString(SONAR_HOST_URL);

        if (baseUrl == null) {
            throw new IllegalArgumentException(String.format("A base URL must be provided with the setting %s or %s",
                    CoreProperties.SERVER_BASE_URL,
                    SONAR_HOST_URL
            ));
        } else if (baseUrl.endsWith("/")) {
            this.ruleUrlPrefix = baseUrl;
        } else {
            this.ruleUrlPrefix = baseUrl + "/";
        }
    }

    /**
     * UTF-8 URL encode a value.
     *
     * @param url
     * @return
     */
    public static String encodeForUrl(final String url) {
        try {
            return URLEncoder.encode(url, StandardCharsets.UTF_8.displayName());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Encoding not supported", e);
        }
    }

    /**
     * Returns the markdown emoji text for a violation severity.
     *
     * @param severity
     * @return
     */
    public static String getEmojiForSeverity(final String severity) {
        return SEVERITY_EMOJI_MAPPINGS.getOrDefault(severity, ":grey_question:");
    }

    /**
     * Format a rule violation for display inline with other information.
     *
     * @param severity
     * @param message
     * @param ruleKey
     * @return
     */
    public String inlineIssue(final String severity, final String message, final String ruleKey) {
        checkNotNull(severity, "severity must not be null");
        checkNotNull(message, "message must not be null");
        checkNotNull(ruleKey, "ruleKey must not be null");

        final String ruleLink = getRuleLink(ruleKey);

        return String.format("%s %s %s", getEmojiForSeverity(severity), message, ruleLink);
    }

    public String globalIssue(String severity, String message, String ruleKey, @Nullable String url, String componentKey) {
        String ruleLink = getRuleLink(ruleKey);
        StringBuilder sb = new StringBuilder();
        sb.append(getEmojiForSeverity(severity)).append(" ");
        if (url != null) {
            sb.append("[").append(message).append("]").append("(").append(url).append(")");
        } else {
            sb.append(message).append(" ").append("(").append(componentKey).append(")");
        }
        sb.append(" ").append(ruleLink);
        return sb.toString();
    }

    /**
     * Get a markdown link to the coding rule with the supplied key.
     *
     * @param ruleKey
     * @return
     */
    public String getRuleLink(String ruleKey) {
        checkNotNull(ruleKey, "ruleKey must not be null");

        return "[:blue_book:](" + ruleUrlPrefix + "coding_rules#rule_key=" + encodeForUrl(ruleKey) + ")";
    }

}
