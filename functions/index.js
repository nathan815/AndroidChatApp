const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const firestore = admin.firestore();

// Whenever a message is sent, update the conversation's updated timestamp and latestMessage value
exports.updateConversationInfo = functions.firestore
    .document('conversations/{convId}/messages/{msgId}')
    .onCreate((snap, context) => {
        const conversationsRef = firestore.collection('conversations');
        const message = snap.data();
        conversationsRef.doc(context.params.convId).set({
            latestMessage: message.text,
            updatedOn: admin.firestore.FieldValue.serverTimestamp()
        }, { merge: true });
    });

// Whenever a user registers, add a users document for them
exports.createUser = functions.auth.user().onCreate((user) => {
    const usersRef = firestore.collection('users');
    usersRef.doc(user.uid).set({
        userId: user.uid,
        email: user.email,
        joinedOn: admin.firestore.FieldValue.serverTimestamp()
    }, { merge: true }).then(documentReference => {
        console.log('Added user node');
    });
});
