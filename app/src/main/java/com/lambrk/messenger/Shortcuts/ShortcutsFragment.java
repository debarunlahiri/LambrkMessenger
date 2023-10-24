package com.lambrk.messenger.Shortcuts;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.lambrk.messenger.R;

public class ShortcutsFragment extends Fragment {

    private Context mContext;


    private Button bCreate;

    public ShortcutsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shortcuts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = getActivity();

        bCreate = view.findViewById(R.id.bCreate);

        bCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent addVideoIntent = new Intent(mContext, AddVideoActivity.class);
//                startActivity(addVideoIntent);
            }
        });
    }
}