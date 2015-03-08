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

import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;

import java.io.IOException;

import org.jenkins_ci.plugins.flexible_publish.builder.FailFastBuilder;
import org.jenkins_ci.plugins.flexible_publish.builder.MarkPerformedBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Stop running publishers immediately when one of them fail.
 * Following publishers aren't performed.
 */
public class FailFastExecutionStrategy extends ConditionalExecutionStrategy {
    @DataBoundConstructor
    public FailFastExecutionStrategy() {
    }
    
    @Override
    public boolean prebuild(PublisherContext context, AbstractBuild<?, ?> build, BuildListener listener) {
        return context.getRunner().prebuild(
                context.getCondition(),
                new FailFastBuilder(context.getPublisherList()),
                build, listener
        );
    }
    
    @Override
    public boolean perform(PublisherContext context, AbstractBuild<?, ?> build,
            Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return context.getRunner().perform(
                context.getCondition(),
                new FailFastBuilder(context.getPublisherList()),
                build, launcher, listener
        );
    }
    
    @Override
    public boolean matrixAggregationStartBuild(AggregatorContext aggregatorContext) throws InterruptedException, IOException {
        for(MatrixAggregator aggregator: aggregatorContext.getAggregatorList()) {
            if (!aggregator.startBuild()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public boolean matrixAggregationEndRun(
            AggregatorContext aggregatorContext, MatrixRun run) throws InterruptedException, IOException
    {
        MarkPerformedBuilder mpb = new MarkPerformedBuilder();
        boolean isSuccess = aggregatorContext.getRunner().perform(
                aggregatorContext.getCondition(),
                mpb,
                run, // watch out! not parent build.
                aggregatorContext.getLauncher(),
                aggregatorContext.getListener()
        );
        
        if(!isSuccess || !mpb.isPerformed()) {
            return isSuccess;
        }
        
        for (MatrixAggregator aggregator: aggregatorContext.getAggregatorList()) {
            if (!aggregator.endRun(run)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public boolean matrixAggregationEndBuild(AggregatorContext aggregatorContext) throws InterruptedException, IOException {
        for(MatrixAggregator aggregator: aggregatorContext.getAggregatorList()) {
            try {
                if (!aggregator.endBuild()) {
                    aggregatorContext.getListener().error(String.format("[flexible-publish] aggregation with %s failed", aggregator.toString()));
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace(aggregatorContext.getListener().error(String.format("[flexible-publish] aggregation with %s is aborted due to exception", aggregator.toString())));
                aggregatorContext.getBuild().setResult(Result.FAILURE);
                return false;
            }
        }
        return true;
    }
    
    @Extension
    public static class DescriptorImpl extends Descriptor<ConditionalExecutionStrategy> {
        @Override
        public String getDisplayName() {
            return Messages.FailFastExecutionStragery_DisplayName();
        }
        
    }
}
