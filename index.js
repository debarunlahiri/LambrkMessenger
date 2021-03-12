/* eslint-disable no-trailing-spaces */
/* eslint-disable camelcase */
/* eslint-disable max-len */
const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.followingNotification = functions.database.ref("/notifications/following/{searched_user_id}/{user_id}").onCreate((snap, context) => {
  const searched_user_id = context.params.searched_user_id;
  // eslint-disable-next-line camelcase
  const user_id = context.params.user_id;

  const getFollowing = admin.database().ref(`/notifications/following/${searched_user_id}/${user_id}/user_id`).once("value");
  return getFollowing.then((result) => {
    const user_id = result.val();

    const deviceToken = admin.database().ref(`/users/${searched_user_id}/token_id`).once("value");

    return deviceToken.then((result) => {
      const token_id = result.val();

      const following_username = admin.database().ref(`/users/${user_id}/user_data/username`).once("value");

      return following_username.then((result) => {
        const username = result.val();
        
        const following_profile_image = admin.database().ref(`/users/${user_id}/user_data/thumb_profile_image`).once("value");

        return following_profile_image.then((result) => {
          const image = result.val();

          const payload = {
            "data": {
              "user_id": user_id,
              "searched_user_id": searched_user_id,
              "title": username,
              "body": "MessageChannel",
              "type": "follow_notification",
              "icon": image,
            },
          };

          return admin.messaging().sendToDevice(token_id, payload).then((response) => {
            return console.log(username + " has followed you" + "message" + image + user_id + searched_user_id);
          });
        });
      });
    });
  });
});

exports.messageNotification = functions.database.ref("/notifications/chats/{receiver_user_id}/{sender_user_id}/{chat_id}").onCreate((snap, context) => {
  const receiver_user_id = context.params.receiver_user_id;
  const sender_user_id = context.params.sender_user_id;
  const chat_id = context.params.chat_id;

  const getMessage = admin.database().ref(`/notifications/chats/${receiver_user_id}/${sender_user_id}/${chat_id}/message`).once("value");
  return getMessage.then((result) => {
    const message = result.val();

    const deviceToken = admin.database().ref(`/users/${receiver_user_id}/token_id`).once("value");

    return deviceToken.then((result) => {
      const token_id = result.val();

      const following_username = admin.database().ref(`/users/${sender_user_id}/user_data/username`).once("value");

      return following_username.then((result) => {
        const username = result.val();

        const following_profile_image = admin.database().ref(`/users/${sender_user_id}/user_data/thumb_profile_image`).once("value");

        return following_profile_image.then((result) => {
          const image = result.val();

          const payload = {
            "data": {
              "user_id": sender_user_id,
              "receiver_user_id": receiver_user_id,
              "title": username,
              "body": message,
              "type": "message_notification",
              "icon": image,
            },
          };

          return admin.messaging().sendToDevice(token_id, payload).then((response) => {
            return console.log(username + " sent you a message" + message + image + sender_user_id + receiver_user_id);
          });
        });
      });
    });
  });
});

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });
