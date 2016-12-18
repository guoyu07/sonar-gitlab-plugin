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

import com.google.common.collect.ImmutableList;
import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import java.util.List;


public class GitLabPlugin implements Plugin {

    protected static final String GITLAB_URL = "sonar.gitlab.url";
    protected static final String GITLAB_MAX_GLOBAL_ISSUES = "sonar.gitlab.max_global_issues";
    protected static final String GITLAB_USER_TOKEN = "sonar.gitlab.user_token";
    protected static final String GITLAB_PROJECT_ID = "sonar.gitlab.project_id";
    protected static final String GITLAB_COMMIT_SHA = "sonar.gitlab.commit_sha";
    protected static final String GITLAB_REF_NAME = "sonar.gitlab.ref_name";
    protected static final String GITLAB_IGNORE_FILE = "sonar.gitlab.ignore_file";
    protected static final String GITLAB_COMMENT_NO_ISSUE = "sonar.gitlab.comment_no_issue";
    protected static final String GITLAB_CUSTOM_BUILDER_NAME = "sonar.gitlab.custom_builder_name";

    private static final String CATEGORY = "gitlab";
    private static final String INSTANCE_SUBCATEGORY = "instance";
    private static final String REPORTING_SUBCATEGORY = "reporting";

    private static final List<PropertyDefinition> DEFINITIONS = ImmutableList.of(
            PropertyDefinition.builder(GITLAB_URL)
                    .name("GitLab URL")
                    .description("Full URL to GitLab instance.")
                    .category(CATEGORY)
                    .subCategory(INSTANCE_SUBCATEGORY)
                    .defaultValue("https://gitlab.com")
                    .index(1)
                    .build(),
            PropertyDefinition.builder(GITLAB_USER_TOKEN)
                    .name("GitLab user token")
                    .description("Token for the user that SonarQube will comment as.")
                    .category(CATEGORY)
                    .subCategory(INSTANCE_SUBCATEGORY)
                    .index(2)
                    .build(),
            PropertyDefinition.builder(GITLAB_MAX_GLOBAL_ISSUES)
                    .name("Max issues in global comment")
                    .description("The maximum number of issues to display in the overall global issue comment.")
                    .category(CATEGORY)
                    .subCategory(REPORTING_SUBCATEGORY)
                    .type(PropertyType.INTEGER)
                    .defaultValue(String.valueOf(10))
                    .index(3)
                    .build(),
            PropertyDefinition.builder(GITLAB_PROJECT_ID)
                    .name("Project ID")
                    .description("The unique id, path with namespace, name with namespace, web url, ssh url or" +
                            "http url of the current project that is being analysed.")
                    .category(CATEGORY)
                    .subCategory(REPORTING_SUBCATEGORY)
                    .index(4)
                    .onlyOnQualifiers(Qualifiers.PROJECT)
                    .build(),
            PropertyDefinition.builder(GITLAB_COMMIT_SHA)
                    .name("Commit SHA")
                    .description("The commit revision that is being analysed.")
                    .category(CATEGORY)
                    .subCategory(REPORTING_SUBCATEGORY)
                    .index(5)
                    .hidden()
                    .build(),
            PropertyDefinition.builder(GITLAB_REF_NAME)
                    .name("Ref name")
                    .description("The commit revision that is being analysed.")
                    .category(CATEGORY)
                    .subCategory(REPORTING_SUBCATEGORY)
                    .index(6)
                    .hidden()
                    .build(),
            PropertyDefinition.builder(GITLAB_IGNORE_FILE)
                    .name("Ignore unrelated files")
                    .description("Ignore issues on files not modified by the commit.")
                    .category(CATEGORY)
                    .subCategory(REPORTING_SUBCATEGORY)
                    .type(PropertyType.BOOLEAN)
                    .defaultValue(String.valueOf(false))
                    .index(7)
                    .build(),
            PropertyDefinition.builder(GITLAB_COMMENT_NO_ISSUE)
                    .name("Comment if no new issues")
                    .description("Add a comment even when there are no new issues.")
                    .category(CATEGORY)
                    .subCategory(REPORTING_SUBCATEGORY)
                    .type(PropertyType.BOOLEAN)
                    .defaultValue(String.valueOf(false))
                    .index(8)
                    .build(),
            PropertyDefinition.builder(GITLAB_CUSTOM_BUILDER_NAME)
                    .name("Custom builder name")
                    .description("Replaces the \"sonarqube\" name when displaying & updating a build in GitLab.")
                    .category(CATEGORY)
                    .subCategory(REPORTING_SUBCATEGORY)
                    .type(PropertyType.TEXT)
                    .index(9)
                    .build()
    );

    @Override
    public void define(Context context) {
        context.addExtensions(CommitIssuePostJob.class, GitLabPluginConfiguration.class, CommitProjectBuilder.class,
                CommitFacade.class, InputFileCacheSensor.class, InputFileCache.class, MarkDownUtils.class);
        context.addExtensions(DEFINITIONS);
    }

}
