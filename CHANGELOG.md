# Change log

## 0.15.2 (Jun 06, 2015)

-   FIXED: Conditions are evaluated for matrix parent builds even if
    contained publishers doesn't support aggregations
    ([JENKINS-28585](https://issues.jenkins-ci.org/browse/JENKINS-28585))
    -   Regression in 0.15.

## 0.15.1 (Mar 29, 2015)

-   No functionality changes from flexible-publish-0.15.
-   Displays incompatibility warnings in the update center for
    flexible-publish 0.14.1 and earlier.
    -   This means the change of the condition evaluation in
        flexible-publish-0.15.
    -   Warnings displayed: [Marking a new plugin version as
        incompatible with older versions\#Modification to Display of
        Updateable Plugin
        List](https://wiki.jenkins.io/display/JENKINS/Marking+a+new+plugin+version+as+incompatible+with+older+versions#Markinganewpluginversionasincompatiblewitholderversions-ModificationtoDisplayofUpdateablePluginList)

## 0.15 (Mar 28, 2015)

-   Introduced "Execution strategy" which controls the behavior when a
    publisher fails.
    ([JENKINS-26936](https://issues.jenkins-ci.org/browse/JENKINS-26936),
    [JENKINS-27278](https://issues.jenkins-ci.org/browse/JENKINS-27278))
    -   See [\#How flexible publish works when a publisher
        fails](https://wiki.jenkins.io/display/JENKINS/Flexible+Publish+Plugin#FlexiblePublishPlugin-Howflexiblepublishworkswhenapublisherfails)
        for details.
-   The condition is evaluated only once when multiple actions in a
    condition
    ([JENKINS-27171](https://issues.jenkins-ci.org/browse/JENKINS-27171)).
    -   Example Configutraion

            Flexible Publish
                Condition 1
                    Publisher 1
                    Publisher 2

    -   It was evaluated like this in flexible-publish 0.14.1

            if(Condition 1)
            {
                Publisher 1
            }
            if(Condition 1)
            {
                Publisher 2
            }

    -   flexible-publish 0.15 now evaluates as following

            if(Condition 1)
            {
                Publisher 1
                Publisher 2
            }

    -   If you really need conditions evaluated for each actions, please
        update the configuration like this:

            Flexible Publish
                Condition 1
                    Publisher 1
                Condition 1
                    Publisher 2

## 0.14.1 (Jan 17, 2015)

**This is a bug fix for 0.13.**  
This should be released as 0.13.1, but I mistook. Sorry.

-   FIXED: NPE if no publisher in conditional step
    ([JENKINS-26452](https://issues.jenkins-ci.org/browse/JENKINS-26452))

## 0.13 (Nov 09, 2014)

-   Supports multiple actions for a condition.
    ([JENKINS-22187](https://issues.jenkins-ci.org/browse/JENKINS-22187))
-   Also work for Depende\*n\*cyDeclarer introduced in Jenkins 1.501.
    ([JENKINS-25017](https://issues.jenkins-ci.org/browse/JENKINS-25017))
-   improved explanation for the aggregation condition.
    ([JENKINS-21345](https://issues.jenkins-ci.org/browse/JENKINS-21345))
-   Fixed a wrong error message when failed to instantiate a publisher.
    ([JENKINS-21497](https://issues.jenkins-ci.org/browse/JENKINS-21497))
-   Add support for upcoming $class annotation change
    ([JENKINS-25403](https://issues.jenkins-ci.org/browse/JENKINS-25403))

## 0.12 (14/09/2013)

-   Now support publishers with customized Descriptor\#newInstance
    ([JENKINS-19494](https://issues.jenkins-ci.org/browse/JENKINS-19494),
    [JENKINS-14454](https://issues.jenkins-ci.org/browse/JENKINS-14454),
    [JENKINS-14597](https://issues.jenkins-ci.org/browse/JENKINS-14597),
    [JENKINS-19257](https://issues.jenkins-ci.org/browse/JENKINS-19257))

## 0.11 (07/09/2013)

-   Support for triggers
    ([JENKINS-19146](https://issues.jenkins-ci.org/browse/JENKINS-19146))
-   Support for multi-configuration projects
    ([JENKINS-14494](https://issues.jenkins-ci.org/browse/JENKINS-14494))

## 0.10 (03/07/2012)

-   Fixed
    [JENKINS-13795](https://issues.jenkins-ci.org/browse/JENKINS-13795)
    NPE when configuring Flexible publish in a new job

## 0.9 (14/02/2012)

-   Stop interfering with the [Warnings
    Plugin](https://wiki.jenkins.io/display/JENKINS/Warnings+Plugin)'s
    radio buttons
    ([JENKINS-12692](https://issues.jenkins-ci.org/browse/JENKINS-12692))

## 0.8 (17/01/2012)

-   Exclude "Trigger parametrized build on other projects"
    [JENKINS-12418](https://issues.jenkins-ci.org/browse/JENKINS-12418)

## 0.7 (14/11/2011)

-   Mark the build as a failure if an action would have stopped the
    build

## 0.6 (12/11/2011)

-   Add some utility methods to allow publishers to be moved into
    Flexible publish from the script console
-   Don't allow "Build other projects" in the Flexible publish - it does
    not work here
-   Do not allow the actions to prevent other publishers from running
    (unless they throw an exception)

## 0.5 (11/11/2011)

-   Changed the EP interface

## 0.4 (11/11/2011)

-   Add extension to enable the list of publishers to be modified

## 0.3 (10/11/2011)

-   Updated a help file

## 0.2 (09/11/2011)

-   Enable the user to choose what will happen if the evaluation of a
    condition fails

## 0.1 (07/11/2011)

-   Initial release
