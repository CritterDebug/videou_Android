package com.secondShot.videou.ui.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import com.google.common.util.concurrent.ListenableFuture;
import com.secondShot.videou.MainActivity;
import com.secondShot.videou.R;
import com.secondShot.videou.databinding.FragmentSettingsBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;
    private FragmentSettingsBinding binding;
    private View root;

    private Switch lblSpeechRecognition;
    private Switch switchGridLines;
    private Switch reArm;
    private Switch startBtnEnabled;

    private ArrayList<HashMap<String, String>> hashMapItem = new ArrayList<HashMap<String, String>>();

    private final String PERMISSION_GRANTED = "permissionsGranted";
    private final int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[] {
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.WRITE_EXTERNAL_STORAGE",
    };
    private final String FILE_LOCATION_GRANTED = "storageLocationGranted";
    private final String STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    private final int STORAGE_CODE_PERMISSION = 102;

    private final String PREFERENCE_FILE_NAME = "preferenceList";
    private final String CAMERA_RESOLUTION = "cameraResolution";
    private final String CAMERA_RESOLUTION_LIST = "cameraResolutionList";
    private final String FILE_LOCATION = "fileLocation";
    private final String START_RECORDING = "startRecording";
    private final String GRIDLINES = "gridLines";
    private final String LBL_SPEECH_RECOGNITION = "lblSpeechRecognition";
    private final String TIMER = "timer";
    private final String RE_ARM = "reArm";
    private final String START_BTN_PREF = "startBtnPref";
    private final String START_BTN = "startBtn";
    private final String SAVE_BTN = "saveBtn";

    private ArrayList<File> storageLocationsList = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        createPreferencesList();
        createDropDowns();

        if ( !checkStoragePermission() ) {
            if (ContextCompat.checkSelfPermission(getContext(), STORAGE_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{STORAGE_PERMISSION}, STORAGE_CODE_PERMISSION);
                }
            }
        }

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavController navController = ((MainActivity) getActivity()).getNavController();
                root.findViewById(R.id.loadingContainer).setVisibility(View.VISIBLE);
                navController.navigate(R.id.action_navigation_settings_to_navigation_home2);
                if ( !allPermissionsGranted() ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
                    }
                }
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        return root;
    }

    public List<Quality> getCameraResolution() {
        ProcessCameraProvider cameraProvider = null;
        VideoCapture videoCapture = new VideoCapture.Builder().build();
        Camera camera = null;
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        try {
            cameraProvider.unbindAll();
        } catch (NullPointerException npe ) { }
        try {
            cameraProvider = cameraProviderFuture.get();
        } catch (ExecutionException | InterruptedException e) { }
        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, videoCapture);
        } catch (Exception exception) { }
        return QualitySelector.getSupportedQualities(camera.getCameraInfo());
    }

    private boolean checkStoragePermission() {
        if(ContextCompat.checkSelfPermission( SettingsFragment.this.requireContext(), STORAGE_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            updateBoolPreference(FILE_LOCATION_GRANTED, false);
            return false;
        }
        updateBoolPreference(FILE_LOCATION_GRANTED, true);
        return true;
    }

    public void createPreferencesList( ) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFERENCE_FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (!sharedPreferences.contains(FILE_LOCATION)) {
            if ( checkStoragePermission() ) {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
                File path = new File(file.getAbsolutePath(), "/Camera");
                editor.putString(FILE_LOCATION, path.getAbsolutePath());
            } else {
                editor.putString(FILE_LOCATION, "-");
            }
        }
        if (!sharedPreferences.contains(FILE_LOCATION_GRANTED) ) {
            editor.putBoolean(FILE_LOCATION_GRANTED, false);
        }
        if (!sharedPreferences.contains(CAMERA_RESOLUTION) ) {
            String quality;
            try {
                quality = getCameraResolution().get(0).toString();
                if (quality.contains("name=UHD")) {
                    quality = "UHD 2160p";
                } else if (quality.contains("name=FHD")) {
                    quality = "FHD 1080p";
                } else if (quality.contains("name=HD")) {
                    quality = "HD 720p";
                } else if (quality.contains("name=SD")) {
                    quality = "SD 480p";
                }
            } catch (NullPointerException npe) {
                quality = "NONE";
            }
            editor.putString(CAMERA_RESOLUTION, quality);
        }
        if (!sharedPreferences.contains(CAMERA_RESOLUTION_LIST) ) {
            Set<String> cameraResolutionList = new HashSet<>();
            for ( Quality quality : getCameraResolution() ) {
                String result = null;
                if (quality.toString().contains("name=UHD")) {
                    result = "UHD 2160p";
                } else if (quality.toString().contains("name=FHD")) {
                    result = "FHD 1080p";
                } else if (quality.toString().contains("name=HD")) {
                    result = "HD 720p";
                } else if (quality.toString().contains("name=SD")) {
                    result = "SD 480p";
                }
                cameraResolutionList.add(result);
            }
            editor.putStringSet(CAMERA_RESOLUTION_LIST, cameraResolutionList);
        }
        if (!sharedPreferences.contains(START_RECORDING) ) {
            editor.putString(START_RECORDING, "start");
        }
        if (!sharedPreferences.contains(GRIDLINES) ) {
            editor.putBoolean(GRIDLINES, false);
        }
        if (!sharedPreferences.contains(PERMISSION_GRANTED) ) {
            if ( allPermissionsGranted() ) {
                editor.putBoolean(PERMISSION_GRANTED, true);
            } else {
                editor.putBoolean(PERMISSION_GRANTED, false);
            }
        }
        if (!sharedPreferences.contains(LBL_SPEECH_RECOGNITION) ) {
            editor.putBoolean(LBL_SPEECH_RECOGNITION, false);
        }
        if (!sharedPreferences.contains(TIMER) ) {
            editor.putString(TIMER, "off");
        }
        if (!sharedPreferences.contains(START_BTN_PREF) ) {
            editor.putBoolean(START_BTN_PREF, true);
        }
        if (!sharedPreferences.contains(START_BTN) ) {
            editor.putString(START_BTN, "volume up");
        }
        if (!sharedPreferences.contains(SAVE_BTN) ) {
            editor.putString(SAVE_BTN, "volume down");
        }
        if (!sharedPreferences.contains(RE_ARM) ) {
            editor.putBoolean(RE_ARM, false);
        }
        editor.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void createDropDowns() {

        // bluetoothList Section

        ArrayList bluetoothList = new ArrayList<HashMap<String, String>>();

        HashMap<String, String> hashMapItem = new HashMap<String, String>();
        hashMapItem.put("line1", "Start Button");
        hashMapItem.put("line2", getPreference(START_BTN));
        bluetoothList.add(hashMapItem);
        hashMapItem = new HashMap<String, String>();
        hashMapItem.put("line1", "Save Button");
        hashMapItem.put("line2", getPreference(SAVE_BTN));
        bluetoothList.add(hashMapItem);

        SimpleAdapter bluetoothListAdapter = new SimpleAdapter( getActivity(), bluetoothList, android.R.layout.simple_list_item_2,
                new String[] { "line1", "line2" }, new int[] { android.R.id.text1, android.R.id.text2 } );
        ListView bluetoothListView = root.findViewById(R.id.bluetoothView);
        bluetoothListView.setDivider(Drawable.createFromPath("android:color/transparent"));
        bluetoothListView.setAdapter(bluetoothListAdapter);

        ArrayList<String> startBtnList = new ArrayList<>();
        startBtnList.add("volume down");
        startBtnList.add("play/pause");
        startBtnList.add("volume up");

        ArrayList<String> saveBtnList = new ArrayList<>();
        saveBtnList.add("volume down");
        saveBtnList.add("play/pause");
        saveBtnList.add("volume up");

        bluetoothListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch ((int) id) {
                    case 0:
                        PopupMenu popupMenu = new PopupMenu(SettingsFragment.super.getContext(), view);
                        for (String string : startBtnList) {
                            popupMenu.getMenu().add(string);
                        }
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.toString().equals(getPreference(SAVE_BTN)) && !item.toString().equals("play/pause")) {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                                    alertDialog.setTitle("Error");
                                    alertDialog.setMessage("Cant have the same Start and Save volume button");
                                    alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    alertDialog.show();
                                } else {
                                    updatePreference(START_BTN, item.toString());
                                    bluetoothList.remove(0);
                                    HashMap<String, String> hashMapItem = new HashMap<String, String>();
                                    hashMapItem.put("line1", "Start Button");
                                    hashMapItem.put("line2", item.toString());
                                    bluetoothList.add(0, hashMapItem);
                                    bluetoothListAdapter.notifyDataSetChanged();
                                }
                                return true;
                            }
                        });
                        popupMenu.show();
                        break;
                    case 1:
                        PopupMenu popupMenu1 = new PopupMenu(SettingsFragment.super.getContext(), view);
                        for (String string : saveBtnList) {
                            popupMenu1.getMenu().add(string);
                        }
                        popupMenu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.toString().equals(getPreference(START_BTN)) && !item.toString().equals("play/pause")) {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                                    alertDialog.setTitle("Error");
                                    alertDialog.setMessage("Cant have the same Start and Save volume button");
                                    alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    alertDialog.show();
                                } else {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                                    alertDialog.setTitle("Recommendation");
                                    alertDialog.setMessage("If wanting music fully uninterrupted, it's recommended that you use volume up for start and volume down for save");
                                    alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                            updatePreference(SAVE_BTN, item.toString());
                                            bluetoothList.remove(1);
                                            HashMap<String, String> hashMapItem = new HashMap<String, String>();
                                            hashMapItem.put("line1", "Save Button");
                                            hashMapItem.put("line2", item.toString());
                                            bluetoothList.add(1, hashMapItem);
                                            bluetoothListAdapter.notifyDataSetChanged();
                                        }
                                    });
                                    alertDialog.show();
                                }
                                return true;
                            }
                        });
                        popupMenu1.show();
                        break;
                    default:
                        break;
                }
            }
        });

        // General

        ArrayList<HashMap<String, String>> generalList = new ArrayList<HashMap<String, String>>();

        hashMapItem = new HashMap<String, String>();
        hashMapItem.put("line1", "Timer");
        hashMapItem.put("line2", getPreference(TIMER));
        generalList.add(hashMapItem);
        hashMapItem = new HashMap<String, String>();
        hashMapItem.put("line1", "Camera Resolution");
        hashMapItem.put("line2", getPreference(CAMERA_RESOLUTION));
        generalList.add(hashMapItem);
        hashMapItem = new HashMap<String, String>();
        hashMapItem.put("line1", "Storage Location");
        hashMapItem.put("line2", getPreference(FILE_LOCATION));
        generalList.add(hashMapItem);

        SimpleAdapter generalListAdapter = new SimpleAdapter( getActivity(), generalList, android.R.layout.simple_list_item_2,
                new String[] { "line1", "line2" }, new int[] { android.R.id.text1, android.R.id.text2 } );
        ListView generalListView = root.findViewById(R.id.generalView);
        generalListView.setDivider(Drawable.createFromPath("android:color/transparent"));
        generalListView.setAdapter(generalListAdapter);

        generalListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch((int) id) {
                    case 0:
                        PopupMenu popupMenu2 = new PopupMenu( SettingsFragment.super.getContext(), view);
                        popupMenu2.getMenu().add("off");
                        popupMenu2.getMenu().add("1 second");
                        popupMenu2.getMenu().add("3 seconds");
                        popupMenu2.getMenu().add("5 seconds");
                        popupMenu2.getMenu().add("10 seconds");
                        popupMenu2.getMenu().add("15 seconds");
                        popupMenu2.getMenu().add("20 seconds");
                        popupMenu2.getMenu().add("30 seconds");
                        popupMenu2.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                updatePreference(TIMER, item.toString());
                                generalList.remove(0);
                                HashMap<String, String> hashMapItem = new HashMap<String, String>();
                                hashMapItem.put("line1", "Timer");
                                hashMapItem.put("line2", item.toString());
                                generalList.add(0, hashMapItem);
                                generalListAdapter.notifyDataSetChanged();
                                return true;
                            }
                        });
                        popupMenu2.show();
                        break;
                    case 1:
                        PopupMenu popupMenu1 = new PopupMenu( SettingsFragment.super.getContext(), view);
                        for ( Quality string : getCameraResolution() ) {
                            String result = null;
                            if (string.toString().contains("name=UHD")) {
                                result = "UHD 2160p";
                            } else if (string.toString().contains("name=FHD")) {
                                result = "FHD 1080p";
                            } else if (string.toString().contains("name=HD")) {
                                result = "HD 720p";
                            } else if (string.toString().contains("name=SD")) {
                                result = "SD 480p";
                            }
                            popupMenu1.getMenu().add(result);
                        }
                        popupMenu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                updatePreference(CAMERA_RESOLUTION, item.toString());
                                generalList.remove(1);
                                HashMap<String, String> hashMapItem = new HashMap<String, String>();
                                hashMapItem.put("line1", "Camera Resolution");
                                hashMapItem.put("line2", item.toString());
                                generalList.add(1, hashMapItem);
                                generalListAdapter.notifyDataSetChanged();
                                return true;
                            }
                        });
                        popupMenu1.show();
                        break;
                    case 2:
                        if ( !checkStoragePermission() ) {
                            if (ContextCompat.checkSelfPermission(getContext(), STORAGE_PERMISSION) == PackageManager.PERMISSION_DENIED){
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                                alertDialog.setTitle("Storage Location Permission Required");
                                alertDialog.setMessage("Please grant the storage permission in order to access this");
                                alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                                        startActivity(intent);
                                    }
                                });
                                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                alertDialog.show();
                            } else if (ContextCompat.checkSelfPermission(getContext(), STORAGE_PERMISSION) != PackageManager.PERMISSION_GRANTED){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    ActivityCompat.requestPermissions(getActivity(), new String[]{STORAGE_PERMISSION}, STORAGE_CODE_PERMISSION);
                                }
                            }
                        } else {
                            storageLocationsList = getStorageLocations();
                            PopupMenu popupMenu = new PopupMenu( SettingsFragment.super.getContext(), view);
                            for ( File file : storageLocationsList ) {
                                popupMenu.getMenu().add(file.toString());
                            }
                            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem item) {
                                    updatePreference(FILE_LOCATION, item.toString());
                                    generalList.remove(2);
                                    HashMap<String, String> hashMapItem = new HashMap<String, String>();
                                    hashMapItem.put("line1", "Storage Location");
                                    hashMapItem.put("line2", item.toString());
                                    generalList.add(2, hashMapItem);
                                    generalListAdapter.notifyDataSetChanged();
                                    return true;
                                }
                            });
                            popupMenu.show();
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        // Experimental

        ArrayList<HashMap<String, String>> experimentalList = new ArrayList<HashMap<String, String>>();

        hashMapItem = new HashMap<String, String>();
        hashMapItem.put("line1", "Start Recording Word / Phrase");
        hashMapItem.put("line2", getPreference(START_RECORDING));
        experimentalList.add(hashMapItem);

        SimpleAdapter experimentalListAdapter = new SimpleAdapter( getActivity(), experimentalList, android.R.layout.simple_list_item_2,
                new String[] { "line1", "line2" }, new int[] { android.R.id.text1, android.R.id.text2 } );
        ListView experimentalListView = root.findViewById(R.id.experimentalView);
        experimentalListView.setDivider(Drawable.createFromPath("android:color/transparent"));
        experimentalListView.setAdapter(experimentalListAdapter);

        experimentalListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch ((int) id) {
                    case 0:
                        AlertDialog.Builder startAlertDialogBuilder = new AlertDialog.Builder(getContext());
                        startAlertDialogBuilder.setTitle("Start Recording Word");
                        startAlertDialogBuilder.setMessage("Please enter the word/s (less than 5 words) you want to say to start recording");
                        LinearLayout startAlertDialogLayout = new LinearLayout(SettingsFragment.super.getContext());
                        startAlertDialogLayout.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams startAlertDialogLayoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        startAlertDialogLayoutParams.setMargins(70, 0, 70, 0);
                        EditText startWordEditText = new EditText(SettingsFragment.super.getContext());

                        startWordEditText.setSingleLine(true);
                        startWordEditText.setSelected(true);
                        startWordEditText.setActivated(true);
                        startWordEditText.setClickable(false);
                        startWordEditText.setTextIsSelectable(false);
                        startWordEditText.setFocusable(true);

                        startAlertDialogLayout.addView(startWordEditText, startAlertDialogLayoutParams);
                        startWordEditText.setText(getPreference(START_RECORDING));
                        startAlertDialogBuilder.setView(startAlertDialogLayout);
                        startAlertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }
                        });
                        startAlertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        AlertDialog startAlertDialog = startAlertDialogBuilder.create();
                        startAlertDialog.show();
                        startAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String text = startWordEditText.getText().toString();
                                boolean test = false;
                                if (text.equals("PPP")) {
                                    ImageView ryanSmythe = root.findViewById(R.id.imgMyFavRanga);
                                    ryanSmythe.setVisibility(View.VISIBLE);
                                    Timer timer = new Timer();
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ryanSmythe.setVisibility(View.INVISIBLE);
                                                }
                                            });
                                        }
                                    }, 5000);
                                }
                                if (text.isEmpty() || isMaxWords(text) || isUniqueWords(text)) {
                                    test = true;
                                }
                                if (!test) {
                                    updatePreference(START_RECORDING, text);
                                    experimentalList.remove(0);
                                    HashMap<String, String> hashMapItem = new HashMap<String, String>();
                                    hashMapItem.put("line1", "Start Recording Word");
                                    hashMapItem.put("line2", text);
                                    experimentalList.add(0, hashMapItem);
                                    experimentalListAdapter.notifyDataSetChanged();
                                    startAlertDialog.dismiss();
                                } else {
                                    startWordEditText.setHint("Please enter text");
                                    startWordEditText.setText("");
                                }
                            }
                        });
                        break;
                    default:
                        break;
                }
            }
        });

        switchGridLines = root.findViewById(R.id.gridlines);
        switchGridLines.setChecked(getBoolPreference(GRIDLINES));
        switchGridLines.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateBoolPreference(GRIDLINES, isChecked);
            }
        });

        lblSpeechRecognition = root.findViewById(R.id.lblSpeechRecognition);
        lblSpeechRecognition.setChecked(getBoolPreference(LBL_SPEECH_RECOGNITION));
        lblSpeechRecognition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateBoolPreference(LBL_SPEECH_RECOGNITION, isChecked);
            }
        });

        startBtnEnabled = root.findViewById(R.id.startBtnEnabled);
        startBtnEnabled.setChecked(getBoolPreference(START_BTN_PREF));
        startBtnEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateBoolPreference(START_BTN_PREF, isChecked);
            }
        });

        reArm = root.findViewById(R.id.reArm);
        reArm.setChecked(getBoolPreference(RE_ARM));
        reArm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateBoolPreference(RE_ARM, isChecked);
                if (isChecked) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    alertDialog.setTitle("Still experimental");
                    alertDialog.setMessage("Useful for making multiple recordings without having to go back to your phone each time to restart the arming process. Can be problematic but is still useful");
                    alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialog.show();
                }
            }
        });

    }

    public boolean isMaxWords( String userInput ) {
        String userInputSplit[] = userInput.split(" ");
        ArrayList<String> al = new ArrayList<>(Arrays.asList(userInputSplit));
        if ( al.size() > 5 ) {
            return true;
        }
        return false;
    }

    public boolean isUniqueWords( String userInput ) {
        String userInputSplit[] = userInput.split(" ");
        ArrayList<String> al = new ArrayList<>(Arrays.asList(userInputSplit));
        Set<String> set = new LinkedHashSet<>(Arrays.asList(userInputSplit));
        if ( al.size() != set.size() ) {
            return true;
        }
        return false;
    }

    public ArrayList<File> getStorageLocations() {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsoluteFile().getParentFile();
        File[] initFilesList = new File(path.toString()).listFiles();
        ArrayList<String> filesList = new ArrayList<>();
        for(File file : initFilesList) {
            // potentially add something to keep gallery working if os version <= 10 or maybe its <= 9
            if ( file.getName().equals("DCIM") || file.getName().equals("Pictures") ) {
                filesList.add(file.getAbsolutePath());
            }
        }
        ArrayList<File> fnlFilesList = new ArrayList<>();
        for ( String s : filesList ) {
            ArrayList<File> tempFile = new ArrayList<>(Arrays.asList(new File(s).listFiles()));
            for ( File f : tempFile ) {
                if ( f.isDirectory() ) {
                    fnlFilesList.add(f);
                }
            }
        }
        if ( fnlFilesList.isEmpty() ) { // IF BY SOME MIRACLE THE USER DOESNT HAVE ANY PHOTOS IT CREATES A NEW FOLDER. PHOTOS APP CANT SEE PHOTO!!!!! :(
            File newFile = new File( path.toString() + "/DCIM/New Folder");
            newFile.mkdir();
            fnlFilesList.add(newFile);
        }
        Collections.sort(fnlFilesList, new Comparator<File>() {
            @Override
            public int compare(File s1, File s2) {
                return s1.getAbsolutePath().toString().compareToIgnoreCase(s2.getAbsolutePath().toString());
            }
        });

        return fnlFilesList;
    }

    public String getPreference( String key ) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFERENCE_FILE_NAME,
                Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, new String());
    }

    public boolean getBoolPreference( String key ) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFERENCE_FILE_NAME,
                Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, false);
    }

    public void updateBoolPreference( String key, boolean preference) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFERENCE_FILE_NAME,
                Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(key, preference).apply();
    }

    public ArrayList<String> getPreferenceList( String key ) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFERENCE_FILE_NAME,
                Context.MODE_PRIVATE);
        Set<String> set = sharedPreferences.getStringSet(key, new HashSet<>());
        return new ArrayList<>(set);
    }

    public void updatePreference( String key, String preference) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFERENCE_FILE_NAME,
                Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(key, preference).apply();
    }

    public boolean allPermissionsGranted() {
        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}