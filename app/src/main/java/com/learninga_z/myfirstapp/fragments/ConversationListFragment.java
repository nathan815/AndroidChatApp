package com.learninga_z.myfirstapp.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.learninga_z.myfirstapp.activities.ConversationActivity;
import com.learninga_z.myfirstapp.activities.NewConversationActivity;
import com.learninga_z.myfirstapp.adapters.ConversationListAdapter;
import com.learninga_z.myfirstapp.models.Conversation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ConversationListFragment extends Fragment {
    private static final String TAG = "ConversationListFrag";

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
        FloatingActionButton fab = view.findViewById(R.id.fab);
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
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Conversation conversation = convoList.get(position);
                askToDelete(conversation);
                return true;
            }
        });
    }

    private void askToDelete(final Conversation conversation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.dialog_delete_conversation_message, conversation.name))
                .setTitle(R.string.dialog_delete_conversation_title);
        builder.setPositiveButton(R.string.dialog_delete_conversation_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               deleteConversation(conversation);
            }
        });
        builder.setNegativeButton(R.string.dialog_delete_conversation_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteConversation(final Conversation conversation) {
        conversation.deleteForUser(currentUser.getUid());
        conversationsRef.document(conversation.conversationId).set(conversation);
        Log.d(TAG, "Deleted " + conversation);
    }

    private void openConversation(Conversation conversation) {
        Intent intent = new Intent(getActivity(), ConversationActivity.class);
        intent.putExtra("conversation", conversation);
        startActivity(intent);
    }

    private void showProgress() {
        if(progressOverlayView != null && convoList.isEmpty())
            progressOverlayView.setVisibility(View.VISIBLE);
    }
    private void hideProgress() {
        if(progressOverlayView != null)
            progressOverlayView.setVisibility(View.GONE);
    }


    private void listenForConversations() {
        showProgress();
        convoList.clear();

        final ConversationListAdapter conversationListAdapter = new ConversationListAdapter(getActivity(), android.R.layout.simple_selectable_list_item, convoList);
        listView.setAdapter(conversationListAdapter);

        conversationListener = conversationsRef
        .whereEqualTo("users." + currentUser.getUid(),true)
        .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                hideProgress();
                if(documentSnapshots != null) {
                    processConversationSnapshots(documentSnapshots);
                    sortConversations();
                }
                conversationListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void processConversationSnapshots(QuerySnapshot snapshots) {
        for (DocumentChange dc : snapshots.getDocumentChanges()) {
            Conversation conversation = dc.getDocument().toObject(Conversation.class);
            switch (dc.getType()) {
                case ADDED:
                    convoList.add(conversation);
                    Log.v(TAG, "Added conversation: " + conversation);
                    break;

                case MODIFIED:
                    int convoIndex = findConversationIndex(conversation);
                    // if it can't be found, just add it
                    if (convoIndex == -1)
                        convoList.add(conversation);
                    else
                        convoList.set(convoIndex, conversation);
                    Log.v(TAG, "Modified conversation: " + conversation);
                    break;

                case REMOVED:
                    convoList.remove(conversation);
                    Log.v(TAG, "Removed conversation: " + conversation);
                    break;
            }
        }
    }

    private int findConversationIndex(Conversation conversation) {
        int index = 0;
        int convoIndex = -1;
        for(Conversation convo : convoList) {
            if(convo.equals(conversation)) {
                convoIndex = index;
            }
            index++;
        }
        return convoIndex;
    }

    private void sortConversations() {
        Log.d(TAG, "Begin sorting list");
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
