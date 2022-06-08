package com.secondShot.videou.ui.help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.secondShot.videou.R;
import com.secondShot.videou.databinding.FragmentFaqBinding;

import java.util.ArrayList;

public class Faq extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Faq() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Faq.
     */
    // TODO: Rename and change types and number of parameters
    public static Faq newInstance(String param1, String param2) {
        Faq fragment = new Faq();
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

    private FragmentFaqBinding binding;
    private View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentFaqBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        courseRV = root.findViewById(R.id.idRVCourse);

        ArrayList<String> al = new ArrayList<>();

        al.add("It will connect to any bluetooth headset that has a microphone and is of a headset profile");
        al.add("Storing files in a designated location means tracking your historical progress more accurately, because when you move" +
                "videos around later to a folder it recreates the time stamp to the current date/time. " +
                "Please note that it will only access files in 'DCIM' or 'Pictures' folders, the folder 'Gallery' has been discontinued and as such the app can only read but not write to it.");
        al.add("It will automatically choose the highest resolution supported up to 1080 x 1920 at roughly 24fps " +
                "In the future this may be changed to enable user selection");
        al.add("Is audible through the speak or bluetooth headset. " +
                "Occasionally it may seem laggy, but it is still accurate and re-adjusts the remaining time");
        al.add("If you are having trouble being voice recognised, try using this to see what the program is picking up ");
        al.add("In this case use the play pause button for starting");
        al.add("1.15");

        ArrayList<String> title = new ArrayList<>();

        title.add("(1/6) What bluetooth device will connect to the app");
        title.add("(2/6) Storage Location");
        title.add("(3/6) Recording Quality");
        title.add("(4/6) Timer");
        title.add("(5/6) Purpose of Auto Re-Arm");
        title.add("(6/6) Purpose of Display Speech Recognition");
        title.add("Version");

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