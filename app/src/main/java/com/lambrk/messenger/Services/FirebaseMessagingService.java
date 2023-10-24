package com.lambrk.messenger.Services;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.lambrk.messenger.MainActivity;
import com.lambrk.messenger.Notifications.NotificationsActivity;
import com.lambrk.messenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "FirebaseMessagingService";
    String GROUP_KEY_WORK_MESSAGE = "com.android.example.WORK_MESSAGE";

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;


    private Bitmap icon_bitmap;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        /*
        String notification_type = remoteMessage.getData().get("notification_type");
        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        String icon = remoteMessage.getNotification().getIcon();
        String receiver_user_id = remoteMessage.getData().get("receiver_user_id"); */


        //createNotificationChannel();

        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(getString(R.string.storage_reference_url));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            showNotification(remoteMessage.getData());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void showNotification(Map<String, String> data) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mCurrentuser = mAuth.getCurrentUser();

        createNotificationChannel();

        String title = data.get("title");
        String body = data.get("body");
        String icon = data.get("icon");
        String type = data.get("type");
        String to_user_id = data.get("to_user_id");
        String from_user_id = data.get("from_user_id");


        if (mCurrentuser != null) {
            icon_bitmap = getBitmapfromUrl(icon);
            if (!to_user_id.equals(mCurrentuser.getUid())) {
                if (type.equals("message_notification")) {


                    // Create an Intent for the activity you want to start
                    Intent resultIntent = new Intent(this, MainActivity.class);
                    resultIntent.putExtra("messageNotification", "messageNotification");
                    // Create the TaskStackBuilder and add the intent, which inflates the back stack
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addNextIntentWithParentStack(resultIntent);
                    // Get the PendingIntent containing the entire back stack
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                            .setSmallIcon(R.drawable.notification_burnab_logo)
                            .setContentTitle(title)
                            .setContentText(body)
                            .setAutoCancel(true)
                            .setLargeIcon(getCircleBitmap(icon_bitmap))
                            .setPriority(NotificationCompat.PRIORITY_HIGH);
                    builder.setContentIntent(resultPendingIntent);
                    int notificationId = 1;
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(1, builder.build());

            /*
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                    .setSmallIcon(R.drawable.burnab_notification)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setLargeIcon(getCircleBitmap(icon_bitmap))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            int notificationId = 1;
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, builder.build()); */
                } else if (type.equals("follow_notification")) {
                    // Create an Intent for the activity you want to start
                    Intent resultIntent = new Intent(this, NotificationsActivity.class);
                    resultIntent.putExtra("followNotification", "followNotification");
                    // Create the TaskStackBuilder and add the intent, which inflates the back stack
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addNextIntentWithParentStack(resultIntent);
                    // Get the PendingIntent containing the entire back stack
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                            .setSmallIcon(R.drawable.notification_burnab_logo)
                            .setContentTitle("@" + title + " has followed you")
                            .setLargeIcon(getCircleBitmap(icon_bitmap))
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    builder.setContentIntent(resultPendingIntent);
                    int notificationId = 1;
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(1, builder.build());
                } else if (type.equals("post_notification")) {
                    String post_notification_type = data.get("post_notification_type");
                    if (post_notification_type.equals("notification_post_liked")) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                                .setSmallIcon(R.drawable.notification_burnab_logo)
                                .setContentTitle("@" + title + " has liked your post")
                                .setLargeIcon(getCircleBitmap(icon_bitmap))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        int notificationId = 1;
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(1, builder.build());
                    } else if (post_notification_type.equals("notification_post_comment")) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                                .setSmallIcon(R.drawable.notification_burnab_logo)
                                .setContentTitle("@" + title + " has commented on your post")
                                .setLargeIcon(getCircleBitmap(icon_bitmap))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        int notificationId = 1;
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(1, builder.build());
                    }
                } else if (type.equals("story_notification")) {
                    // Create an Intent for the activity you want to start
                    Intent resultIntent = new Intent(this, NotificationsActivity.class);
                    resultIntent.putExtra("storyLikeNotification", "storyLikeNotification");
                    // Create the TaskStackBuilder and add the intent, which inflates the back stack
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addNextIntentWithParentStack(resultIntent);
                    // Get the PendingIntent containing the entire back stack
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                            .setSmallIcon(R.drawable.notification_burnab_logo)
                            .setContentTitle("@" + title + " " + body)
                            .setLargeIcon(getCircleBitmap(icon_bitmap))
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    builder.setContentIntent(resultPendingIntent);
                    int notificationId = 1;
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(1, builder.build());
                } else if (type.equals("story_comment_notification")) {
                    // Create an Intent for the activity you want to start
                    Intent resultIntent = new Intent(this, NotificationsActivity.class);
                    resultIntent.putExtra("storyLikeNotification", "storyLikeNotification");
                    // Create the TaskStackBuilder and add the intent, which inflates the back stack
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addNextIntentWithParentStack(resultIntent);
                    // Get the PendingIntent containing the entire back stack
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                            .setSmallIcon(R.drawable.notification_burnab_logo)
                            .setContentTitle("@" + title + " has commented on your story ")
                            .setContentText(body)
                            .setLargeIcon(getCircleBitmap(icon_bitmap))
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    builder.setContentIntent(resultPendingIntent);
                    int notificationId = 1;
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(1, builder.build());
                }
            }

        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.default_notification_channel_id);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setShowBadge(true);
            channel.setVibrationPattern(new long[]{0, 100});
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output;
        Rect srcRect, dstRect;
        float r;
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        if (width > height){
            output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);
            int left = (width - height) / 2;
            int right = left + height;
            srcRect = new Rect(left, 0, right, height);
            dstRect = new Rect(0, 0, height, height);
            r = height / 2;
        }else{
            output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            int top = (height - width)/2;
            int bottom = top + width;
            srcRect = new Rect(0, top, width, bottom);
            dstRect = new Rect(0, 0, width, width);
            r = width / 2;
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint);

        bitmap.recycle();

        return output;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d(TAG, s);
    }
}
