<!-- ********************************************************************
     Copyright 2013 Ellucian Company L.P. and its affiliates.
******************************************************************** -->

#Banner General Utility plugin documentation

##Status
Production quality, although subsequent changes may not be backward compatible.  Remember to include this software in export compliance reviews when shipping a solution that uses this plugin.

##Overview
This plugin adds Common menu services for General Menu App and Menu & Self Service Menu services for Admin App and Self Service App. This plugin will also provide any generic utilities for Admin and SelfService Apps.

##Installation and quickstart
The recommended approach is to install the plugin as a git submodule.

###1. Add Git submodule
The plugin repo is located at ssh://git@devgit1/banner/plugins/banner_general_utility.git and releases are tagged (e.g., 'pub-2.x.x'). We recommend using this plugin as an in-place plugin using Git submodules.

To add the plugin as a Git submodule under a 'plugins' directory:

        test_app (master)$ git submodule add ssh://git@devgit1/banner/plugins/banner_general_utility.git plugins/banner_general_utility.git
        Cloning into 'plugins/banner_general_utility.git'...
        remote: Counting objects: 1585, done.
        remote: Compressing objects: 100% (925/925), done.
        remote: Total 1585 (delta 545), reused 309 (delta 72)
        Receiving objects: 100% (1585/1585), 294.45 KiB | 215 KiB/s, done.
        Resolving deltas: 100% (545/545), done.

Then add the in-place plugin definition to BuildConfig.groovy:

        grails.plugin.location.'banner_general_utility' = "plugins/banner_general_utility.git"

Note that adding the plugin this way will the latest commit on the master branch at the time you ran the submodule command.  If you want to use an official release instead, go to the plugin directory and checkout a specific version, e.g.:

    cd plugins/banner_general_utility.git
    git checkout pub-2.7.3

Don't forget to go back to your project root and commit the change this will make to your git submodules file.

###3. Configure the UrlMappings to use the controller
Edit the UrlMappings.groovy to look similar to the following defaults.  Your application map already have url mappings defined; if so, add the following mappings for as appropriate.

####For SSB apps:

    static mappings = {

        "/ssb/menu" {
            controller = "selfServiceMenu"
            action = [GET: "data", POST: "create"]
        }

    }