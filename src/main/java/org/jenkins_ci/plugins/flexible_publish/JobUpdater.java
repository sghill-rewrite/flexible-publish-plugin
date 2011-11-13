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

import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.tasks.BuildStep;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import org.jenkins_ci.plugins.run_condition.BuildStepRunner;
import org.jenkins_ci.plugins.run_condition.core.AlwaysRun;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for the script console
 */
public class JobUpdater {

    /**
     * Move a publisher from the Post-build Actions and into the Flexible publish publisher
     * 
     * For a freestyle project called 'xxx', to move the 'Archive the artifacts' step:
     * <code>
     * import static org.jenkins_ci.plugins.flexible_publish.JobUpdater.*
     * 
     * def job = hudson.model.Hudson.instance.getItem('xxx')
     * movePublisher job, 'archive the artifacts'
     * </code>
     * 
     * Once executed, go to the configure page to check that everything looks OK, then save the configuration
     */    
    public static boolean movePublisher(final AbstractProject<?, ?> project, final String publisherName) throws IOException {
        if (project == null) throw new RuntimeException(Messages.jobUpdated_theProjectIsNull());
        final DescribableList<Publisher, Descriptor<Publisher>> publishers =  project.getPublishersList();
        Publisher target = null;
        for (Publisher pub : publishers) {
            if (pub.getDescriptor().getDisplayName().equalsIgnoreCase(publisherName))
                target = pub;
        }
        if (target == null)
            throw new RuntimeException(format(Messages.jobUpdater_publisherNotFound(publisherName), publishers.toList()));
        final List<? extends Descriptor<? extends BuildStep>> allowed = getAllowedPublishers(project);
        if (!allowed.contains(target.getDescriptor()))
            throw new RuntimeException(Messages.jobUpdater_publisherNotAllowed(publisherName));
        final ConditionalPublisher wrapped = new ConditionalPublisher(new AlwaysRun(), target, new BuildStepRunner.Fail());
        publishers.remove(target);
        getOrCreateFlexiblePublisher(publishers).getPublishers().add(wrapped);
        return true;
    }

    /**
     * Move all publishers that are "allowed" to be moved into the Flexible publish
     *
     * Only use this method if you really know what you're doing - or don't care about the consequences
     * For those that like to live on the edge!
     */
    public static boolean moveAllPublishers(final AbstractProject<?, ?> project) throws IOException {
        if (project == null) throw new RuntimeException(Messages.jobUpdated_theProjectIsNull());
        final DescribableList<Publisher, Descriptor<Publisher>> publishers =  project.getPublishersList();
        final List<? extends Descriptor<? extends BuildStep>> allowed = getAllowedPublishers(project);
        final List<ConditionalPublisher> wrapped = new ArrayList<ConditionalPublisher>();
        for (Publisher publisher : publishers) {
            if (allowed.contains(publisher.getDescriptor())) {
                publishers.remove(publisher);
                wrapped.add(new ConditionalPublisher(new AlwaysRun(), publisher, new BuildStepRunner.Fail()));
            }
        }
        if (wrapped.size() > 0)
            getOrCreateFlexiblePublisher(publishers).getPublishers().addAll(wrapped);
        return true;
    }

    private static FlexiblePublisher getOrCreateFlexiblePublisher(final DescribableList<Publisher, Descriptor<Publisher>> publishers)
                                                                                                                        throws IOException {
        FlexiblePublisher flex = publishers.get(FlexiblePublisher.class);
        if (flex == null) {
            flex = new FlexiblePublisher(new ArrayList<ConditionalPublisher>());
            final List<Publisher> replacement = new ArrayList<Publisher>(publishers.toList());
            replacement.add(0, flex);
            publishers.replaceBy(replacement);
        }
        return flex;
    }

    /**
     * List the publishers that are enabled on the project, and that can be moved into the Flexible publish
     * 
     * <code>
     * import static org.jenkins_ci.plugins.flexible_publish.JobUpdater.*
     * 
     * def job = hudson.model.Hudson.instance.getItem('xxx')
     * list job
     * </code>
     * 
     * // Or, if you like doing it the hard way ...
     * 
     * <code>
     * org.jenkins_ci.plugins.flexible_publish.JobUpdater.list(hudson.model.Hudson.instance.getItem('xxx'))
     * </code>
     */
    public static String list(final AbstractProject<?, ?> project) {
        return list(project, true) + System.getProperty("line.separator") + list(project, false);
    }

    /**
     * List the publishers that are enabled on the project, and that can be moved into the Flexible publish
     * 
     * <code>
     * import static org.jenkins_ci.plugins.flexible_publish.JobUpdater.*
     * 
     * def job = hudson.model.Hudson.instance.getItem('xxx')
     * listCanMove job
     * </code>
     */
    public static String listCanMove(final AbstractProject<?, ?> project) {
        return list(project, true);
    }

    /**
     * List the publishers that are enabled on the project, and but cannot be moved into the Flexible publish
     * 
     * <code>
     * import static org.jenkins_ci.plugins.flexible_publish.JobUpdater.*
     * 
     * def job = hudson.model.Hudson.instance.getItem('xxx')
     * listCannotMove job
     * </code>
     * 
     * // Or, if you like doing it the hard way ...
     * 
     * <code>
     * org.jenkins_ci.plugins.flexible_publish.JobUpdater.list(hudson.model.Hudson.instance.getItem('xxx'))
     * </code>
     */
    public static String listCannotMove(final AbstractProject<?, ?> project) {
        return list(project, false);
    }

    public static String list(final AbstractProject<?, ?> project, final boolean listCanMove) {
        if (project == null) return Messages.jobUpdated_theProjectIsNull();
        final List<? extends Descriptor<? extends BuildStep>> allowed = getAllowedPublishers(project);
        final List<Publisher> selected = new ArrayList<Publisher>();
        for (Publisher pub : project.getPublishersList()) {
            if (listCanMove == allowed.contains(pub.getDescriptor()))
                selected.add(pub);
        }
        return format(listCanMove ? Messages.jobUpdater_canMovePublishers() : Messages.jobUpdater_cannotMovePublishers(), selected);
    }
    
    private static List<? extends Descriptor<? extends BuildStep>> getAllowedPublishers(final AbstractProject<?, ?> project) {
        return new DefaultPublisherDescriptorLister().getAllowedPublishers(project);
    }

    private static String format(final String header, final List<Publisher> publishers) {
        final StringWriter writer = new StringWriter();
        final PrintWriter printer = new PrintWriter(writer);
        printer.println(header);
        append(printer, publishers);
        return writer.toString();
    }

    private static void append(final PrintWriter printWriter, final List<Publisher> publishers) {
        for (Publisher pub : publishers)
            printWriter.println("\t[" + pub.getDescriptor().getDisplayName() + "]");
    }

}
