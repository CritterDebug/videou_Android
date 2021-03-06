package com.secondShot.videou.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.media.AudioManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraXConfig;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.FocusMeteringResult;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.ViewPort;
import androidx.camera.core.impl.utils.executor.CameraXExecutors;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.camera.video.FallbackStrategy;
import androidx.camera.video.PendingRecording;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.video.FileOutputOptions;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.PermissionChecker;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.NavController;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.secondShot.videou.MainActivity;
import com.secondShot.videou.R;
import com.secondShot.videou.SettingsContentObserver;
import com.secondShot.videou.StartStopObject;
import com.secondShot.videou.databinding.FragmentHomeBinding;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.codec.language.DoubleMetaphone;


public class HomeFragment extends Fragment implements CameraXConfig.Provider, TextToSpeech.OnInitListener {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private View root;

    private Button btnPrep;
    private Button btnFlip;
    private Button btnFlash;
    private Button btnSettings;
    private ImageButton btnGallery;
    private TextView recordingTimeStamp;
    private TextView bluetoothText;
    private CircleOutline circleOutline;
    private ConstraintLayout constraintLayout;
    private GridOutline gridOutline;
    private Uri videoURI;

    private Timer removeCircleTimer;
    private Timer zoomTimer;
    private Timer exposureTimer;

    private int sec;
    private int min;
    private int timerPreferenceInt;
    private TextView hudTimer;

    private Recording recording;
    private PendingRecording pendingRecording;
    private VideoCapture<Recorder> videoCapture;
    private PreviewView previewView;
    private Preview preview;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;
    private Camera camera;
    private CameraControl cameraControl;
    private ViewPort viewPort;

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private TextView lblRecognisedText;

    private BroadcastReceiver btOnOffReceiver;
    private BroadcastReceiver broadcastReceiver;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothHeadset btHeadset;
    private BluetoothProfile.ServiceListener mProfileListener;
    private BluetoothDevice deviceConnected;

    private AudioManager mAudioManager;
    private boolean musicActiveOnStart = false;
    private TextToSpeech textToSpeech;
    private boolean isRecording = false;

    private int btnPressState = 0;
    private boolean lblSpeechRecognitionOnOff;

    private final int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.BLUETOOTH",
    };

    private SharedPreferences sharedPreferences;
    private final int STORAGE_CODE_PERMISSION = 102;
    private final String STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    private final String PREFERENCE_FILE_NAME = "preferenceList";
    private final String FILE_LOCATION = "fileLocation";
    private final String START_RECORDING = "startRecording";
    private final String GRIDLINES = "gridLines";
    private final String CAMERA_RESOLUTION = "cameraResolution";
    private final String LBL_SPEECH_RECOGNITION = "lblSpeechRecognition";
    private final String TIMER = "timer";
    private final String START_BTN_PREF = "startBtnPref";
    private final String START_BTN = "startBtn";
    private final String SAVE_BTN = "saveBtn";
    private final String RE_ARM = "reArm";

    private SettingsContentObserver settingsContentObserver;
    private StartStopObject startStopObject;

    private Handler recordingTextViewHandler;
    private Runnable recordingTextViewRunnable;
    private Runnable runnable;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        homeViewModel = new ViewModelProvider((ViewModelStoreOwner) this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        // keep it here for first time app users if they dont grant permission
        btnSettings = root.findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navHostFragment = ((MainActivity) getActivity()).getNavController();
                navHostFragment.navigate(R.id.action_navigation_home_to_navigation_settings2);
                ActivityCompat.requestPermissions(getActivity(), new String[]{STORAGE_PERMISSION}, STORAGE_CODE_PERMISSION);
            }
        });
        textToSpeech = new TextToSpeech(getContext(), this);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        return root;

    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable @org.jetbrains.annotations.Nullable Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public File getGalleryDrawable() {
        File newFile = null;
        File file = new File(sharedPreferences.getString(FILE_LOCATION, new String()));
        if (file.getAbsolutePath().equals("/")) {
            return newFile;
        }
        File[] fileList = file.getAbsoluteFile().listFiles();
        if (fileList != null) {
            Arrays.sort(fileList, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                }
            });
            for (File file1 : fileList) {
                if (file1.getAbsolutePath().contains(".mp4")) {
                    newFile = new File(file1.getAbsolutePath());
                }
            }
        }
        return newFile;
    }

    public void updateGalleryDrawable() {
        File file = getGalleryDrawable();
        if (getGalleryDrawable() != null) {
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 52, getContext().getResources().getDisplayMetrics());
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 52, getContext().getResources().getDisplayMetrics());
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            RoundedBitmapDrawable bdrawable = RoundedBitmapDrawableFactory.create(getContext().getResources(), bitmap);
            bdrawable.setCircular(true);
            btnGallery.setImageDrawable(bdrawable);
        } else {
            Drawable bitmap = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_image_not_supported_24, null);
            VectorDrawableCompat emptyGallery = VectorDrawableCompat.create(getResources(), R.drawable.empty_gallery, null);
            Drawable ripple = ResourcesCompat.getDrawable(getResources(), R.drawable.ripple_button_curved, null);
            btnGallery.setBackground(emptyGallery);
            btnGallery.setForeground(ripple);
            btnGallery.setImageDrawable(bitmap);
        }
    }

    @SuppressLint("MissingPermission")
    public void init() {
        lblRecognisedText = root.findViewById(R.id.lblRecognisedText);
        constraintLayout = root.findViewById(R.id.layoutHome);
        recordingTimeStamp = root.findViewById(R.id.recordingTimeStamp);
        bluetoothText = root.findViewById(R.id.textBluetooth);
        previewView = root.findViewById(R.id.viewFinder);
        lblSpeechRecognitionOnOff = getBoolPreference(LBL_SPEECH_RECOGNITION);
        hudTimer = root.findViewById(R.id.hudTimerLbl);

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        sharedPreferences = getActivity().getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);

        sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(GRIDLINES)) {
                    setGridLines(getBoolPreference(GRIDLINES));
                }
            }
        });

        startBluetoothChecks();

        btnGallery = root.findViewById(R.id.btnGallery);
        if (getGalleryDrawable() != null) {
            videoURI = FileProvider.getUriForFile(getContext(), getContext().getPackageName(), getGalleryDrawable());
        } else {
            videoURI = null;
        }

        File file = getGalleryDrawable();
        if (getGalleryDrawable() != null) {
            Bitmap bitmap;
            bitmap = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
            if (bitmap != null) {
                int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 52, getContext().getResources().getDisplayMetrics());
                int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 52, getContext().getResources().getDisplayMetrics());
                bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                RoundedBitmapDrawable bdrawable = RoundedBitmapDrawableFactory.create(getContext().getResources(), bitmap);
                bdrawable.setCircular(true);
                Drawable whiteOutline = ResourcesCompat.getDrawable(getResources(), R.drawable.rounded_corners, null);
                Drawable ripple = ResourcesCompat.getDrawable(getResources(), R.drawable.ripple_button_curved, null);
                btnGallery.setBackground(whiteOutline);
                btnGallery.setForeground(ripple);
                btnGallery.setImageDrawable(bdrawable);
            }
        }

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoURI != null) {
                    Intent intent = ShareCompat.IntentBuilder.from(getActivity())
                            .setStream(videoURI)
                            .setType("text/html")
                            .getIntent()
                            .setAction(Intent.ACTION_VIEW)
                            .setDataAndType(videoURI, "video/*")
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivity(intent);
                }
            }
        });

        btnFlip = root.findViewById(R.id.btnFlip);
        btnFlip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraSelector.getLensFacing() == CameraSelector.LENS_FACING_BACK) {
                    cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                            .build();
                } else {
                    cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build();
                }
                try {
                    startCamera();
                } catch (ExecutionException | InterruptedException | CameraAccessException | IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnPrep = root.findViewById(R.id.customButton);
        btnPrep.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        if (btnPressState == 1) {
                            try {
                                removeSpeech();
                                showAppIcons();
                                turnOffBluetoothCall();
                                startStopObject = null;
                                getContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
                                settingsContentObserver = null;
                            } catch (NullPointerException ignored) { }
                            hudTimer.setVisibility(View.INVISIBLE);
                            btnPrep.setBackground(getResources().getDrawable(R.drawable.arming_button));
                            btnPressState = 0;
                        } else if (btnPressState == 0) {
                            if (!new File(getPreference(FILE_LOCATION), "").isDirectory()) {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                                alertDialog.setTitle("Error");
                                alertDialog.setMessage("File storage location no longer exists. Please choose a new one");
                                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                alertDialog.show();
                                btnPrep.setBackground(getResources().getDrawable(R.drawable.arming_button));
                                break;
                            }
                            if (bluetoothAdapter.isEnabled() && deviceConnected != null) {
                                if (!mAudioManager.isMusicActive()) {
                                    Toast.makeText(getContext(), "Please make sure a music service is active", Toast.LENGTH_SHORT).show();
                                }
                            }
                            if (!bluetoothAdapter.isEnabled()) {
                                implementListener();
                                btnPrep.setBackground(getResources().getDrawable(R.drawable.listening_button));
                            } else if (bluetoothAdapter.isEnabled() && getBoolPreference(START_BTN_PREF) == true) {
                                startStopImplementListener();
                                btnPrep.setBackground(getResources().getDrawable(R.drawable.pending_button));
                            } else {
                                implementListener();
                                btnPrep.setBackground(getResources().getDrawable(R.drawable.listening_button));
                            }
                            btnPressState++;
                        } else if (btnPressState == 2) {
                            hudTimer.setVisibility(View.INVISIBLE);
                            handlerStopVideo();
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        btnFlash = root.findViewById(R.id.btnFlash);
        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera.getCameraInfo().getTorchState().getValue() == 0) {
                    btnFlash.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.outline_flash_on_24));
                    camera.getCameraControl().enableTorch(true);
                } else {
                    camera.getCameraControl().enableTorch(false);
                    btnFlash.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_flash_off_24));
                }
            }
        });

        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        try {
            startCamera();
        } catch (ExecutionException | IOException | InterruptedException | CameraAccessException e) {
            e.printStackTrace();
        }

//        Display display = getActivity().getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        int difference = size.y - (int) (size.x * 16 / 9);

//        ConstraintLayout.LayoutParams layoutParamsPrep = (ConstraintLayout.LayoutParams) btnPrep.getLayoutParams();
//        layoutParamsPrep.setMargins(
//                ((ConstraintLayout.LayoutParams) btnPrep.getLayoutParams()).leftMargin,
//                ((ConstraintLayout.LayoutParams) btnPrep.getLayoutParams()).topMargin,
//                ((ConstraintLayout.LayoutParams) btnPrep.getLayoutParams()).rightMargin,
//                difference + 60);
//        btnPrep.setLayoutParams(layoutParamsPrep);
//
//
//        ConstraintLayout.LayoutParams layoutParamsGallery = (ConstraintLayout.LayoutParams) btnGallery.getLayoutParams();
//        layoutParamsGallery.setMargins(
//                ((ConstraintLayout.LayoutParams) btnGallery.getLayoutParams()).leftMargin,
//                ((ConstraintLayout.LayoutParams) btnGallery.getLayoutParams()).topMargin,
//                ((ConstraintLayout.LayoutParams) btnGallery.getLayoutParams()).rightMargin,
//                difference + 84);
//        btnGallery.setLayoutParams(layoutParamsGallery);

    }

    public void setGridLines(boolean input) {
        if (input) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

//            int difference = size.y - (int) (size.x * 16 / 9);

//            ConstraintLayout.LayoutParams layoutParamsPrep = (ConstraintLayout.LayoutParams) btnPrep.getLayoutParams();
//            layoutParamsPrep.setMargins(
//                    ((ConstraintLayout.LayoutParams) btnPrep.getLayoutParams()).leftMargin,
//                    ((ConstraintLayout.LayoutParams) btnPrep.getLayoutParams()).topMargin,
//                    ((ConstraintLayout.LayoutParams) btnPrep.getLayoutParams()).rightMargin,
//                    ((ConstraintLayout.LayoutParams) btnPrep.getLayoutParams()).bottomMargin+difference/2);
//            btnPrep.setLayoutParams(layoutParamsPrep);
//
//            ConstraintLayout.LayoutParams layoutParamsGallery = (ConstraintLayout.LayoutParams) btnGallery.getLayoutParams();
//            layoutParamsGallery.setMargins(
//                    ((ConstraintLayout.LayoutParams) btnGallery.getLayoutParams()).leftMargin,
//                    ((ConstraintLayout.LayoutParams) btnGallery.getLayoutParams()).topMargin,
//                    ((ConstraintLayout.LayoutParams) btnGallery.getLayoutParams()).rightMargin,
//                    ((ConstraintLayout.LayoutParams) btnGallery.getLayoutParams()).bottomMargin+difference/2);
//            btnGallery.setLayoutParams(layoutParamsGallery);

            if (viewPort.getAspectRatio().equals(new Rational(4,3))) {
                height = (int) (size.x * 4 / 3);
            } else if (viewPort.getAspectRatio().equals(new Rational(9,16))) {
                height = (int) (size.x * 16 / 9);
            }
            gridOutline = new GridOutline(getContext(), width, height);
            constraintLayout.addView(gridOutline);
        } else {
            constraintLayout.removeView(gridOutline);
        }
    }

    public void updateTimeStamp() {
        sec = 0;
        min = 0;
        recordingTimeStamp.setVisibility(View.VISIBLE);
        recordingTimeStamp.setText(String.format("%02d", min) + ":" + String.format("%02d", sec));
        if (recordingTextViewHandler == null) {
            recordingTextViewHandler = new Handler();
        }
        recordingTextViewRunnable = new Runnable() {
            @Override
            public void run() {
                sec++;
                if (sec > 60) {
                    min++;
                    sec = 0;
                }
                recordingTimeStamp.setText(String.format("%02d", min) + ":" + String.format("%02d", sec));
                recordingTextViewHandler.postDelayed(this, 1000);
            }
        };
        recordingTextViewHandler.postDelayed(recordingTextViewRunnable, 1000);
    }

    public void startBluetoothChecks() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getContext(), "Bluetooth not available on this device", Toast.LENGTH_SHORT).show();
        } else {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothText.setText("Not Connected");
                bluetoothOn();
            } else {
                bluetoothText.setText("Turned Off");
                tempBluetoothOff();
            }
            btOnOffReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();
                    if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                        switch (state) {
                            case BluetoothAdapter.STATE_OFF:
                                bluetoothText.setText("Turned Off");
                                tempBluetoothOff();
                                break;
                            case BluetoothAdapter.STATE_ON:
                                bluetoothText.setText("Not Connected");
                                bluetoothOn();
                                break;
                        }
                    }
                }
            };
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            getContext().registerReceiver(btOnOffReceiver, filter);
        }
    }

    public void turnOffBluetoothCall() {
        try {
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.stopBluetoothSco();
            mAudioManager.setMicrophoneMute(false);
            mAudioManager.setSpeakerphoneOn(false);
        } catch (NullPointerException ignored) { }
    }

    public void removeSpeech() {
        try {
            speech.cancel();
            speech.stopListening();
            speech.destroy();
        } catch (NullPointerException ignored) { }
        speech = null;
        recognizerIntent = null;
    }

    public void showAppIcons() {
        btnFlip.setVisibility(View.VISIBLE);
        btnSettings.setVisibility(View.VISIBLE);
        btnGallery.setVisibility(View.VISIBLE);
    }

    // check this // used for speech recogniser
    public void implementListener() {
        if (speech == null) {
            createSpeechRecogniserListener();
        }

        if (getPreference(SAVE_BTN).equals("volume up")) {
            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            if (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == maxVolume) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume - 1, AudioManager.FLAG_SHOW_UI);
            }
        } else if (getPreference(SAVE_BTN).equals("volume down")) {
            if (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 1, AudioManager.FLAG_SHOW_UI);
            }
        }

        startStopObject = new StartStopObject();
        startStopObject.setCustomObjectListener(new StartStopObject.StartStopListener() {
            boolean counter = false;

            @Override
            public void confirmedStart() { }

            @Override
            public void confirmedStop() {
                if (isRecording) {
                    if (getPreference(SAVE_BTN).equals("play/pause")) {
                        if (!counter) {
                            counter = true;
                            if (getPreference(SAVE_BTN).equals("volume up")) {
                                int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                if (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == maxVolume) {
                                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume - 1, AudioManager.FLAG_SHOW_UI);
                                }
                            } else if (getPreference(SAVE_BTN).equals("volume down")) {
                                if (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
                                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 1, AudioManager.FLAG_SHOW_UI);
                                }
                            }
                        } else {
                            handlerStopVideo();
                            isRecording = false;
                        }
                    } else {
                        handlerStopVideo();
                        isRecording = false;
                    }
                }
            }
        });
        int originalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        String saveBtn = getPreference(SAVE_BTN);
        int startBtnInt = 0;
        int saveBtnInt = 0;

        if (saveBtn.equals("volume down")) {
            saveBtnInt = 3;
        } else if (saveBtn.equals("volume up")) {
            saveBtnInt = 2;
        } else if (saveBtn.equals("play/pause")) {
            saveBtnInt = 1;
        }

        if (getBoolPreference(START_BTN_PREF)) {
            String startBtn = getPreference(START_BTN);
            if (startBtn.equals("volume down")) {
                startBtnInt = 3;
            } else if (startBtn.equals("volume up")) {
                startBtnInt = 2;
            } else if (startBtn.equals("play/pause")) {
                startBtnInt = 1;
            }
        }

        settingsContentObserver = new SettingsContentObserver(getContext(), new Handler(), originalVolume, startStopObject, getBoolPreference(START_BTN_PREF), startBtnInt, saveBtnInt);
        // not sure if this is going to bug out
        getContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, settingsContentObserver);
        btnFlip.setVisibility(View.INVISIBLE);
        btnSettings.setVisibility(View.INVISIBLE);
        btnGallery.setVisibility(View.INVISIBLE);

        // possibly just keep looping through until we hear the phrase or it matches closely

        if (mAudioManager.isMusicActive()) {
            musicActiveOnStart = true;
        }

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled() && deviceConnected != null) {
            musicActiveOnStart = false;
            if (mAudioManager.isMusicActive()) {
                musicActiveOnStart = true;
            }
            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            mAudioManager.setBluetoothScoOn(true);
            mAudioManager.startBluetoothSco();
            mAudioManager.setMicrophoneMute(false);
            mAudioManager.setSpeakerphoneOn(false);

            mAudioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE));

        }
        speech.startListening(recognizerIntent);
    }

    // check this
    public void startStopImplementListener() {

        if (getBoolPreference(START_BTN_PREF)) {
            if (getPreference(START_BTN).equals("volume up")) {
                int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                if (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == maxVolume) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume - 1, AudioManager.FLAG_SHOW_UI);
                }
            } else if (getPreference(START_BTN).equals("volume down")) {
                if (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 1, AudioManager.FLAG_SHOW_UI);
                }
            }
        }

        startStopObject = new StartStopObject();
        startStopObject.setCustomObjectListener(new StartStopObject.StartStopListener() {
            @Override
            public void confirmedStart() {
                if (bluetoothAdapter.isEnabled() && getBoolPreference(START_BTN_PREF)) {
                    handlerPrepStartVideo();
                }
            }

            @Override
            public void confirmedStop() {
                if (isRecording) {
                    handlerStopVideo();
                    isRecording = false;
                }
            }
        });

        int originalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        String saveBtn = getPreference(SAVE_BTN);
        int startBtnInt = 0;
        int saveBtnInt = 0;
        if (saveBtn.equals("volume down")) {
            saveBtnInt = 3;
        } else if (saveBtn.equals("volume up")) {
            saveBtnInt = 2;
        } else if (saveBtn.equals("play/pause")) {
            saveBtnInt = 1;
        }
        if (getBoolPreference(START_BTN_PREF)) {
            String startBtn = getPreference(START_BTN);
            if (startBtn.equals("volume down")) {
                startBtnInt = 3;
            } else if (startBtn.equals("volume up")) {
                startBtnInt = 2;
            } else if (startBtn.equals("play/pause")) {
                startBtnInt = 1;
            }
        }
        settingsContentObserver = new SettingsContentObserver(getContext(), new Handler(), originalVolume, startStopObject, getBoolPreference(START_BTN_PREF), startBtnInt, saveBtnInt);
        getContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, settingsContentObserver);
        btnFlip.setVisibility(View.INVISIBLE);
        btnSettings.setVisibility(View.INVISIBLE);
        btnGallery.setVisibility(View.INVISIBLE);

        if (mAudioManager.isMusicActive()) {
            musicActiveOnStart = true;
        }

        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                musicActiveOnStart = false;
                if (mAudioManager.isMusicActive()) {
                    musicActiveOnStart = true;
                }
                if (!getBoolPreference(START_BTN_PREF)) {
                    mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    mAudioManager.setBluetoothScoOn(true);
                    mAudioManager.startBluetoothSco();
                    mAudioManager.setMicrophoneMute(false);
                    mAudioManager.setSpeakerphoneOn(false);

                    mAudioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
                } else {
                    mAudioManager.setMode(AudioManager.MODE_NORMAL);
                }
            }
        }
    }

    // Code is good from here

    public void bluetoothOff() {
        try {
            bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, btHeadset);
        } catch (NullPointerException | IllegalArgumentException ignored) {
        }
        try {
            getContext().unregisterReceiver(btOnOffReceiver);
        } catch (NullPointerException | IllegalArgumentException ignored) {
        }

        try {
            getContext().unregisterReceiver(broadcastReceiver);
        } catch (NullPointerException | IllegalArgumentException ignored) {
        }
        btHeadset = null;
        bluetoothAdapter = null;
        mProfileListener = null;
        deviceConnected = null;
    }

    public void bluetoothOn() {
        // grabs initial bt connected devices when app service started

        mProfileListener = new BluetoothProfile.ServiceListener() {
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.HEADSET) {
                    btHeadset = (BluetoothHeadset) proxy;

                    if (getActivity().checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) // asks for android 12
                        {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                            return;
                        }
                    }

                    if (btHeadset.getConnectedDevices().size() > 0) { // asking if devices are currently connected
                        deviceConnected = btHeadset.getConnectedDevices().get(0);
                        bluetoothText.setText("Connected " + deviceConnected.getName());
                    }
                }
            }

            public void onServiceDisconnected(int profile) {
                if (profile == BluetoothProfile.A2DP) {
                    btHeadset = null;
                }
            }
        };

        bluetoothAdapter.getProfileProxy(getContext(), mProfileListener, BluetoothProfile.HEADSET);

        // when receiving a bluetooth device connect of disconnect request
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    deviceConnected = device;
                    bluetoothText.setText("Connected " + deviceConnected.getName());
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    if (btHeadset.getConnectedDevices().size() > 0) {
                        deviceConnected = btHeadset.getConnectedDevices().get(0);
                        bluetoothText.setText("Connected " + deviceConnected.getName());
                    } else {
                        bluetoothText.setText("Not Connected");
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        getContext().registerReceiver(broadcastReceiver, filter);
    }

    public void tempBluetoothOff() {
        try {
            bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, btHeadset);
        } catch (NullPointerException | IllegalArgumentException ignored) {
        }
        try {
            getContext().unregisterReceiver(broadcastReceiver);
        } catch (NullPointerException | IllegalArgumentException ignored) {
        }
        btHeadset = null;
        deviceConnected = null;
    }

    public void handlerPrepStartVideo() {

        if (!getBoolPreference(START_BTN_PREF)) {
            removeSpeech();
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            mAudioManager.setBluetoothScoOn(true); // needed
            mAudioManager.stopBluetoothSco();
            mAudioManager.setMicrophoneMute(false);
            mAudioManager.setSpeakerphoneOn(false);
        }

        btnFlip.setVisibility(View.VISIBLE);
        btnPrep.setBackground(getResources().getDrawable(R.drawable.recording_button));
        btnPressState = 2;

        String date = (new SimpleDateFormat("yyyyMMdd_HHmmSS", Locale.US)).format(System.currentTimeMillis());
        File file = new File(sharedPreferences.getString(FILE_LOCATION, ""), date + ".mp4");
        FileOutputOptions fileOutputOptions = new FileOutputOptions.Builder(file).build();

        pendingRecording = videoCapture
                .getOutput()
                .prepareRecording(getContext(), fileOutputOptions);

        if (PermissionChecker.checkSelfPermission(getContext(), "android.permission.RECORD_AUDIO") == PermissionChecker.PERMISSION_GRANTED) {
            pendingRecording.withAudioEnabled();
        }

        String timerPreference = getPreference(TIMER);
        boolean timerOn = false;
        if (!timerPreference.equals("off")) {
            timerOn = true;
        }
        timerPreferenceInt = 0;
        if (timerOn) {
            switch (timerPreference) {
                case "1 second":
                    timerPreferenceInt = 1;
                    break;
                case "3 seconds":
                    timerPreferenceInt = 3;
                    break;
                case "5 seconds":
                    timerPreferenceInt = 5;
                    break;
                case "10 seconds":
                    timerPreferenceInt = 10;
                    break;
                case "15 seconds":
                    timerPreferenceInt = 15;
                    break;
                case "20 seconds":
                    timerPreferenceInt = 20;
                    break;
                case "30 seconds":
                    timerPreferenceInt = 30;
                    break;
            }
            hudTimer.setVisibility(View.VISIBLE);
            hudTimer.setText(String.valueOf(timerPreferenceInt));

            recordingTextViewHandler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    textToSpeech.speak(String.valueOf(timerPreferenceInt), TextToSpeech.QUEUE_FLUSH, null, null);
                    hudTimer.setText(String.valueOf(timerPreferenceInt));
                    if (timerPreferenceInt <= 0) {
                        hudTimer.setVisibility(View.INVISIBLE);
                        handlerStartVideo(file);
                        recordingTextViewHandler.removeCallbacks(this);
                    } else {
                        timerPreferenceInt--;
                        recordingTextViewHandler.postDelayed(this, 1000);
                    }
                }
            };
            recordingTextViewHandler.postDelayed(runnable, 0);

        } else {
            handlerStartVideo(file);
        }
    }

    public void handlerStartVideo(File file) {

        updateTimeStamp();

        settingsContentObserver.setStart(true);

        recording = pendingRecording.start(ContextCompat.getMainExecutor(getContext()), recordEvent -> {
            if (recordEvent instanceof VideoRecordEvent.Start) {
//                new Thread(() -> textToSpeech.speak("Recording", TextToSpeech.QUEUE_ADD, null, null)).start();
                Toast.makeText(getContext(), "Recording Started Successfully", Toast.LENGTH_SHORT).show();
            } else if (recordEvent instanceof VideoRecordEvent.Finalize) {
                if (!((VideoRecordEvent.Finalize)recordEvent).hasError()) {
//                    OutputResults outputResults = ((VideoRecordEvent.Finalize)recordEvent).getOutputResults();
                    textToSpeech.speak("Saved", TextToSpeech.QUEUE_FLUSH, null, null);
                    Toast.makeText(getActivity().getApplicationContext(), "Video Saved Successfully", Toast.LENGTH_LONG).show();
                    getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                    updateGalleryDrawable();
                    videoURI = FileProvider.getUriForFile(getContext(), getContext().getPackageName(), getGalleryDrawable());
                } else {
                    textToSpeech.speak("Save Failed", TextToSpeech.QUEUE_FLUSH, null, null);
                    Toast.makeText(getContext(), "Recording Save Failed", Toast.LENGTH_SHORT).show();
                    btnPrep.setBackground(getResources().getDrawable(R.drawable.arming_button));
                    btnPressState = 0;
                    constraintLayout.removeView(circleOutline);
                    recordingTextViewHandler.removeCallbacks(recordingTextViewRunnable);
                    startStopObject = null;
                    try {
                        getContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
                    } catch (NullPointerException ignored ) {}
                    settingsContentObserver = null;
                    recordingTimeStamp.setVisibility(View.INVISIBLE);
                    if (lblSpeechRecognitionOnOff) {
                        lblRecognisedText.setText("");
                        lblRecognisedText.setVisibility(View.INVISIBLE);
                    }
                    showAppIcons();
                    if (getBoolPreference(RE_ARM)) {
                        reArm();
                    }
                }
            }
        });

        isRecording = true;

//        // need to have music playing otherwise wont be able to receive commands
        if ( musicActiveOnStart && !getBoolPreference(START_BTN_PREF) ) {
            KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY);
            mAudioManager.dispatchMediaKeyEvent(event);
        }
    }

    public void handlerStopVideo () {
        try {
            recording.stop();
        } catch (NullPointerException ignored) { }
        try {
            recordingTextViewHandler.removeCallbacks(runnable);
        } catch (NullPointerException ignored) { }
        btnPrep.setBackground(getResources().getDrawable(R.drawable.arming_button));
        btnPressState = 0;
        constraintLayout.removeView(circleOutline);
        recordingTextViewHandler.removeCallbacks(recordingTextViewRunnable);
        startStopObject = null;
        try {
            getContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
        } catch (NullPointerException ignored) { }
        settingsContentObserver = null;
        recordingTimeStamp.setVisibility(View.INVISIBLE);
        if (lblSpeechRecognitionOnOff) {
            lblRecognisedText.setText("");
            lblRecognisedText.setVisibility(View.INVISIBLE);
        }
        showAppIcons();
        if (getBoolPreference(RE_ARM)) {
            reArm();
        }
    }

    public void reArm () {
        if (musicActiveOnStart) {
            mAudioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
        }
        if (!bluetoothAdapter.isEnabled()) {
            implementListener();
            btnPrep.setBackground(getResources().getDrawable(R.drawable.listening_button));
        } else if (bluetoothAdapter.isEnabled() && getBoolPreference(START_BTN_PREF) == true) {
            startStopImplementListener();
            btnPrep.setBackground(getResources().getDrawable(R.drawable.pending_button));
        } else {
            implementListener();
            btnPrep.setBackground(getResources().getDrawable(R.drawable.listening_button));
        }
        btnPressState++;
    }

    public void createSpeechRecogniserListener () {
        speech = SpeechRecognizer.createSpeechRecognizer(getContext());
        recognizerIntent = new Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault());
        speech.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int errorCode) {
                if (lblSpeechRecognitionOnOff) {
                    lblRecognisedText.setText("-failed to recognise");
                }
                if (errorCode == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    speech.cancel();
                } else { }
                speech.cancel();
                speech.startListening(recognizerIntent);
            }

            @Override
            public void onEvent(int arg0, Bundle arg1) {
            }

            @Override
            public void onReadyForSpeech(Bundle arg0) {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (lblSpeechRecognitionOnOff) {
                    lblRecognisedText.setText("");
                    lblRecognisedText.setText(matches.get(0));
                }
                boolean test = computeMetaphone(matches.get(0));
                if (test) {
                    handlerPrepStartVideo();
                    settingsContentObserver.setStart(true);
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (lblSpeechRecognitionOnOff) {
                    lblRecognisedText.setText("");
                    lblRecognisedText.setText(matches.get(0));
                }
                boolean test = computeMetaphone(matches.get(0));
                if (!test) {
                    speech.startListening(recognizerIntent);
                } else {
                    handlerPrepStartVideo();
                    settingsContentObserver.setStart(true);
                }
            }
        });
    }

    public boolean computeMetaphone (String result){
        String requiredString = getActivity().getSharedPreferences(PREFERENCE_FILE_NAME,
                Context.MODE_PRIVATE).getString(START_RECORDING, new String());
        String testString = result;
        DoubleMetaphone dMetaphone = new DoubleMetaphone();
        dMetaphone.setMaxCodeLen(100);
        if (dMetaphone.isDoubleMetaphoneEqual(requiredString, testString)) {
            return true;
        }
        if (!requiredString.contains(" ")) {
            String dmValueRequiredString = dMetaphone.doubleMetaphone(requiredString);
            Set<String> dmArrayListTestString = new LinkedHashSet<>();
            String testStringSplit[] = testString.split(" ");
            List<String> testUniqueWords = new ArrayList<>(Arrays.asList(testStringSplit));
            for (String word : testUniqueWords) {
                dmArrayListTestString.add(dMetaphone.doubleMetaphone(word));
            }
            // need to use set so we can remove duplicates of metaphone words
            dmArrayListTestString = new LinkedHashSet<>(dmArrayListTestString);
            for (String newWord : dmArrayListTestString) {
                int compDiff = computeDifference(dmValueRequiredString, newWord);
                int biggerLengthString = dmValueRequiredString.length();
                if (biggerLengthString < newWord.length()) {
                    biggerLengthString = newWord.length();
                }
                float percentageCorrect = (float) compDiff / (float) biggerLengthString;
                if (percentageCorrect < 0.25) {
                    return true;
                }
            }
        } else {
            ArrayList<String> dmArrayListRequiredString = new ArrayList<>();
            String requiredStringSplit[] = requiredString.split(" ");
            for (String word : requiredStringSplit) {
                dmArrayListRequiredString.add(dMetaphone.doubleMetaphone(word));
            }
            int requiredCorrectWords = dmArrayListRequiredString.size();
            int countedCorrectWords = 0;
            Set<String> dmArrayListTestString = new LinkedHashSet<>();
            String testStringSplit[] = testString.split(" ");
            // need to use set otherwise wont remove duplicates
            Set<String> testUniqueWords = new LinkedHashSet<>(Arrays.asList(testStringSplit));
            for (String word : testUniqueWords) {
                dmArrayListTestString.add(dMetaphone.doubleMetaphone(word));
            }
            // need to use set so we can remove duplicates of metaphone words
            dmArrayListTestString = new LinkedHashSet<>(dmArrayListTestString);
            for (String newTestWord : dmArrayListTestString) {
                for (String newRequiredWord : dmArrayListRequiredString) {
                    int compDiff = computeDifference(newRequiredWord, newTestWord);
                    int biggerLengthString = newRequiredWord.length();
                    if (biggerLengthString < newTestWord.length()) {
                        biggerLengthString = newTestWord.length();
                    }
                    float percentageCorrect = (float) compDiff / (float) biggerLengthString;
                    if (percentageCorrect < 0.25) {
                        if (countedCorrectWords == requiredCorrectWords) {
                            return true;
                        } else {
                            countedCorrectWords++;
                        }
                    }
                }
            }
            if (requiredCorrectWords == countedCorrectWords) {
                return true;
            }
        }
        return false;
    }

    public int computeDifference (String x, String y){
        int[][] dp = new int[x.length() + 1][y.length() + 1];
        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(dp[i - 1][j - 1] +
                            costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)), dp[i - 1][j] + 1, dp[i][j - 1] + 1);
                }
            }
        }
        return dp[x.length()][y.length()];
    }

    public int costOfSubstitution ( char a, char b){
        return a == b ? 0 : 1;
    }

    public int min ( int...numbers){
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

    public void startCamera () throws ExecutionException, InterruptedException, CameraAccessException, IOException {
        try {
            cameraProvider.unbindAll();
        } catch (NullPointerException ignored) { }

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                cameraProvider = null;
                try {
                    cameraProvider = cameraProviderFuture.get();
                    preview = new Preview.Builder()
                            .setCameraSelector(cameraSelector)
                            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                            .build();
                    // TODO check whether to move this line
                    preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

                    previewView.setScaleType(PreviewView.ScaleType.FIT_CENTER);

                    String resolution = getPreference(CAMERA_RESOLUTION);
                    Quality quality;
                    switch (resolution) {
                        case "UHD 2160p":
                            quality = Quality.UHD;
                            break;
                        case "FHD 1080p":
                            quality = Quality.FHD;
                            break;
                        case "HD 720p":
                            quality = Quality.HD;
                            break;
                        case "SD 480p":
                            quality = Quality.SD;
                            break;
                        default:
                            quality = Quality.HIGHEST;
                            break;
                    }

                    Recorder recorder = new Recorder.Builder()
                            .setQualitySelector(QualitySelector.from(quality, FallbackStrategy.lowerQualityOrHigherThan(quality)))
                            .build();

                    videoCapture = VideoCapture.withOutput(recorder);
                    videoCapture.setTargetRotation(((Activity) getContext()).getWindowManager().getDefaultDisplay().getRotation());

//                    viewPort = new ViewPort.Builder(new Rational(binding.viewFinder.getWidth(), binding.viewFinder.getHeight()), preview.getTargetRotation()).build();
//                    viewPort = new ViewPort.Builder(previewView.getViewPort().getAspectRatio(), preview.getTargetRotation()).build();
                    viewPort = new ViewPort.Builder(new Rational(9, 16), preview.getTargetRotation()).build();
//                    viewPort = ((PreviewView)root.findViewById(R.id.viewFinder)).getViewPort();
                    UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
                            .setViewPort(viewPort)
                            .addUseCase(preview)
                            .addUseCase(videoCapture).build();

                    cameraProvider.unbindAll();
                        camera = cameraProvider.bindToLifecycle(HomeFragment.this, cameraSelector, useCaseGroup);
//                    camera = cameraProvider.bindToLifecycle(HomeFragment.this, cameraSelector, preview, videoCapture);
                    cameraControl = camera.getCameraControl();

                    OrientationEventListener orientationEventListener = new OrientationEventListener(getContext()) {
                        @Override
                        public void onOrientationChanged(int orientation) {
                            int rotation;
                            if (orientation >= 45 && orientation < 135) {
                                rotation = Surface.ROTATION_270;
                            } else if (orientation >= 135 && orientation < 225) {
                                rotation = Surface.ROTATION_180;
                            } else if (orientation >= 225 && orientation < 315) {
                                rotation = Surface.ROTATION_90;
                            } else {
                                rotation = Surface.ROTATION_0;
                            }
                            videoCapture.setTargetRotation(rotation);
                        }
                    };
                    orientationEventListener.enable();

                    setGridLines(getBoolPreference(GRIDLINES));

                } catch (ExecutionException | InterruptedException ignored) { }
            }
        }, ContextCompat.getMainExecutor(getContext()));

        TextView textView = root.findViewById(R.id.zoomTextView);
        SeekBar zoomSlider = root.findViewById(R.id.zoomSeekbar);
        SeekBar exposureSlider = root.findViewById(R.id.exposureSeekbar);

        ScaleGestureDetector.SimpleOnScaleGestureListener _simpleListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float currentZoomRatio = camera.getCameraInfo().getZoomState().getValue().getZoomRatio();
                float delta = detector.getScaleFactor();
                cameraControl.setZoomRatio(currentZoomRatio * delta);
                zoomSlider.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                zoomSlider.setProgress((int) (camera.getCameraInfo().getZoomState().getValue().getLinearZoom() * 100));
                return true;
            }
        };

        ScaleGestureDetector mGestureDetector = new ScaleGestureDetector(getContext(), _simpleListener);
        root.findViewById(R.id.viewFinder).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MeteringPointFactory factory = previewView.getMeteringPointFactory();
                        float afPointWidth = 1.0f / 6.0f;  // 1/6 total area
                        float aePointWidth = afPointWidth * 1.5f;
                        MeteringPoint afPoint = factory.createPoint(event.getX(), event.getY(), afPointWidth);
                        MeteringPoint aePoint = factory.createPoint(event.getX(), event.getY(), aePointWidth);
                        FocusMeteringAction action = new FocusMeteringAction.Builder(afPoint, FocusMeteringAction.FLAG_AF)
                                .addPoint(aePoint, FocusMeteringAction.FLAG_AE)
                                .addPoint(aePoint, FocusMeteringAction.FLAG_AWB)
                                .disableAutoCancel()
//                                .setAutoCancelDuration(5, TimeUnit.SECONDS)
                                .build();
                        ListenableFuture future = cameraControl.startFocusAndMetering(action);
                        Runnable listener = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    FocusMeteringResult result = (FocusMeteringResult) future.get();
                                } catch (Exception ignored) { }
                            }
                        };
                        future.addListener(listener, ContextCompat.getMainExecutor(getContext()));
                        try {
                            constraintLayout.removeView(circleOutline);
                            circleOutline = new CircleOutline(getContext(), event.getX(), event.getY(), 200);
                            constraintLayout.addView(circleOutline);
                            removeCircleTimer = new Timer();
                            removeCircleTimer.schedule(new TimerTask() {
                                public void run() {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            constraintLayout.removeView(circleOutline);
                                        }
                                    });
                                }
                            }, 3000);
                        } catch (NullPointerException ignored) { }
                        return true;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                exposureSlider.setVisibility(View.VISIBLE);
                exposureSlider.setProgress(50);
                try {
                    exposureTimer.cancel();
                } catch (NullPointerException ignored) {
                }
                return false;
            }
        });

        exposureSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("UnsafeExperimentalUsageError")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    exposureTimer.cancel();
                } catch (NullPointerException ignored) { }
                int min = camera.getCameraInfo().getExposureState().getExposureCompensationRange().getLower();
                int max = camera.getCameraInfo().getExposureState().getExposureCompensationRange().getUpper();
                if (progress > 50) {
                    int test = (int) (max * ((progress / 100f + progress / 100f) - 1));
                    camera.getCameraControl().setExposureCompensationIndex(test);
                } else if (progress < 50) {
                    int test = (int) (min * (((100f - progress) / 100f) + ((100f - progress) / 100f) - 1f));
                    camera.getCameraControl().setExposureCompensationIndex(test);
                } else {
                    camera.getCameraControl().setExposureCompensationIndex(0);
                }
                exposureTimer = new Timer();
                exposureTimer.schedule(new TimerTask() {
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                exposureSlider.setVisibility(View.INVISIBLE);
                                exposureTimer.cancel();
                            }
                        });
                    }
                }, 3000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        zoomSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                try {
                    zoomTimer.cancel();
                } catch (NullPointerException ignored) { }
                cameraControl.setLinearZoom(progress / 100f);
                float currentZoom = camera.getCameraInfo().getZoomState().getValue().getZoomRatio();
                String zoom = String.valueOf(currentZoom).substring(0, 3);
                textView.setText(zoom + "x");
                int width = seekbar.getWidth() - seekbar.getPaddingLeft() - seekbar.getPaddingRight();
                int thumbPos = seekbar.getPaddingLeft() + width * seekbar.getProgress() / seekbar.getMax();
                textView.measure(0, 0);
                int txtW = textView.getMeasuredWidth();
                int delta = txtW / 2;
                textView.setX(seekbar.getX() + thumbPos - delta);
                textView.setForegroundGravity(Gravity.CENTER_VERTICAL);
                zoomTimer = new Timer();
                zoomTimer.schedule(new TimerTask() {
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.setVisibility(View.INVISIBLE);
                                zoomSlider.setVisibility(View.INVISIBLE);
                                zoomTimer.cancel();
                            }
                        });
                    }
                }, 3000);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekbar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    public String getPreference(String key) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, new String());
    }

    public boolean getBoolPreference(String key) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        unRegisterReceivers();
        removeTimers();

        if ( allPermissionsGranted() ) { // this is important for when changing preferences
            init();
        } else {
            Toast.makeText(getContext(), "Please grant all 3 permissions in order to use the app: Camera, Storage and Microphone", Toast.LENGTH_SHORT).show();
        }
    }

    public void unRegisterReceivers() {
        try {
            getContext().getContentResolver().unregisterContentObserver(settingsContentObserver);
            settingsContentObserver = null;
        } catch (NullPointerException | IllegalArgumentException ignored) { }
    }

    public void removeTimers() {
        if ( removeCircleTimer != null ) {
            removeCircleTimer.purge();
            removeCircleTimer.cancel();
        }
        if ( zoomTimer != null ) {
            zoomTimer.purge();
            zoomTimer.cancel();
        }
        if ( exposureTimer != null ) {
            exposureTimer.purge();
            exposureTimer.cancel();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            removeSpeech();
        } catch ( NullPointerException ignored ) { }
        try {
            handlerStopVideo();
        } catch ( NullPointerException ignored ) { }
        removeTimers();
        bluetoothOff();
        // check with bluetooth
        turnOffBluetoothCall();
        unRegisterReceivers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeTimers();
        bluetoothOff();
        turnOffBluetoothCall();
        unRegisterReceivers();

        try {
            textToSpeech.stop();
            textToSpeech.shutdown();
        } catch (NullPointerException ignored) { }

        binding = null;

//        try {
//            cameraExecutor.shutdown();
//        } catch (NullPointerException npe) { }

        //        cameraProvider.unbindAll();
        //        cameraProvider.shutdown();

    }

    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
                .setMinimumLoggingLevel(Log.ERROR)
                .build();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
        } else {
            Log.e("TTS", "Initialization Failed!");
        }
    }

}