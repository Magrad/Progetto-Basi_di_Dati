package project.gui;

import project.utils.GUIUtils;

import javax.swing.*;

public interface FrameLauncher {

    /**
     * Method used to launch a frame.
     * Initialize the frame and set the caller frame in order
     * to be able to go back to the previous frame when needed.
     * @param name Name of the frame to launch.
     * @param caller Caller frame.
     */
    public void Launcher(String name, JFrame caller);
}
