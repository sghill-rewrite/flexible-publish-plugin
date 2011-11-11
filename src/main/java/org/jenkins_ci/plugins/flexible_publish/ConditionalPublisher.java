/*
 * The MIT License
 *
 * Copyright (C) 2011 by Anthony Robinson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkins_ci.plugins.flexible_publish;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.tasks.BuildStep;
import org.jenkins_ci.plugins.run_condition.BuildStepRunner;
import org.jenkins_ci.plugins.run_condition.RunCondition;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class ConditionalPublisher implements Describable<ConditionalPublisher> {

    private final RunCondition condition;
    private final BuildStep publisher;
    private BuildStepRunner runner;

    @DataBoundConstructor
    public ConditionalPublisher(final RunCondition condition, final BuildStep publisher, final BuildStepRunner runner) {
        this.condition = condition;
        this.publisher = publisher;
        this.runner = runner;
    }

    public RunCondition getCondition() {
        return condition;
    }

    public BuildStep getPublisher() {
        return publisher;
    }

    public BuildStepRunner getRunner() {
        return runner;
    }

    public ConditionalPublisherDescriptor getDescriptor() {
        return Hudson.getInstance().getDescriptorByType(ConditionalPublisherDescriptor.class);
    }

    public Collection<? extends Action> getProjectActions(final AbstractProject<?, ?> project) {
        return publisher.getProjectActions(project);
    }

    public boolean prebuild(final AbstractBuild<?, ?> build, final BuildListener listener) {
        return runner.prebuild(condition, publisher, build, listener);
    }

    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
                                                                                                throws InterruptedException, IOException {
        return runner.perform(condition, publisher, build, launcher, listener);
    }

    public Object readResolve() {
        if (runner == null)
            runner = new BuildStepRunner.Fail();
        return this;
    }

    @Extension
    public static class ConditionalPublisherDescriptor extends Descriptor<ConditionalPublisher> {

        @Override
        public String getDisplayName() {
            return "Never seen - one hopes :-)";
        }

        public DescriptorExtensionList<BuildStepRunner, BuildStepRunner.BuildStepRunnerDescriptor> getBuildStepRunners() {
            return BuildStepRunner.all();
        }

        public List<? extends Descriptor<? extends RunCondition>> getRunConditions() {
            return RunCondition.all();
        }

        public List<? extends Descriptor<? extends BuildStep>> getAllowedPublishers() {
            return Hudson.getInstance().getDescriptorByType(FlexiblePublisher.FlexiblePublisherDescriptor.class).getPublisherLister()
                                                                                                                    .getAllowedPublishers();
        }

    }

}
