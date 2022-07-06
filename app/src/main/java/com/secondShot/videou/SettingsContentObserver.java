package com.secondShot.videou;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;


public class SettingsContentObserver extends ContentObserver {
    private int originalVolume;
    private Context context;
    private AudioManager audioManager;
    private StartStopObject startStopObject;
    private boolean start = false;
    private boolean stop = false;
    private boolean startEnabled;
    private boolean musicPrevActive;
    private boolean musicInitActive;

    private enum ButtonType {
        NOT_ENABLED(0), PLAY_PAUSE(1), VOLUME_UP(2), VOLUME_DOWN(3);
        private final int value;
        ButtonType(int value) {
            this.value = value;
        }
    }

    private ButtonType startBtn;
    private ButtonType stopBtn;

    public SettingsContentObserver(Context context, Handler handler, int volume,
                                   StartStopObject startStopObject, boolean startEnabled,
                                   int startBtn, int saveInt) {
        super(handler);
        this.context = context;
        this.startStopObject = startStopObject;
        this.startEnabled = startEnabled;
        this.startBtn = ButtonType.values()[startBtn];
        this.stopBtn = ButtonType.values()[saveInt];
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        originalVolume = volume;
        musicPrevActive = audioManager.isMusicActive();
        musicInitActive = audioManager.isMusicActive();

        if ((this.startBtn == ButtonType.PLAY_PAUSE || this.stopBtn == ButtonType.PLAY_PAUSE) && this.startEnabled) {
            createTimer();
        }

    }

    public void createTimer() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("preferenceList", Context.MODE_PRIVATE);
        String timerLag = sharedPreferences.getString("timer", new String());
        int timerLagInt = 0;
        switch (timerLag) {
            case "1 second":
                timerLagInt = 1000;
                break;
            case "3 seconds":
                timerLagInt = 3000;
                break;
            case "5 seconds":
                timerLagInt = 5000;
                break;
            case "10 seconds":
                timerLagInt = 10000;
                break;
            case "15 seconds":
                timerLagInt = 15000;
                break;
            case "20 seconds":
                timerLagInt = 20000;
                break;
            case "30 seconds":
                timerLagInt = 30000;
                break;
        }

        // TODO speech recognition fails - needs to restart and update text view
        // Use case 1. Music is off, recognition, and stops with play button
        // Use case 2. Music is off, recognition, and stops with volume button
        // Use case 3. Music is on, recognition, and stops with play button
        // Use case 4. Music is on, recognition, and stops with either volume button - TAKEN CARE OF
        // Use case 5. Music is on, start with pause, and stops with either volume button
        // Use case 6. Music is on, start with pause, and stops with pause button
        // Use case 7. Music is off, start with pause, and stops with volume button
        // Use case 8. Music is off, start with pause, and stops with pause button

        Handler newHandler = new Handler(Looper.getMainLooper());

        // We safely assume speech recognition handled for start
        if (startBtn == ButtonType.NOT_ENABLED) {
            Runnable runnable = new Runnable() {
                Boolean counter = false;
                @Override
                public void run() {
                    if (start) {
                        // for handling with music originally active
                        if (musicInitActive && !musicPrevActive) {
                            if (stopBtn == ButtonType.PLAY_PAUSE) {
                                stop = true;
                                startStopObject.processStop();
                                newHandler.removeCallbacks(this);
                                return;
                            }
                        // for handling with music not originally active
                        } else if (!musicInitActive && !musicPrevActive && !counter) {
                            if (stopBtn == ButtonType.PLAY_PAUSE) {
                                audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
                                counter = true;
                            }
                        // for handling with music not originally active 2nd part
                        } else if (!musicInitActive && !musicPrevActive && counter){
                            stop = true;
                            startStopObject.processStop();
                            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE));
                            newHandler.removeCallbacks(this);
                            return;
                        }
                        musicPrevActive = audioManager.isMusicActive();
                        newHandler.postDelayed(this, 250);
                    }
                }
            };
            newHandler.postDelayed(runnable, timerLagInt);
        } else {
            // used for case with start button as play/pause and or stop button being play/pause when not used with speech recognition
            int finalTimerLagInt = timerLagInt;
            Runnable runnable = new Runnable() {
                boolean firstTime = false;
                @Override
                public void run() {
                    if (audioManager.isMusicActive() != musicPrevActive) {
                        if (startEnabled && musicInitActive && !firstTime && !start) {
                            firstTime = true;
                            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
                            start = true;
                            startStopObject.processStart();
                            if (stopBtn == ButtonType.PLAY_PAUSE && !stop) {
                                newHandler.postDelayed(this, finalTimerLagInt + 250);
                            } else {
                                newHandler.removeCallbacks(this);
                            }
                            return;
                        }
                        musicPrevActive = audioManager.isMusicActive();
                        if (startEnabled) {
                            if (start && !stop) {
                                if (stopBtn == ButtonType.PLAY_PAUSE) {
                                    stop = true;
                                    startStopObject.processStop();
                                    newHandler.removeCallbacks(this);
                                }
                            } else if (!start) {
                                if (startBtn == ButtonType.PLAY_PAUSE) {
                                    start = true;
                                    startStopObject.processStart();
                                }
                                if (stopBtn != ButtonType.PLAY_PAUSE) {
                                    newHandler.removeCallbacks(this);
                                } else {
                                    newHandler.postDelayed(this, finalTimerLagInt + 250);
                                }
                            }
                        }
                    } else {
                        newHandler.postDelayed(this, 250);
                    }
                }
            };
            newHandler.postDelayed(runnable, 0);
        }
    }

    public void setStart(boolean startPref) {
        // only used for creating timer with speech recognition as start and play/pause as stop
        // doing this to prevent multiple calls
        if (this.stopBtn == ButtonType.PLAY_PAUSE && !startEnabled) {
            createTimer();
        }
        this.start = startPref;
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        if (!((startEnabled && startBtn != ButtonType.PLAY_PAUSE) || (stopBtn != ButtonType.PLAY_PAUSE))) {
            return;
        }

        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int delta = originalVolume - currentVolume;

        if (!startEnabled) {
            if (stopBtn == ButtonType.VOLUME_UP) {
                if (delta < 0 && !stop) {
                    startStopObject.processStop();
                    stop = true;
                }
            } else if (stopBtn == ButtonType.VOLUME_DOWN) {
                if (delta > 0 && !stop) {
                    startStopObject.processStop();
                    stop = true;
                }
            }
        } else {
            if (start) {
                if (stopBtn == ButtonType.VOLUME_UP) {
                    if (delta < 0 && !stop) {
                        startStopObject.processStop();
                        stop = true;
                    }
                } else if (stopBtn == ButtonType.VOLUME_DOWN) {
                    if (delta > 0 && !stop) {
                        startStopObject.processStop();
                        stop = true;
                    }
                }
            } else {
                if (startBtn == ButtonType.VOLUME_UP) {
                    if (delta < 0 && !start) {
                        startStopObject.processStart();
                        start = true;
                    }
                } else if (startBtn == ButtonType.VOLUME_DOWN) {
                    if (delta > 0 && !start) {
                        startStopObject.processStart();
                        start = true;
                    }
                }
            }
        }

        originalVolume = currentVolume;

    }

}