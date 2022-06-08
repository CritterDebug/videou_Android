package com.secondShot.videou.ui.help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.secondShot.videou.R;
import com.secondShot.videou.databinding.FragmentTroubleshootingBinding;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Troubleshooting#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Troubleshooting extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Troubleshooting() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Troubleshooting.
     */
    // TODO: Rename and change types and number of parameters
    public static Troubleshooting newInstance(String param1, String param2) {
        Troubleshooting fragment = new Troubleshooting();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private RecyclerView courseRV;
    private ArrayList<CardModel> courseModelArrayList;

    private FragmentTroubleshootingBinding binding;
    private View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentTroubleshootingBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        courseRV = root.findViewById(R.id.idRVCourse);

        ArrayList<String> al = new ArrayList<>();

        al.add("This is because the app doesn't register the button click but rather a change in volume");
        al.add("If bt selected as a preference but no bt device connected it will automatically use voice activation instead");
        al.add("For it to be quick and accurate, try to keep the word distinct, eg. unicorn. " +
                "Please avoid using voice recognition if listening for over 30 seconds, " +
                "instead use the Bluetooth start button feature");
        al.add("This is because the gallery only shows the last video in the folder not necessarily the newest item");
        al.add("This is because the app is only able show folders in the DCIM or Pictures sub-directory. " +
                "If your current folder is stored under gallery or something else, you will not be able to save videos there");
        al.add("This means that you don't have to go back to your phone to re-record. It automatically re-arms the camera after a recording is finished");

        ArrayList<String> title = new ArrayList<>();

        title.add("(1/6) Why is using the volume up start button on max volume decreasing volume by one");
        title.add("(2/6) Why did it go back to voice recognition when preference was bt start btn");
        title.add("(3/6) Why is voice recognition taking so long");
        title.add("(4/6) Gallery not showing newest item in folder");
        title.add("(5/6) Storage Location dropdown not showing all my folders");
        title.add("(6/6) I want my music to start once I start the recording");

        courseModelArrayList = new ArrayList<>();

        for (String string : al) {
            courseModelArrayList.add(new CardModel(string, title.get(al.indexOf(string))));
        }

        CardAdapter courseAdapter = new CardAdapter(getContext(), courseModelArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        // in below two lines we are setting layoutmanager and adapter to our recycler view.
        courseRV.setLayoutManager(linearLayoutManager);
        courseRV.setAdapter(courseAdapter);

        return root;

    }
}