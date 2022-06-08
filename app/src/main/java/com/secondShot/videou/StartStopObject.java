package com.secondShot.videou;


public class StartStopObject {

    private StartStopListener listener;

    public interface StartStopListener {
        void confirmedStart();
        void confirmedStop();
    }

    public StartStopObject() {
        this.listener = null;
    }

    public void setCustomObjectListener(StartStopListener listener) {
        this.listener = listener;
    }

    public void processStart() {
        if (listener != null) {
            listener.confirmedStart();
        }
    }

    public void processStop() {
        if (listener != null) {
            listener.confirmedStop();
        }
    }

}
