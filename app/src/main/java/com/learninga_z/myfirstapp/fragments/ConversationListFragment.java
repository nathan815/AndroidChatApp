package com.learninga_z.myfirstapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.learninga_z.myfirstapp.R;
import com.learninga_z.myfirstapp.activities.ConversationActivity;
import com.learninga_z.myfirstapp.activities.NewConversationActivity;
import com.learninga_z.myfirstapp.adapters.ConversationListAdapter;
import com.learninga_z.myfirstapp.models.Conversation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConversationListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConversationListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConversationListFragment extends Fragment {
    private static final String TAG = "ConversationListFragmen";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private View progressOverlayView;
    private ListView listView;
    private List<Conversation> convoList = new ArrayList<>();

    private CollectionReference conversationsRef;
    private ListenerRegistration conversationListener;

    public ConversationListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConversationListFragment.
     */
    public static ConversationListFragment newInstance(String param1, String param2) {
        ConversationListFragment fragment = new ConversationListFragment();
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

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        conversationsRef = db.collection("conversations");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversation_list, container, false);

        progressOverlayView = view.findViewById(R.id.progress_circle);
        listView = view.findViewById(R.id.convo_list);

        registerFabClickHandler(view);
        registerListViewClickHandler();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
    public void onResume() {
        listenForConversations();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        conversationListener.remove();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void registerFabClickHandler(View view) {
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), NewConversationActivity.class);
                startActivity(i);
            }
        });
    }

    private void registerListViewClickHandler() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Conversation conversation = convoList.get(position);
                openConversation(conversation);
            }
        });
    }

    private void openConversation(Conversation conversation) {
        Intent intent = new Intent(getActivity(), ConversationActivity.class);
        intent.putExtra("conversation_id", conversation.conversationId);
        intent.putExtra("conversation_name", conversation.name);
        startActivity(intent);
    }

    private void showProgress() {
        progressOverlayView.setVisibility(View.VISIBLE);
    }
    private void hideProgress() {
        progressOverlayView.setVisibility(View.GONE);
    }


    private void listenForConversations() {
        showProgress();
        final ConversationListAdapter conversationListAdapter = new ConversationListAdapter(getActivity(), android.R.layout.simple_selectable_list_item, convoList);
        listView.setAdapter(conversationListAdapter);
        conversationListener = conversationsRef
                .whereEqualTo("users." + currentUser.getUid(),true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        convoList.clear();
                        Log.d(TAG, "Loaded data");
                        hideProgress();
                        if(documentSnapshots != null) {
                            for (DocumentSnapshot snapshot : documentSnapshots) {
                                Conversation conversation = snapshot.toObject(Conversation.class);
                                convoList.add(conversation);
                                Log.d(TAG, "Adding " + conversation);
                            }
                        }
                        sortConversations();
                        conversationListAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void sortConversations() {
        Log.d(TAG, "Begin sorting");
        Collections.sort(convoList, new Comparator<Conversation>() {
            public int compare(Conversation c1, Conversation c2) {
                if(c1.updatedOn == null || c2.updatedOn == null)
                    return 0;
                return c1.updatedOn.compareTo(c2.updatedOn);
            }
        });
        Collections.reverse(convoList);
        Log.d(TAG, "End sorting");
    }
}
