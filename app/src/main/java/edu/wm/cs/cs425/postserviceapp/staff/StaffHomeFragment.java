package edu.wm.cs.cs425.postserviceapp.staff;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.wm.cs.cs425.postserviceapp.LoginActivity;
import edu.wm.cs.cs425.postserviceapp.R;
import edu.wm.cs.cs425.postserviceapp.ui.dashboard.DashboardFragment;

/**
 * fragment class corresponds to Staff Homepage
 */
public class StaffHomeFragment extends Fragment {

    private TextView enter;
    private TextView request;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //back to login page if onBackPressed()
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(getContext(), LoginActivity.class));
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);

        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_staff_home, container, false);
        enter=view.findViewById(R.id.enter);
        request=view.findViewById(R.id.request);
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(StaffHomeFragment.this)
                        .navigate(R.id.action_staffHomeFragment_to_dashboardFragment);
            }
        });

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(StaffHomeFragment.this)
                        .navigate(R.id.action_staffHomeFragment_to_showScheduleFragment);
            }
        });
        return view;
    }
}