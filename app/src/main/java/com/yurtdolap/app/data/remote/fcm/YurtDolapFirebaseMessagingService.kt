package com.yurtdolap.app.data.remote.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.yurtdolap.app.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class YurtDolapFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New Token: $token")

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w("FCM", "Token alindi ama aktif kullanici yok, Firestore'a yazilmadi.")
            return
        }

        firestore.collection("users")
            .document(userId)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FCM", "Kullanici FCM token'i Firestore'a kaydedildi.")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "FCM token Firestore'a yazilamadi.", e)
            }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Message received from: ${message.from}")

        if (message.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${message.data}")
            // Optional: handle data payloads, e.g., navigating to specific screens
        }

        message.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            sendLocalNotification(it.title ?: "YurtDolap", it.body ?: "")
        }
    }

    private fun sendLocalNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = "yurtdolap_fcm_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Fallback icon
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "YurtDolap Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
