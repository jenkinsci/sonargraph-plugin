package com.hello2morrow.sonargraph.jenkinsplugin.controller;

import hudson.model.Action;

/**
 * Extending from this class means that your action will not be visible as a link in the sidebar, 
 * but since you can implement the getURLName method, It will be possible to access the action
 * by using the URL.
 * @author esteban
 *
 */
public abstract class InvisibleFromSidebarAction extends AbstractHTMLAction implements Action
{
    /**
     * Hides the Icon.
     */
    public final String getIconFileName()
    {
        return null;
    }

    /**
     * Hides the link.
     */
    public final String getDisplayName()
    {
        return null;
    }

}
