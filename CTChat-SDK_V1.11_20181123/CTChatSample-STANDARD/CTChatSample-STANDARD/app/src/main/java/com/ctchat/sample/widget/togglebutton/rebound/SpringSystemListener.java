package com.ctchat.sample.widget.togglebutton.rebound;

/**
 * SpringSystemListener provides an interface for listening to events before and after each Physics
 * solving loop the BaseSpringSystem runs.
 */
public interface SpringSystemListener {

  /**
   * Runs before each pass through the physics integration loop providing an opportunity to do any
   * setup or alterations to the Physics state before integrating.
   * @param springSystem the BaseSpringSystem listened to
   */
  void onBeforeIntegrate(BaseSpringSystem springSystem);

  /**
   * Runs after each pass through the physics integration loop providing an opportunity to do any
   * setup or alterations to the Physics state after integrating.
   * @param springSystem the BaseSpringSystem listened to
   */
  void onAfterIntegrate(BaseSpringSystem springSystem);
}

