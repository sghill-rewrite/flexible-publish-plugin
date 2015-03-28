/*
 * The MIT License
 * 
 * Copyright (c) 2015 IKEDA Yasuyuki
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

package org.jenkins_ci.plugins.flexible_publish.strategy;

import java.io.IOException;
import java.util.List;

import jenkins.model.Jenkins;

import org.jenkins_ci.plugins.flexible_publish.ConditionalPublisher;
import org.jenkins_ci.plugins.run_condition.RunCondition;
import org.jenkins_ci.plugins.run_condition.BuildStepRunner;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixBuild;
import hudson.model.AbstractDescribableImpl;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.tasks.BuildStep;

/**
 * A base class for a strategy for how to run publishers in a condition
 */
public abstract class ConditionalExecutionStrategy
        extends AbstractDescribableImpl<ConditionalExecutionStrategy>
        implements ExtensionPoint
{
    /**
     * Carries parameters from {@link ConditionalPublisher} to run publishers.
     * This allows us keep compatibility when introducing new parameters.
     */
    public static class PublisherContext {
        private final BuildStepRunner runner;
        private final RunCondition condition;
        private final List<BuildStep> publisherList;
        
        public PublisherContext(BuildStepRunner runner, RunCondition condition, List<BuildStep> publisherList) {
            this.runner = runner;
            this.condition = condition;
            this.publisherList = publisherList;
        }
        
        public BuildStepRunner getRunner() {
            return runner;
        }
        
        public RunCondition getCondition() {
            return condition;
        }
        
        public List<BuildStep> getPublisherList() {
            return publisherList;
        }
    }
    
    /**
     * Carries parameters from {@link ConditionalPublisher} to run aggregators.
     * This allows us keep compatibility when introducing new parameters.
     */
    public static class AggregatorContext {
        private final MatrixBuild build;
        private final Launcher launcher;
        private final BuildListener listener;
        private final BuildStepRunner runner;
        private final RunCondition condition;
        private final List<MatrixAggregator> aggregatorList;
        
        public AggregatorContext(MatrixBuild build, Launcher launcher, BuildListener listener, 
                BuildStepRunner runner, RunCondition condition, List<MatrixAggregator> aggregatorList
        ) {
            this.build = build;
            this.launcher = launcher;
            this.listener = listener;
            this.runner = runner;
            this.condition = condition;
            this.aggregatorList = aggregatorList;
        }
        
        public MatrixBuild getBuild() {
            return build;
        }
        
        public Launcher getLauncher() {
            return launcher;
        }
        
        public BuildListener getListener() {
            return listener;
        }
        
        public BuildStepRunner getRunner() {
            return runner;
        }
        
        public RunCondition getCondition() {
            return condition;
        }
        
        public List<MatrixAggregator> getAggregatorList() {
            return aggregatorList;
        }
    }
    
    /**
     * Run {@link BuildStep#prebuild(AbstractBuild, BuildListener)} for all publishers.
     * 
     * @param context
     * @param build
     * @param listener
     * @return false to abort the build.
     */
    public abstract boolean prebuild(PublisherContext context, AbstractBuild<?,?> build, BuildListener listener);
    
    /**
     * Run {@link BuildStep#perform(AbstractBuild, Launcher, BuildListener)} for all publishes.
     * 
     * @param context
     * @param build
     * @param launcher
     * @param listener
     * @return false to indicate a failure. That doesn't abort the build.
     * @throws InterruptedException
     * @throws IOException
     */
    public abstract boolean perform(PublisherContext context, AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException;
    
    /**
     * Run {@link MatrixAggregator#startBuild()} for all publishes.
     * 
     * @param aggregatorContext
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    public abstract boolean matrixAggregationStartBuild(AggregatorContext aggregatorContext) throws InterruptedException, IOException;
    
    /**
     * Run {@link MatrixAggregator#endRun(MatrixRun)} for all publishes.
     * 
     * @param aggregatorContext
     * @param run
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    public abstract boolean matrixAggregationEndRun(AggregatorContext aggregatorContext, MatrixRun run) throws InterruptedException, IOException;
    
    /**
     * Run {@link MatrixAggregator#endBuild()} for all publishes.
     * 
     * @param aggregatorContext
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    public abstract boolean matrixAggregationEndBuild(AggregatorContext aggregatorContext) throws InterruptedException, IOException;
    
    public static DescriptorExtensionList<ConditionalExecutionStrategy, Descriptor<ConditionalExecutionStrategy>> all() {
        return Jenkins.getInstance().getDescriptorList(ConditionalExecutionStrategy.class);
    }

}
