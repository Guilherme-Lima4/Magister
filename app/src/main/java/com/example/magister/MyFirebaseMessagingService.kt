package com.example.magister

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

        // Salvar o token no Firestore para o usuário atual
        // ...

        // Ou enviar o token para o servidor do seu aplicativo para gerenciar as notificações
        // ...
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Verifica se a mensagem contém dados
        if (remoteMessage.data.isNotEmpty()) {
            // Aqui você pode tratar os dados da mensagem
            // ...
        }

        // Verifica se a mensagem contém uma notificação
        remoteMessage.notification?.let { notification ->
            // Aqui você pode tratar a notificação recebida
            // ...

            // Exibe uma notificação
            showNotification(notification.title, notification.body)
        }
    }

    private fun showNotification(title: String?, message: String?) {
        val channelId = "default_channel_id"
        val channelName = "Default Channel"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Cria o canal de notificação para dispositivos com versão do Android >= Oreo (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Default Notification Channel"
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
        }

        // Cria o builder da notificação
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo) // ícone da notificação
            .setContentTitle(title) // título da notificação
            .setContentText(message) // conteúdo da notificação
            .setAutoCancel(true) // remove a notificação ao ser clicada
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // som da notificação
            .setPriority(NotificationCompat.PRIORITY_HIGH) // prioridade da notificação

        // Exibe a notificação
        notificationManager.notify(0, notificationBuilder.build())

        // Adicione um log para verificar se a notificação foi enviada
        Log.d(TAG, "Notificação enviada: $title - $message")
    }

    companion object {
        private const val TAG = "MyFirebaseMessagingService"
    }
}