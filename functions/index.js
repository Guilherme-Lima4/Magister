const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.enviarNotificacao = functions.firestore
    .document("Mensagens/{conversationId}")
    .onCreate(async (snapshot, context) => {
      const messageData = snapshot.data();

      const toId = messageData.toId;
      const fromId = messageData.fromId;
      const text = messageData.text;

      // Recuperar o token de registro FCM do destinatário da mensagem
      const recipientSnapshot = await admin
          .firestore()
          .collection("Usuarios")
          .doc(toId)
          .get();
      const recipientData = recipientSnapshot.data();
      const recipientToken = recipientData.fcmToken;

      // Construir a mensagem de notificação
      const payload = {
        notification: {
          title: "Nova mensagem",
          body: `Você recebeu uma nova mensagem de ${fromId}: ${text}`,
          clickAction: "FLUTTER_NOTIFICATION_CLICK",
        },
      };

      // Enviar a notificação para o destinatário usando o token FCM
      await admin.messaging().sendToDevice(recipientToken, payload);

      console.log("Notificação enviada com sucesso!");
    });
