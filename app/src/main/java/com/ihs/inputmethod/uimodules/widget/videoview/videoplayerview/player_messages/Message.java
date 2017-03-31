package com.ihs.inputmethod.uimodules.widget.videoview.videoplayerview.player_messages;

/**
 * This generic interface for messages
 */
public interface Message {
    void runMessage();
    void polledFromQueue();
    void messageFinished();
}
