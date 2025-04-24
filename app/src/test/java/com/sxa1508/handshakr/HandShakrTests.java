package com.sxa1508.handshakr;

import org.junit.Assert;
import org.junit.Test;
import org.pgpainless.sop.SOPImpl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import sop.ByteArrayAndResult;
import sop.DecryptionResult;
import sop.ReadyWithResult;
import sop.SOP;
import sop.exception.SOPGPException;


public class HandShakrTests {

    @Test
    public void testEncryption(){
        SOP sop = new SOPImpl();
        String message = "Lorem Ipsum";
        byte[] plaintext = message.getBytes(StandardCharsets.UTF_8);

        try {
            byte[] AlicePrivateKey = sop.generateKey()
                    .userId("Alice")
                    .generate()
                    .getBytes();

            byte [] AlicePublicKey = sop.extractCert()
                    .key(AlicePrivateKey).getBytes();

            byte[] BobPrivateKey = sop.generateKey()
                    .userId("Bob")
                    .generate()
                    .getBytes();

            byte [] BobPublicKey = sop.extractCert()
                    .key(BobPrivateKey).getBytes();

            byte[] MalloryPrivateKey = sop.generateKey()
                    .userId("Mallory")
                    .generate()
                    .getBytes();

            byte [] MalloryPublicKey = sop.extractCert()
                    .key(MalloryPrivateKey).getBytes();

            Assert.assertNotEquals("Keys should be unique", AlicePrivateKey,BobPrivateKey);

            byte[] ciphertextbytes = sop.encrypt()
                    .withCert(AlicePublicKey)
                    .withCert(BobPublicKey)
                    .signWith(AlicePrivateKey)
                    .plaintext(plaintext)
                    .toByteArrayAndResult().getBytes();

            String ciphertext = new String(ciphertextbytes, StandardCharsets.UTF_8);

            Assert.assertNotEquals("Cipher and message should be different", ciphertext, message);


            ReadyWithResult<DecryptionResult> BobReadyWithResult = null;
            BobReadyWithResult = sop.decrypt()
                        .withKey(BobPrivateKey)
                        .verifyWithCert(AlicePublicKey)
                        .verifyWithCert(BobPublicKey)
                        .ciphertext(ciphertextbytes);
            ByteArrayAndResult<DecryptionResult> BobbytesAndResult = null;
                BobbytesAndResult = BobReadyWithResult.toByteArrayAndResult();

            byte[] BobresultText = BobbytesAndResult.getBytes();
            String BobdecryptedMessage = new String(BobresultText, StandardCharsets.UTF_8);

            Assert.assertEquals("Message should be decryptable by Bob", BobdecryptedMessage, message);

            try {
                ReadyWithResult<DecryptionResult> MalloryReadyWithResult = null;
                MalloryReadyWithResult = sop.decrypt()
                        .withKey(MalloryPrivateKey)
                        .verifyWithCert(AlicePublicKey)
                        .verifyWithCert(MalloryPublicKey)
                        .ciphertext(ciphertextbytes);
                ByteArrayAndResult<DecryptionResult> MallorybytesAndResult = null;
                MallorybytesAndResult = MalloryReadyWithResult.toByteArrayAndResult();

                byte[] MalloryresultText = MallorybytesAndResult.getBytes();
                String MallorydecryptedMessage = new String(MalloryresultText, StandardCharsets.UTF_8);

                Assert.assertNotEquals("Message should NOT be decryptable by Mallory", MallorydecryptedMessage, message);
            } catch (SOPGPException e){
                Assert.assertTrue("SOP should throw an error with Mallory's key", e instanceof SOPGPException.CannotDecrypt);
            }
        } catch (IOException e) {
            Assert.fail("IOException in test");
        }


    }

}
