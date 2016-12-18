package com.jsherz.sonargitlab;

import com.synaptix.sonar.plugins.gitlab.CommitFacade;
import com.synaptix.sonar.plugins.gitlab.CommitProjectBuilder;
import com.synaptix.sonar.plugins.gitlab.GitLabPluginConfiguration;
import org.junit.Test;
import org.sonar.api.batch.bootstrap.ProjectBuilder;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.api.config.Settings;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class CommitProjectBuilderTest {

    @Test
    public void testDisabledPluginDoesntDoAnything() {
        final Settings settings = new Settings();
        final GitLabPluginConfiguration gitLabPluginConfiguration = new GitLabPluginConfiguration(settings);
        final CommitFacade commitFacade = spy(new CommitFacade(gitLabPluginConfiguration));
        final CommitProjectBuilder commitProjectBuilder = new CommitProjectBuilder(gitLabPluginConfiguration, commitFacade, settings);

        commitProjectBuilder.build(null);

        verify(commitFacade, never()).init(any());
        verify(commitFacade, never()).createOrUpdateSonarQubeStatus(any(), any());
    }

    @Test
    public void testEnabledPluginInitialisesCommmitFacade() {
        final Settings settings = new Settings();
        settings.setProperty("sonar.gitlab.commit_sha", "12345");

        final GitLabPluginConfiguration gitLabPluginConfiguration = new GitLabPluginConfiguration(settings);
        final CommitFacade commitFacade = mock(CommitFacade.class);
        final CommitProjectBuilder commitProjectBuilder = new CommitProjectBuilder(gitLabPluginConfiguration, commitFacade, settings);

        final ProjectBuilder.Context context = () -> new ProjectReactor(ProjectDefinition.create());
        commitProjectBuilder.build(context);

        verify(commitFacade, times(1)).init(any());
        verify(commitFacade, times(1)).createOrUpdateSonarQubeStatus(any(), any());
    }

}
