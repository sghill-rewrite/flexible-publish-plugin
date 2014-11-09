/*
 * The MIT License
 * 
 * Copyright (c) 2014 IKEDA Yasuyuki
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

import java.io.File;
import java.util.Arrays;

import hudson.model.FreeStyleBuild;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import hudson.model.StringParameterValue;
import hudson.tasks.BuildStep;
import hudson.tasks.ArtifactArchiver;

import org.jenkins_ci.plugins.flexible_publish.testutils.FileWriteBuilder;
import org.jenkins_ci.plugins.run_condition.BuildStepRunner;
import org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.recipes.LocalData;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 *
 */
public class FlexiblePublisherTest extends HudsonTestCase {
    @LocalData
    public void testMigrationFrom0_12() throws Exception {
        FreeStyleProject p = jenkins.getItemByFullName("migration_from_0.12", FreeStyleProject.class);
        FlexiblePublisher fp = p.getPublishersList().get(FlexiblePublisher.class);
        ConditionalPublisher cp = fp.getPublishers().get(0);
        assertEquals(
                Arrays.<Class<?>>asList(ArtifactArchiver.class),
                Lists.transform(cp.getPublisherList(), new Function<BuildStep, Class<?>>() {
                    public Class<?> apply(BuildStep input) {
                        return input.getClass();
                    }
                })
        );
        ArtifactArchiver aa = (ArtifactArchiver)cp.getPublisherList().get(0);
        assertEquals("artifact.txt", aa.getArtifacts());
        
        {
            @SuppressWarnings("deprecation")
            FreeStyleBuild b = p.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(
                    new StringParameterValue("SWITCH", "off")
            )).get();
            assertBuildStatusSuccess(b);
            assertFalse(new File(b.getArtifactsDir(), "artifact.txt").exists());
        }
        
        {
            @SuppressWarnings("deprecation")
            FreeStyleBuild b = p.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(
                    new StringParameterValue("SWITCH", "on")
            )).get();
            assertBuildStatusSuccess(b);
            assertTrue(new File(b.getArtifactsDir(), "artifact.txt").exists());
        }
    }
    
    public void testMultipleConditionsMultipleActions() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        
        p.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("SWITCH", "off")
        ));
        
        p.getBuildersList().add(new FileWriteBuilder("artifact1.txt", "blahblahblah"));
        p.getBuildersList().add(new FileWriteBuilder("artifact2.txt", "blahblahblah"));
        p.getBuildersList().add(new FileWriteBuilder("artifact3.txt", "blahblahblah"));
        p.getBuildersList().add(new FileWriteBuilder("artifact4.txt", "blahblahblah"));
        
        p.getPublishersList().add(new FlexiblePublisher(Arrays.asList(
                new ConditionalPublisher(
                        new StringsMatchCondition("${SWITCH}", "off", false),
                        Arrays.<BuildStep>asList(
                                new ArtifactArchiver("artifact1.txt", "", false),
                                new ArtifactArchiver("artifact2.txt", "", false)
                        ),
                        new BuildStepRunner.Fail(),
                        false,
                        null,
                        null
                ),
                new ConditionalPublisher(
                        new StringsMatchCondition("${SWITCH}", "on", false),
                        Arrays.<BuildStep>asList(
                                new ArtifactArchiver("artifact3.txt", "", false),
                                new ArtifactArchiver("artifact4.txt", "", false)
                        ),
                        new BuildStepRunner.Fail(),
                        false,
                        null,
                        null
                )
        )));
        
        {
            @SuppressWarnings("deprecation")
            FreeStyleBuild b = p.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(
                    new StringParameterValue("SWITCH", "off")
            )).get();
            assertBuildStatusSuccess(b);
            assertTrue(new File(b.getArtifactsDir(), "artifact1.txt").exists());
            assertTrue(new File(b.getArtifactsDir(), "artifact2.txt").exists());
            assertFalse(new File(b.getArtifactsDir(), "artifact3.txt").exists());
            assertFalse(new File(b.getArtifactsDir(), "artifact4.txt").exists());
        }
        
        {
            @SuppressWarnings("deprecation")
            FreeStyleBuild b = p.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(
                    new StringParameterValue("SWITCH", "on")
            )).get();
            assertBuildStatusSuccess(b);
            assertFalse(new File(b.getArtifactsDir(), "artifact1.txt").exists());
            assertFalse(new File(b.getArtifactsDir(), "artifact2.txt").exists());
            assertTrue(new File(b.getArtifactsDir(), "artifact3.txt").exists());
            assertTrue(new File(b.getArtifactsDir(), "artifact4.txt").exists());
        }
        
        {
            @SuppressWarnings("deprecation")
            FreeStyleBuild b = p.scheduleBuild2(0, new Cause.UserCause(), new ParametersAction(
                    new StringParameterValue("SWITCH", "badValue")
            )).get();
            assertBuildStatusSuccess(b);
            assertFalse(new File(b.getArtifactsDir(), "artifact1.txt").exists());
            assertFalse(new File(b.getArtifactsDir(), "artifact2.txt").exists());
            assertFalse(new File(b.getArtifactsDir(), "artifact3.txt").exists());
            assertFalse(new File(b.getArtifactsDir(), "artifact4.txt").exists());
        }
    }
}
