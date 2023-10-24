package com.lambrk.messenger.Chat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.lambrk.messenger.OverallProfileActivity;
import com.lambrk.messenger.R;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lambrk.messenger.Utils.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements ChatMultipleImagesUploadAdapter.OnItemClickListener {

    private static final int PICK_IMAGE = 143;
    private static final String TAG = ChatActivity.class.getSimpleName();
    private EditText etChat;
    private Button chatsendbutton;
    private CircleImageView chatuserprofileCIV;
    private TextView tvChatUserName, tvReplyUsername, tvReplyMessage, tvChatUserIndicator1;
    private RelativeTimeTextView tvChatUserIndicator;
    private CardView chatheadprofileCV;
    private ImageButton chatbackIB, replymessagecloseIB, ibChatAddMedia;
    private CardView messaggereplyCV, cvChatAddMedia;
    private ProgressBar pbMultipleMediaUpload;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private RecyclerView chatRV, rvChatAddMedia, rvChatUploadImages;
    private ChatAdapter chatAdapter;
    private ChatAddMediaAdapter chatAddMediaAdapter;
    private ChatMultipleImagesUploadAdapter chatMultipleImagesUploadAdapter;
    private Context mContext;
    private LinearLayoutManager linearLayoutManager;
    private List<Chat> chatList = new ArrayList<>();
    private List<ChatAddMedia> chatAddMediaList = new ArrayList<>();
    private List<ChatMultipleImagesUpload> chatMultipleImagesUploadList = new ArrayList<>();
    private List<String> mKeys = new ArrayList<>();
    ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
    ArrayList<Boolean> progressImageUpload = new ArrayList<Boolean>();

    private String searched_user_id;
    private String user_id;
    public boolean isOnline = false;

    private String searched_user_name;
    private String searched_profile_image;
    private String reply_chat_id, reply_message, reply_sender_user_id;
    private int reply_chat_position = 0;

    private Timer timer = new Timer();
    private final long DELAY = 400; // milliseconds
    boolean isTyping = false;
    private Bitmap mCompressedStoryImage;
    String imageEncoded;
    List<String> imagesEncodedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mContext = ChatActivity.this;

        Bundle bundle = getIntent().getExtras();
        searched_user_id = bundle.get("searched_user_id").toString();

        etChat = findViewById(R.id.etChat);
        chatsendbutton = findViewById(R.id.chatsendbutton);
        chatuserprofileCIV = findViewById(R.id.chatuserprofileCIV);
        tvChatUserName = findViewById(R.id.tvChatUserName);
        tvChatUserIndicator = findViewById(R.id.tvChatUserIndicator);
        chatheadprofileCV = findViewById(R.id.chatheadprofileCV);
        chatbackIB = findViewById(R.id.chatbackIB);
        messaggereplyCV = findViewById(R.id.messaggereplyCV);
        tvReplyUsername = findViewById(R.id.tvReplyUsername);
        tvReplyMessage = findViewById(R.id.tvReplyMessage);
        replymessagecloseIB = findViewById(R.id.replymessagecloseIB);
        tvChatUserIndicator1 = findViewById(R.id.tvChatUserIndicator1);
        ibChatAddMedia = findViewById(R.id.ibChatAddMedia);
        cvChatAddMedia = findViewById(R.id.cvChatAddMedia);
        pbMultipleMediaUpload = findViewById(R.id.pbMultipleMediaUpload);

        chatRV = findViewById(R.id.chatRV);
        chatAdapter = new ChatAdapter(chatList, mContext);
        linearLayoutManager = new LinearLayoutManager(mContext);
        chatRV.setLayoutManager(linearLayoutManager);
        chatRV.setAdapter(chatAdapter);
        //linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        rvChatAddMedia = findViewById(R.id.rvChatAddMedia);
        rvChatUploadImages = findViewById(R.id.rvChatUploadImages);
        chatAddMediaAdapter = new ChatAddMediaAdapter(mContext, chatAddMediaList);
        chatMultipleImagesUploadAdapter = new ChatMultipleImagesUploadAdapter(mContext, mArrayUri, this);
        rvChatAddMedia.setAdapter(chatAddMediaAdapter);
        rvChatUploadImages.setAdapter(chatMultipleImagesUploadAdapter);
        rvChatUploadImages.setLayoutManager(new LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false));
        rvChatAddMedia.setLayoutManager(new GridLayoutManager(mContext, 1));

        rvChatUploadImages.setVisibility(View.GONE);
        pbMultipleMediaUpload.setVisibility(View.GONE);

        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(getString(R.string.storage_reference_url));

        user_id = currentUser.getUid();
        messaggereplyCV.setBackgroundResource(R.drawable.chat_et_bg);
        messaggereplyCV.setVisibility(View.GONE);

        getSearchedUserDetails();
        fetchMessages();
        showTypingIndicator();

        chatbackIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        chatsendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = etChat.getText().toString();
                if (message.isEmpty() && mArrayUri.size() == 0) {
                    Toast.makeText(mContext, "Cannot send empty message", Toast.LENGTH_LONG).show();
                } else {
                    etChat.setText("");
                    if (reply_chat_id == null && mArrayUri.size() == 0) {
                        sendMessage(message);
                    } else if (mArrayUri.size() > 0) {
                        sendUploadMessage(message);
                    } else {
                        replyMessage(message);
                    }

                }

            }
        });

        tvChatUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(mContext, OverallProfileActivity.class);
                profileIntent.putExtra("searched_user_id", searched_user_id);
                startActivity(profileIntent);
            }
        });

        chatuserprofileCIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(mContext, OverallProfileActivity.class);
                profileIntent.putExtra("searched_user_id", searched_user_id);
                startActivity(profileIntent);
            }
        });

        tvChatUserIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(mContext, OverallProfileActivity.class);
                profileIntent.putExtra("searched_user_id", searched_user_id);
                startActivity(profileIntent);
            }
        });

        etChat.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (view.hasFocus()) {
                    etChat.setBackgroundResource(R.drawable.chat_et_focus_bg);
                } else {
                    etChat.setBackgroundResource(R.drawable.chat_et_bg);
                }
            }
        });

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiver, new IntentFilter("reply_message"));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiver, new IntentFilter("reply_message_position"));

        replymessagecloseIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reply_chat_id = null;
                messaggereplyCV.setVisibility(View.GONE);
            }
        });

        cvChatAddMedia.setVisibility(View.GONE);
        ibChatAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (cvChatAddMedia.getVisibility() == View.VISIBLE) {
//                    cvChatAddMedia.setVisibility(View.GONE);
//                } else {
//                    cvChatAddMedia.setVisibility(View.VISIBLE);
//                }
                Dexter.withActivity(ChatActivity.this)
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        })
                        .check();

            }
        });

        initChatAddMedia();
    }

    private void sendUploadMessage(String message) {
        Toast.makeText(getApplicationContext(), "Uploading Images", Toast.LENGTH_LONG).show();
        pbMultipleMediaUpload.setVisibility(View.VISIBLE);
        if (mArrayUri.size() > 0) {
            rvChatUploadImages.setVisibility(View.GONE);
        }
        for (int i=0; i<mArrayUri.size(); i++) {
            try {
                uploadImage(mArrayUri.get(i), i, message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (progressImageUpload.equals(true)) {
            if (!message.isEmpty()) {
                sendMessage(message);
            }
        }
    }

    private void uploadImage(Uri postImageUri, int position, String message) throws IOException {
        if (postImageUri == null) {
            Toast.makeText(getApplicationContext(), "Select an image", Toast.LENGTH_LONG).show();
        } else {
//            ProgressDialog progressDialog = new ProgressDialog(this);
//            progressDialog.setMessage("Uploading images...");
//            progressDialog.show();
            File mFileGroupProfileImage = FileUtil.from(ChatActivity.this, postImageUri);
            String chat_id = mDatabase.child("chats").push().getKey();
            final Long ts_long = System.currentTimeMillis() / 1000;
            final String ts = ts_long.toString();
            //final StorageReference childRef = storageReference.child("users/profiles/profile_images/" + currentUser.getUid() + ".jpg");
            //final StorageReference thumb_childRef = storageReference.child("users/profile_images/profile_images/" + currentUser.getUid() + ".jpg");

//            mCompressedStoryImage = new Compressor(ChatActivity.this).setQuality(8).compressToBitmap(mFileGroupProfileImage);


            ByteArrayOutputStream mProfileBAOS = new ByteArrayOutputStream();
            mCompressedStoryImage.compress(Bitmap.CompressFormat.JPEG, 25, mProfileBAOS);
            byte[] mProfileThumbData = mProfileBAOS.toByteArray();

            final StorageReference mThumbChildRefProfile = storageReference.child("chats/images/" + chat_id + "/" + ts + ".jpg");

            final UploadTask profile_thumb_uploadTask = mThumbChildRefProfile.putBytes(mProfileThumbData);

            profile_thumb_uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        Task<Uri> thumb_uriTask = profile_thumb_uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return mThumbChildRefProfile.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri profile_thumb_downloadUri = task.getResult();
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                                        String formattedDate = sdf.format(new Date());
                                        HashMap<String, Object> mChatDataMap = new HashMap<>();
//                                        mChatDataMap.put("message", message.trim());
                                        mChatDataMap.put("sender_user_id", user_id);
                                        mChatDataMap.put("receiver_user_id", searched_user_id);
                                        mChatDataMap.put("timestamp", System.currentTimeMillis());
                                        mChatDataMap.put("has_seen", isOnline);
                                        mChatDataMap.put("formatted_date", formattedDate);
                                        mChatDataMap.put("chat_id", chat_id);
                                        mChatDataMap.put("media_type", "image");
                                        mChatDataMap.put("media", downloadUri.toString());
                                        mDatabase.child("chats").child(user_id).child(searched_user_id).child(chat_id).setValue(mChatDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    if (isOnline == false) {
                                                        mDatabase.child("notifications").child(searched_user_id).child(user_id).child(chat_id).setValue(mChatDataMap);
                                                    }
                                                    mDatabase.child("chats").child(searched_user_id).child(user_id).child(chat_id).setValue(mChatDataMap);
//                                                    mDatabase.child("last_message").child(user_id).child(searched_user_id).child(chat_id).setValue(mChatDataMap);
//                                                    mDatabase.child("last_message").child(searched_user_id).child(user_id).child(chat_id).setValue(mChatDataMap);
                                                    progressImageUpload.add(true);
                                                    if (position == mArrayUri.size()-1) {
                                                        mArrayUri.clear();
                                                        pbMultipleMediaUpload.setVisibility(View.GONE);
                                                        if (!message.isEmpty()) {
                                                            sendMessage(message);
                                                        }
                                                    }
//                                                    progressDialog.dismiss();
                                                } else {
                                                    Toast.makeText(mContext, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                    if (position == mArrayUri.size()-1) {
                                                        pbMultipleMediaUpload.setVisibility(View.GONE);
                                                    }
//                                                    progressDialog.dismiss();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(mContext, "Failure: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                progressImageUpload.add(false);
                                                if (position == mArrayUri.size()-1) {
                                                    pbMultipleMediaUpload.setVisibility(View.GONE);
                                                }
                                            }
                                        });
                                    } else {
                                        // Handle failures
                                        // ...
//                                        progressDialog.dismiss();
                                        String errMsg = task.getException().getMessage();
                                        Toast.makeText(getApplicationContext(), "Download Uri Error: " + errMsg, Toast.LENGTH_LONG).show();
                                        if (position == mArrayUri.size()-1) {
                                            pbMultipleMediaUpload.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                        });
                    }

                }
            });
        }
    }

    private void initChatAddMedia() {
        ChatAddMedia chatAddMedia = new ChatAddMedia("Add Image", R.drawable.add_icon);
        chatAddMediaList.add(chatAddMedia);
    }


    private void showTypingIndicator() {
        etChat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!isTyping) {
                    mDatabase.child("typing_indicator").child(user_id).child("isTyping").setValue(true);
                    // Send notification for start typing event
                    isTyping = true;
                }
                timer.cancel();
                timer = new Timer();
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                isTyping = false;
                                mDatabase.child("typing_indicator").child(user_id).child("isTyping").setValue(false);
                                //send notification for stopped typing event
                            }
                        },
                        DELAY
                );

            }
        });

        mDatabase.child("typing_indicator").child(searched_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean isTyping  = (Boolean) dataSnapshot.child("isTyping").getValue();

                    if (isTyping == true) {
                        //tvTypingIndicator.setVisibility(View.VISIBLE);
                        tvChatUserIndicator1.setText("typing...");
                        tvChatUserIndicator.setVisibility(View.GONE);
                    } else if (isTyping == false) {
                        //tvTypingIndicator.setVisibility(View.GONE);
                        checkSearchedUserIdOnlineOrNot();
                    }
                } else {
                    checkSearchedUserIdOnlineOrNot();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void replyMessage(String message) {
        if (reply_chat_id!= null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            String formattedDate = sdf.format(new Date());
            String chat_id = mDatabase.child("chats").push().getKey();
            HashMap<String, Object> mReplyMessageDataMap = new HashMap<>();
            mReplyMessageDataMap.put("message", message.trim());
            mReplyMessageDataMap.put("sender_user_id", user_id);
            mReplyMessageDataMap.put("receiver_user_id", searched_user_id);
            mReplyMessageDataMap.put("timestamp", System.currentTimeMillis());
            mReplyMessageDataMap.put("has_seen", isOnline);
            mReplyMessageDataMap.put("formatted_date", formattedDate);
            mReplyMessageDataMap.put("chat_id", chat_id);
            mReplyMessageDataMap.put("reply_chat_id", reply_chat_id);
            mDatabase.child("chats").child(user_id).child(searched_user_id).child(chat_id).setValue(mReplyMessageDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mDatabase.child("chats").child(searched_user_id).child(user_id).child(chat_id).setValue(mReplyMessageDataMap);
//                        mDatabase.child("last_message").child(user_id).child(searched_user_id).child(chat_id).setValue(mReplyMessageDataMap);
//                        mDatabase.child("last_message").child(searched_user_id).child(user_id).child(chat_id).setValue(mReplyMessageDataMap);
                        messaggereplyCV.setVisibility(View.GONE);
                        reply_chat_id = null;
                    } else {
                        Toast.makeText(mContext, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(mContext, "Failure: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(mContext, "Error sending message", Toast.LENGTH_LONG).show();
        }
    }

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            reply_chat_id = intent.getStringExtra("reply_chat_id");
            if (intent.getStringExtra("reply_chat_id_position") != null) {
                reply_chat_position = Integer.parseInt(intent.getStringExtra("reply_chat_id_position"));
                Toast.makeText(mContext, String.valueOf(reply_chat_position), Toast.LENGTH_LONG).show();
                chatRV.smoothScrollToPosition(reply_chat_position);
            }

            if (reply_chat_id != null) {
                mDatabase.child("chats").child(user_id).child(searched_user_id).child(reply_chat_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        reply_message = dataSnapshot.child("message").getValue().toString();
                        reply_sender_user_id  = dataSnapshot.child("sender_user_id").getValue().toString();

                        if (reply_sender_user_id.equals(user_id)) {
                            tvReplyUsername.setText("You");
                        } else {
                            mDatabase.child("users").child(reply_sender_user_id).child("user_data").child("name").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String reply_name = dataSnapshot.getValue().toString();
                                    tvReplyUsername.setText(reply_name);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        tvReplyMessage.setText(reply_message);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                messaggereplyCV.setVisibility(View.VISIBLE);

            }
        }
    };

    private void fetchMessages() {
        mDatabase.child("chats").child(user_id).child(searched_user_id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String keys = dataSnapshot.getKey();
                mKeys.add(keys);
                Chat chat = dataSnapshot.getValue(Chat.class);
                chatList.add(chat);
                chatAdapter.notifyDataSetChanged();
                chatRV.smoothScrollToPosition(chatAdapter.getItemCount()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Chat chat = dataSnapshot.getValue(Chat.class);
//                String key = dataSnapshot.getKey();
//                int index = mKeys.indexOf(key);
//                chatList.set(index, chat);
//                chatAdapter.notifyDataSetChanged();

                String key = dataSnapshot.getKey();

                for (int i = 0; i < chatList.size(); i++) {
                    // Find the item to remove and then remove it by index
                    if (chatList.get(i).getChat_id().equals(key)) {
                        chatList.set(i, chat);
                        break;
                    }
                }

                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String chat_key = dataSnapshot.getKey();
                for (Chat c : chatList) {
                    if (c.getChat_id().equals(chat_key)) {
                        chatList.remove(c);
                        chatAdapter.notifyDataSetChanged();
                        break;
                    }
                }


            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getSearchedUserDetails() {
        mDatabase.child("users").child(searched_user_id).child("user_data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    searched_profile_image = dataSnapshot.child("profile_image").getValue().toString();
                    searched_user_name = dataSnapshot.child("name").getValue().toString();
                    Glide.with(mContext).load(searched_profile_image).thumbnail(0.1f).into(chatuserprofileCIV);
                    tvChatUserName.setText(searched_user_name);
                } else {
                    Glide.with(mContext).load(R.drawable.default_profile_pic).into(chatuserprofileCIV);
                    tvChatUserName.setText("Unknown User");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkSearchedUserIdOnlineOrNot() {
        mDatabase.child("active").child(searched_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    isOnline = (boolean) dataSnapshot.child("isOnline").getValue();
                    long timestamp = (long) dataSnapshot.child("timestamp").getValue();
                    String formatted_date = dataSnapshot.child("formatted_date").getValue().toString();

                    if (isOnline == false) {
                        isOnline = false;
                        if (Math.abs(timestamp - System.currentTimeMillis()) >= 60000) {
                            tvChatUserIndicator1.setText("Last active:");
                            /*
                            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                            Date date = null;
                            try {
                                date = inputFormat.parse(formatted_date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            String niceDateStr = (String) DateUtils.getRelativeTimeSpanString(date.getTime() , Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS);
                            */
                            tvChatUserIndicator.setReferenceTime(timestamp);
                        } else {
                            tvChatUserIndicator1.setText("offline");
                            tvChatUserIndicator.setVisibility(View.GONE);
                        }
                    } else {
                        isOnline = true;
                        tvChatUserIndicator1.setText("Active now");
                        tvChatUserIndicator.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        String formattedDate = sdf.format(new Date());
        String chat_id = mDatabase.child("chats").push().getKey();
        HashMap<String, Object> mChatDataMap = new HashMap<>();
        mChatDataMap.put("message", message.trim());
        mChatDataMap.put("sender_user_id", user_id);
        mChatDataMap.put("receiver_user_id", searched_user_id);
        mChatDataMap.put("timestamp", System.currentTimeMillis());
        mChatDataMap.put("has_seen", isOnline);
        mChatDataMap.put("formatted_date", formattedDate);
        mChatDataMap.put("chat_id", chat_id);
        mDatabase.child("chats").child(user_id).child(searched_user_id).child(chat_id).setValue(mChatDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (isOnline == false) {
                        mDatabase.child("notifications").child(searched_user_id).child(user_id).child(chat_id).setValue(mChatDataMap);
                    }
                    mDatabase.child("chats").child(searched_user_id).child(user_id).child(chat_id).setValue(mChatDataMap);
//                    mDatabase.child("last_message").child(user_id).child(searched_user_id).child(chat_id).setValue(mChatDataMap);
//                    mDatabase.child("last_message").child(searched_user_id).child(user_id).child(chat_id).setValue(mChatDataMap);
                } else {
                    Toast.makeText(mContext, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(mContext, "Failure: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            // When an Image is picked
            if (requestCode == PICK_IMAGE && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                imagesEncodedList = new ArrayList<String>();
                if(data.getData()!=null){

                    Uri mImageUri=data.getData();

                    // Get the cursor
                    Cursor cursor = getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageEncoded  = cursor.getString(columnIndex);
                    cursor.close();
                    mArrayUri.add(mImageUri);
                    rvChatUploadImages.setVisibility(View.VISIBLE);
                    chatMultipleImagesUploadAdapter.setChatMultipleImagesUploadList(mArrayUri);
                    Log.v(TAG, "Selected Images" + mArrayUri.size());
                    Log.e(TAG, "onActivityResult: Multiple Selected Images:\n" + new Gson().toJson(imagesEncodedList));
                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
//                        ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        ChatMultipleImagesUpload chatMultipleImagesUpload = new ChatMultipleImagesUpload();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            mArrayUri.add(uri);
                            // Get the cursor
                            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                            // Move to first row
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageEncoded  = cursor.getString(columnIndex);
                            imagesEncodedList.add(imageEncoded);
                            cursor.close();

                        }
                        rvChatUploadImages.setVisibility(View.VISIBLE);
                        chatMultipleImagesUploadAdapter.setChatMultipleImagesUploadList(mArrayUri);
                        Log.v(TAG, "Selected Images" + mArrayUri.size());
                        Log.e(TAG, "onActivityResult: Multiple Selected Images:\n" + new Gson().toJson(imagesEncodedList));
                    }
                }
            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "onActivityResult: " + e.getLocalizedMessage());
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    protected void onStop() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        String formattedDate = sdf.format(new Date());
        HashMap<String, Object> mActiveDataMap = new HashMap<>();
        mActiveDataMap.put("user_id", user_id);
        mActiveDataMap.put("timestamp", System.currentTimeMillis());
        mActiveDataMap.put("isOnline", false);
        mActiveDataMap.put("formatted_date", formattedDate);
        mDatabase.child("active").child(user_id).setValue(mActiveDataMap);
        super.onStop();
    }

    @Override
    protected void onResume() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        String formattedDate = sdf.format(new Date());
        HashMap<String, Object> mActiveDataMap = new HashMap<>();
        mActiveDataMap.put("user_id", user_id);
        mActiveDataMap.put("timestamp", System.currentTimeMillis());
        mActiveDataMap.put("isOnline", true);
        mActiveDataMap.put("formatted_date", formattedDate);
        mDatabase.child("active").child(user_id).setValue(mActiveDataMap);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        String formattedDate = sdf.format(new Date());
        HashMap<String, Object> mActiveDataMap = new HashMap<>();
        mActiveDataMap.put("user_id", user_id);
        mActiveDataMap.put("timestamp", System.currentTimeMillis());
        mActiveDataMap.put("isOnline", false);
        mActiveDataMap.put("formatted_date", formattedDate);
        mDatabase.child("active").child(user_id).setValue(mActiveDataMap);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        String formattedDate = sdf.format(new Date());
        HashMap<String, Object> mActiveDataMap = new HashMap<>();
        mActiveDataMap.put("user_id", user_id);
        mActiveDataMap.put("timestamp", System.currentTimeMillis());
        mActiveDataMap.put("isOnline", true);
        mActiveDataMap.put("formatted_date", formattedDate);
        mDatabase.child("active").child(user_id).setValue(mActiveDataMap);
        super.onStart();
    }

    @Override
    public void onItemClick(Uri uri, int position, List<Uri> chatMultipleImagesUploadList) {
        mArrayUri.remove(position);
        chatMultipleImagesUploadAdapter.setChatMultipleImagesUploadList(mArrayUri);
        if (chatMultipleImagesUploadList.size() == 0) {
            rvChatUploadImages.setVisibility(View.GONE);
        }
    }
}
