package com.learninga_z.myfirstapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.learninga_z.myfirstapp.R;
import com.learninga_z.myfirstapp.adapters.UserListAdapter;
import com.learninga_z.myfirstapp.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UsersFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UsersFragment extends Fragment {
    private static final String TAG = "UsersFragment";

    private OnFragmentInteractionListener mListener;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private CollectionReference usersRef;
    private ListenerRegistration usersListener;

    private View progressOverlayView;
    private ListView listView;
    private List<User> userList = new ArrayList<>();

    public UsersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UsersFragment.
     */
    public static UsersFragment newInstance() {
        UsersFragment fragment = new UsersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        usersRef = db.collection("users");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        progressOverlayView = view.findViewById(R.id.progress_circle);
        listView = view.findViewById(R.id.users_list_view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        listenForUsers();
    }

    @Override
    public void onPause() {
        super.onPause();
        usersListener.remove();
    }

    private void listenForUsers() {
        showProgress();
        userList.clear();

        final UserListAdapter userListAdapter = new UserListAdapter(getActivity(), android.R.layout.simple_selectable_list_item, userList);
        listView.setAdapter(userListAdapter);

        usersListener = usersRef
        .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                hideProgress();
                if(documentSnapshots != null) {
                    processUserSnapshots(documentSnapshots);
                }
                userListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void processUserSnapshots(QuerySnapshot snapshots) {
        for (DocumentChange dc : snapshots.getDocumentChanges()) {
            User user = dc.getDocument().toObject(User.class);
            switch (dc.getType()) {
                case ADDED:
                    userList.add(user);
                    Log.v(TAG, "Added user: " + user);
                    break;

                case MODIFIED:
                    break;

                case REMOVED:
                    userList.remove(user);
                    Log.v(TAG, "Removed user: " + user);
                    break;
            }
        }
    }

    private void showProgress() {
        if(progressOverlayView != null && userList.isEmpty())
            progressOverlayView.setVisibility(View.VISIBLE);
    }
    private void hideProgress() {
        if(progressOverlayView != null)
            progressOverlayView.setVisibility(View.GONE);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction();
    }
}
