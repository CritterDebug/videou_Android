package com.secondShot.videou.ui.help;


import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.NavController;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.secondShot.videou.MainActivity;
import com.secondShot.videou.R;
import com.secondShot.videou.databinding.FragmentHelpBinding;


public class HelpFragment extends Fragment {

    private HelpViewModel helpViewModel;
    private FragmentHelpBinding binding;
    private View root;

    private final String STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    private final int STORAGE_CODE_PERMISSION = 102;

    private TabLayout tabLayout;
    private ViewPager pager2;
    private FragmentAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        helpViewModel = new ViewModelProvider((ViewModelStoreOwner) this).get(HelpViewModel.class);
        binding = FragmentHelpBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        tabLayout = root.findViewById(R.id.tab_layout);
        pager2 = root.findViewById(R.id.view_pager2);
        tabLayout.setupWithViewPager(pager2);
        adapter = new FragmentAdapter(getChildFragmentManager());
        adapter.addFrag(new Usage(), "How to Use");
        adapter.addFrag(new Troubleshooting(), "Trouble Shooting");
        adapter.addFrag(new Faq(), "FAQS");
        pager2.setAdapter(adapter);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavController navController = ((MainActivity) getActivity()).getNavController();
                navController.navigate(R.id.action_navigation_help_to_navigation_settings2);
                if (ContextCompat.checkSelfPermission(getContext(), STORAGE_PERMISSION) != PackageManager.PERMISSION_GRANTED){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ActivityCompat.requestPermissions(getActivity(), new String[] {STORAGE_PERMISSION}, STORAGE_CODE_PERMISSION);
                    }
                }
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        return root;

    }

}


