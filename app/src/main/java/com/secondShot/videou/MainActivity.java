package com.secondShot.videou;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

import com.secondShot.videou.databinding.ActivityMainBinding;
import org.jetbrains.annotations.NotNull;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private final String[] REQUIRED_PERMISSIONS = new String[]{
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.WRITE_EXTERNAL_STORAGE",
    };

    private final String PREFERENCE_FILE_NAME = "preferenceList";
    private final String PERMISSION_GRANTED = "permissionsGranted";
    private final int REQUEST_CODE_PERMISSIONS = 101;

    private final String STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    private final String STORAGE_LOCATION_GRANTED = "storageLocationGranted";
    private final int STORAGE_CODE_PERMISSION = 102;

    private final String FIRST_TIME = "firstTime";

    private NavController navController;
    private Menu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);

        if (!sharedPreferences.contains(FIRST_TIME) || !sharedPreferences.getBoolean(FIRST_TIME, false)) {
            sharedPreferences.edit().putBoolean(FIRST_TIME, true).apply();
            navController.navigate(R.id.action_navigation_home_to_navigation_help2);
        }

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull @NotNull NavController controller, @NonNull @NotNull NavDestination destination, @Nullable @org.jetbrains.annotations.Nullable Bundle arguments) {
                if (destination.getId() == R.id.navigation_home) {
                    getSupportActionBar().hide();
                }
                if (destination.getId() == R.id.navigation_settings) {
                    getSupportActionBar().setTitle("Settings");
                    try {
                        MenuItem shareItem = menu.findItem(R.id.btnHelp);
                        if (!shareItem.isVisible()) {
                            shareItem.setVisible(true);
                        }
                    } catch (NullPointerException ignored) { }
                }
                if (destination.getId() == R.id.navigation_help) {
                    getSupportActionBar().setTitle("Help");
                    try {
                        MenuItem shareItem = menu.findItem(R.id.btnHelp);
                        if (shareItem.isVisible()) {
                            shareItem.setVisible(false);
                        }
                    } catch (NullPointerException ignored) { }
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        this.menu = menu;
        if (navController.getCurrentDestination().getId() == R.id.navigation_help) {
            getSupportActionBar().setTitle("Help");
            MenuItem shareItem = menu.findItem(R.id.btnHelp);
            if (shareItem.isVisible()) {
                shareItem.setVisible(false);
            }
        }
        return true;
    }

    public NavController getNavController() {
        return navController;
    }

    public Menu getMenu() {
        return menu;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnHelp:
                navController.navigate(R.id.action_navigation_settings_to_navigation_help2);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                sharedPreferences.edit().putBoolean(PERMISSION_GRANTED, true).apply();
                sharedPreferences.edit().putBoolean(STORAGE_LOCATION_GRANTED, true).apply();
            } else {
                sharedPreferences.edit().putBoolean(PERMISSION_GRANTED, true).apply();
                Toast.makeText(this, "Please grant all 3 permissions in order to use the app: Camera, Storage and Microphone", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == STORAGE_CODE_PERMISSION) {
            if (ContextCompat.checkSelfPermission(this, STORAGE_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
                sharedPreferences.edit().putBoolean(STORAGE_LOCATION_GRANTED, true).apply();
            } else {
                sharedPreferences.edit().putBoolean(STORAGE_LOCATION_GRANTED, false).apply();
            }
        }
    }

    public boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}