const test = require('node:test');
const assert = require('node:assert');
const { initializeTestEnvironment, assertSucceeds, assertFails } = require('@firebase/rules-unit-testing');
const { readFileSync } = require('node:fs');

const PROJECT_ID = 'global-calling-471221-g2';
const DATABASE_ID = 'ai-studio-android-remixdee-38f17e5e-4e71-4b94-b04e-281b56f92fae';

test('Firestore Security Rules', async (t) => {
  const rules = readFileSync('firestore.rules', 'utf8');
  const testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: '127.0.0.1',
      port: 8085,
    },
  });

  const aliceId = 'alice_123';
  const bobId = 'bob_456';
  const aliceContext = testEnv.authenticatedContext(aliceId);
  const bobContext = testEnv.authenticatedContext(bobId);
  const unauthContext = testEnv.unauthenticatedContext();

  const aliceDb = aliceContext.firestore(DATABASE_ID);
  const bobDb = bobContext.firestore(DATABASE_ID);
  const unauthDb = unauthContext.firestore(DATABASE_ID);

  await t.test('User Profile Isolation', async () => {
    const aliceProfile = aliceDb.doc(`users/${aliceId}`);
    
    // Alice can create her own profile
    await assertSucceeds(aliceProfile.set({
      userId: aliceId,
      email: 'alice@example.com',
      createdAt: new Date(),
      updatedAt: new Date()
    }));

    // Alice cannot read Bob's profile
    await assertFails(aliceDb.doc(`users/${bobId}`).get());
    
    // Unauth cannot read Alice's profile
    await assertFails(unauthDb.doc(`users/${aliceId}`).get());
  });

  await t.test('Conversation Ownership', async () => {
    const convId = 'conv_789';
    const aliceConv = aliceDb.doc(`users/${aliceId}/conversations/${convId}`);

    // Alice can create her conversation
    await assertSucceeds(aliceConv.set({
      id: convId,
      userId: aliceId,
      title: 'Hello Deep Think',
      createdAt: new Date(),
      updatedAt: new Date()
    }));

    // Bob cannot read Alice's conversation
    await assertFails(bobDb.doc(`users/${aliceId}/conversations/${convId}`).get());
    
    // Alice cannot create a conversation for Bob
    await assertFails(aliceDb.doc(`users/${bobId}/conversations/${convId}`).set({
      id: convId,
      userId: bobId,
      title: 'Spoofed',
      createdAt: new Date()
    }));
  });

  await t.test('Message Immutability and Ownership', async () => {
    const convId = 'conv_789';
    const msgId = 'msg_001';
    const aliceMsg = aliceDb.doc(`users/${aliceId}/conversations/${convId}/messages/${msgId}`);

    // Alice can add message to her conversation
    await assertSucceeds(aliceMsg.set({
      id: msgId,
      conversationId: convId,
      userId: aliceId,
      role: 'user',
      content: 'Who are you?',
      createdAt: new Date()
    }));

    // Alice cannot update the message (immutable)
    await assertFails(aliceMsg.update({ content: 'Modified' }));

    // Bob cannot read Alice's messages
    await assertFails(bobDb.doc(`users/${aliceId}/conversations/${convId}/messages/${msgId}`).get());
  });

  await testEnv.cleanup();
});
