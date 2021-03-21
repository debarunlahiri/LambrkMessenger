package com.lahiriproductions.lambrk_messenger.Chat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lahiriproductions.lambrk_messenger.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public static final int MESSAGE_RIGHT = 1;
    public static final int MESSAGE_LEFT = 2;
    public static final int MESSAGE_REPLY_RIGHT = 3;
    public static final int MESSAGE_REPLY_LEFT = 4;
    private static final int MESSAGE_MEDIA_RIGHT = 5;
    private static final int MESSAGE_MEDIA_LEFT = 6;
    private List<Chat> chatList;
    private Context mContext;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;
    private String user_id;
    private String message;
    private boolean isImageUpload = false;


    public ChatAdapter(List<Chat> chatList, Context mContext) {
        this.chatList = chatList;
        this.mContext = mContext;
    }

    public static void setForceShowIcon(PopupMenu popupMenu) {
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper
                            .getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void setChatUpload(boolean isImageUpload) {
        this.isImageUpload = isImageUpload;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MESSAGE_RIGHT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sender_chat_list_item, parent, false);
            return new ViewHolder(view);
        } else if (viewType == MESSAGE_REPLY_RIGHT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sender_chat_reply_list_item, parent, false);
            return new ViewHolder(view);
        } else if (viewType == MESSAGE_REPLY_LEFT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receiver_chat_reply_list_item, parent, false);
            return new ViewHolder(view);
        } else if (viewType == MESSAGE_MEDIA_LEFT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_chat_media_left_layout, parent, false);
            return new ViewHolder(view);
        } else if (viewType == MESSAGE_MEDIA_RIGHT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_chat_media_right_layout, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receiver_chat_list_item, parent, false);
            return new ViewHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        mDatabase = FirebaseDatabase.getInstance(mContext.getString(R.string.firebase_url)).getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(mContext.getString(R.string.storage_reference_url));

//        setMessage(holder, position);
        if (chat.getMedia_type() != null && chat.getMedia() != null) {
            if (chat.getMedia_type().equalsIgnoreCase("image")) {
                Glide.with(mContext).load(chat.getMedia()).into(holder.ivChatMediaImage);
            }

            holder.cvChatMedia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent chatMediaIntent = new Intent(mContext, ChatMediaActivity.class);
                    chatMediaIntent.putExtra("sender_user_id", chat.getSender_user_id());
                    chatMediaIntent.putExtra("receiver_user_id", chat.getReceiver_user_id());
                    chatMediaIntent.putExtra("chat_id", chat.getChat_id());
                    mContext.startActivity(chatMediaIntent);
                }
            });
        } else {
            holder.tvChatMessage.setText(chat.getMessage());
            holder.tvChatMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    initPopupMenu(holder, position);
                }
            });

            holder.tvTime.setVisibility(View.GONE);
            if (chat.getReply_chat_id() == null) {
                holder.tvChatMessage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        initTvTime(holder, position);
                        return true;
                    }
                });
            } else {
                holder.tvChatMessage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        initTvTime(holder, position);
                        return true;
                    }
                });
            }

            if (chat.getReply_chat_id() != null) {
                fetchReplyMessage(holder, position);
            }
        }


    }

    private void initTvTime(ViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        if (holder.tvTime.getVisibility() == View.GONE) {
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvTime.setReferenceTime(chat.getTimestamp());
        } else if (holder.tvTime.getVisibility() == View.VISIBLE) {
            holder.tvTime.setVisibility(View.GONE);
        }
    }

    private void setMessage(ViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        mDatabase.child("chats").child(chat.getSender_user_id()).child(chat.getReceiver_user_id()).child(chat.getChat_id()).child("message").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    message = dataSnapshot.getValue().toString();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initPopupMenu(ViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        PopupMenu popupMenu = new PopupMenu(mContext, holder.tvChatMessage);
        popupMenu.inflate(R.menu.message_menu);
        popupMenu.getMenu().findItem(R.id.message_reply_item).setVisible(true);
        if (!chat.getSender_user_id().equals(user_id)) {
            popupMenu.getMenu().findItem(R.id.message_delete_for_everyone_item).setVisible(false);
            popupMenu.getMenu().findItem(R.id.message_edit_item).setVisible(false);
        }
        setForceShowIcon(popupMenu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.message_reply_item:
                        Intent replyMessageIntent = new Intent("reply_message");
                        replyMessageIntent.putExtra("reply_chat_id", chat.getChat_id());
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(replyMessageIntent);
                        return true;

                    case R.id.message_edit_item:
                        Dialog dialog = new Dialog(mContext);
                        dialog.setContentView(R.layout.chat_edit_dialog_layout);
                        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, WRAP_CONTENT);
                        dialog.show();

                        TextView tvEditChatDialog = dialog.findViewById(R.id.tvEditChatDialog);
                        EditText etEditChatDialogBody = dialog.findViewById(R.id.etEditChatDialogBody);
                        Button editchatdialogcancelbutton = dialog.findViewById(R.id.editchatdialogcancelbutton);
                        Button editchatdialogsavebutton = dialog.findViewById(R.id.editchatdialogsavebutton);

                        mDatabase.child("chats").child(chat.getSender_user_id()).child(chat.getReceiver_user_id()).child(chat.getChat_id()).child("message").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String edited_message = dataSnapshot.getValue().toString();
                                    etEditChatDialogBody.setText(edited_message);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        editchatdialogcancelbutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.cancel();
                            }
                        });
                        editchatdialogsavebutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String edited_message = etEditChatDialogBody.getText().toString();
                                mDatabase.child("chats").child(chat.getSender_user_id()).child(chat.getReceiver_user_id()).child(chat.getChat_id()).child("message").setValue(edited_message).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mDatabase.child("chats").child(chat.getReceiver_user_id()).child(chat.getSender_user_id()).child(chat.getChat_id()).child("message").setValue(edited_message);
                                            dialog.cancel();
                                            Toast.makeText(mContext, "Message edited successfully", Toast.LENGTH_LONG).show();
                                        } else {
                                            dialog.cancel();
                                            Toast.makeText(mContext, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.cancel();
                                        Toast.makeText(mContext, "Failure: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
                        return true;

                    case R.id.message_delete_item:
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("Delete?");
                        builder.setMessage("You won't recover this message by deleting this message.");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (chat.getReceiver_user_id().equals(user_id)) {
                                    mDatabase.child("chats").child(chat.getReceiver_user_id()).child(chat.getSender_user_id()).child(chat.getChat_id()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(mContext, "Message deleted successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(mContext, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                } else {
                                    mDatabase.child("chats").child(chat.getSender_user_id()).child(chat.getReceiver_user_id()).child(chat.getChat_id()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(mContext, "Message deleted successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(mContext, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }

                                //Toast.makeText(mContext, "Feature not implemented yet", Toast.LENGTH_LONG).show();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                        builder.show();
                        return true;

                    case R.id.message_delete_for_everyone_item:
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                        builder1.setTitle("Delete for Everyone?");
                        builder1.setMessage("You won't recover this message by deleting this message.");
                        builder1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mDatabase.child("chats").child(chat.getSender_user_id()).child(chat.getReceiver_user_id()).child(chat.getChat_id()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mDatabase.child("chats").child(chat.getReceiver_user_id()).child(chat.getSender_user_id()).child(chat.getChat_id()).removeValue();
                                            Toast.makeText(mContext, "Message successfully deleted", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(mContext, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(mContext, "Fauilure: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                        builder1.show();
                        return true;

                    case R.id.message_copy_item:
                        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(30);
                        ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText("message_copy", chat.getMessage());
                        clipboardManager.setPrimaryClip(clipData);
                        Toast.makeText(mContext, "Message copied", Toast.LENGTH_SHORT).show();
                        return true;

                    default:
                        return false;
                }

            }
        });
        popupMenu.show();


    }

    private void fetchReplyMessage(ViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.replychatCV.setBackgroundResource(R.drawable.reply_top_bg);
        holder.tvChatMessage.setBackgroundResource(R.drawable.reply_bottom_bg);
        holder.senderreplychatLL.setGravity(Gravity.RIGHT);

        if (chat.getSender_user_id().equals(user_id) && !chat.getReceiver_user_id().equals(user_id)) {
            mDatabase.child("chats").child(chat.getSender_user_id()).child(chat.getReceiver_user_id()).child(chat.getReply_chat_id()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String reply_sender_user_id = dataSnapshot.child("sender_user_id").getValue().toString();
                    String reply_receiver_user_id = dataSnapshot.child("receiver_user_id").getValue().toString();
                    String message = dataSnapshot.child("message").getValue().toString();
                    holder.tvReplyChatMessage.setText(message);
                    if (chat.getSender_user_id().equals(reply_sender_user_id) && chat.getReceiver_user_id().equals(reply_receiver_user_id)) {
                        holder.tvReplyChatName.setText("You");
                    } else if (!chat.getSender_user_id().equals(reply_sender_user_id) && !chat.getReceiver_user_id().equals(reply_receiver_user_id)) {
                        mDatabase.child("users").child(reply_sender_user_id).child("user_data").child("name").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String name = dataSnapshot.getValue().toString();
                                    holder.tvReplyChatName.setText(name);
                                } else {
                                    holder.tvReplyChatName.setText("Unknown User");
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else if (!chat.getSender_user_id().equals(user_id) && chat.getReceiver_user_id().equals(user_id)) {
            mDatabase.child("chats").child(chat.getSender_user_id()).child(chat.getReceiver_user_id()).child(chat.getReply_chat_id()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String reply_sender_user_id = dataSnapshot.child("sender_user_id").getValue().toString();
                    String reply_receiver_user_id = dataSnapshot.child("receiver_user_id").getValue().toString();
                    String message = dataSnapshot.child("message").getValue().toString();
                    holder.tvReplyChatMessage.setText(message);
                    if (chat.getSender_user_id().equals(reply_receiver_user_id) && chat.getReceiver_user_id().equals(reply_sender_user_id)) {
                        holder.tvReplyChatName.setText("You");
                    } else if (!chat.getSender_user_id().equals(reply_receiver_user_id) && !chat.getReceiver_user_id().equals(reply_sender_user_id)) {
                        mDatabase.child("users").child(reply_sender_user_id).child("user_data").child("name").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String name = dataSnapshot.getValue().toString();
                                    holder.tvReplyChatName.setText(name);
                                } else {
                                    holder.tvReplyChatName.setText("Unkown User");
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Chat chat = chatList.get(position);
        mDatabase = FirebaseDatabase.getInstance(mContext.getString(R.string.firebase_url)).getReference();
        ;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(mContext.getString(R.string.storage_reference_url));

        user_id = currentUser.getUid();

        if (chat.getSender_user_id() != null && chat.getReply_chat_id() == null && chat.getSender_user_id().equals(user_id)) {
            if (chat.getMedia_type() != null && chat.getMedia() != null) {
                return MESSAGE_MEDIA_RIGHT;
            } else {
                return MESSAGE_RIGHT;
            }
        } else if (chat.getReply_chat_id() != null && chat.getSender_user_id().equals(user_id) && !chat.getReceiver_user_id().equals(user_id)) {
            if (chat.getMedia_type() != null && chat.getMedia() != null) {
                return MESSAGE_MEDIA_RIGHT;
            } else {
                return MESSAGE_REPLY_RIGHT;
            }
        } else if (chat.getReply_chat_id() != null && !chat.getSender_user_id().equals(user_id) && chat.getReceiver_user_id().equals(user_id)) {
            if (chat.getMedia_type() != null && chat.getMedia() != null) {
                return MESSAGE_MEDIA_LEFT;
            } else {
                return MESSAGE_REPLY_LEFT;
            }
        } else {
            if (chat.getMedia_type() != null && chat.getMedia() != null) {
                return MESSAGE_MEDIA_LEFT;
            } else {
                return MESSAGE_LEFT;
            }

        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvChatMessage;
        private RelativeTimeTextView tvTime;
        private ImageView ivChatMediaImage;

        private CardView replychatCV, cvChatMedia;
        private TextView tvReplyChatName, tvReplyChatMessage;
        private LinearLayout senderreplychatLL;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvChatMessage = itemView.findViewById(R.id.tvChatMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            //chatCV = itemView.findViewById(R.id.chatCV);

            tvReplyChatName = itemView.findViewById(R.id.tvReplyChatName);
            tvReplyChatMessage = itemView.findViewById(R.id.tvReplyChatMessage);
            replychatCV = itemView.findViewById(R.id.replychatCV);
            //senderreplychatCV = itemView.findViewById(R.id.senderreplychatCV);
            senderreplychatLL = itemView.findViewById(R.id.senderreplychatLL);
            ivChatMediaImage = itemView.findViewById(R.id.ivChatMediaImage);
            cvChatMedia = itemView.findViewById(R.id.cvChatMedia);
        }
    }
}
