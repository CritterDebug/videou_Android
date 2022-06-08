package com.secondShot.videou;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;

import java.util.Timer;
import java.util.TimerTask;


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
    private Timer timer;

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
        timer = new Timer();
        if (this.startBtn == ButtonType.PLAY_PAUSE || this.stopBtn == ButtonType.PLAY_PAUSE) {
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

        // We can safely assume speech recognition handled for start
        if (startBtn == ButtonType.NOT_ENABLED) {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (musicInitActive && start && musicInitActive != musicPrevActive) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (stopBtn == ButtonType.PLAY_PAUSE) {
                                    stop = true;
                                    startStopObject.processStop();
                                }
                            }
                        });
                    } else if (!musicInitActive && start && musicInitActive == musicPrevActive) {
                        System.out.println("YOOOOOOO");
                    }
                    musicPrevActive = audioManager.isMusicActive();

//                    if (audioManager.isMusicActive() == musicPrevActive) {
//                                musicPrevActive = audioManager.isMusicActive();
//                                if (!musicInitActive && musicPrevActive == musicInitActive) {
//                                    System.out.println("MAGICALLY CALLED Here");
//                                } else if (musicInitActive && musicPrevActive == musicInitActive) {
//                                    if (stopBtn == ButtonType.PLAY_PAUSE) {
//                                        stop = true;
//                                        startStopObject.processStop();
//                                    }
//                                } else {
//                                    if (stopBtn == ButtonType.PLAY_PAUSE) {
//                                        stop = true;
//                                        startStopObject.processStop();
//                                    }
//                                }
//                            }
//                        });
//                    }

                }
            }, timerLagInt, 250);
        } else {
            // code is good from here
            timer.scheduleAtFixedRate(new TimerTask() {
                boolean firstTime = false;
                @Override
                public void run() {
                    if (audioManager.isMusicActive() != musicPrevActive) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                // used for start and stop with music active at start
                                if (startEnabled && musicInitActive && !firstTime) {
                                    firstTime = true;
                                    KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY);
                                    audioManager.dispatchMediaKeyEvent(event);
                                    start = true;
                                    startStopObject.processStart();
                                    return;
                                }
                                // keep it there -- DONT KNOW WHY
                                musicPrevActive = audioManager.isMusicActive();
                                // ------ //
                                if (!startEnabled) {
                                    if (!musicInitActive && musicPrevActive == musicInitActive) {

                                    } else if (musicInitActive && musicPrevActive == musicInitActive) {
                                        if (stopBtn == ButtonType.PLAY_PAUSE) {
                                            stop = true;
                                            startStopObject.processStop();
                                        }
                                    } else {
                                        if (stopBtn == ButtonType.PLAY_PAUSE) {
                                            stop = true;
                                            startStopObject.processStop();
                                        }
                                    }
                                } else {
                                    if (start) {
                                        if (stopBtn == ButtonType.PLAY_PAUSE) {
                                            stop = true;
                                            startStopObject.processStop();
                                        }
                                    } else {
                                        if (startBtn == ButtonType.PLAY_PAUSE) {
                                            start = true;
                                            startStopObject.processStart();
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }, 0, 250);
        }
    }

    public void setStart(boolean startPref) {
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

    public void remove(){
        timer.purge();
        timer.cancel();
    }

}