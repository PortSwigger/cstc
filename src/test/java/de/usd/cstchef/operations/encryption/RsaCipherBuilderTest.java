package de.usd.cstchef.operations.encryption;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;

import org.junit.Before;
import org.junit.Test;

public class RsaCipherBuilderTest {

    private KeyPair keyPair;

    @Before
    public void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        this.keyPair = generator.generateKeyPair();
    }

    private byte[] encrypt(byte[] plain, String padding, String oaepHash, String mgf1Hash) throws Exception {
        Cipher cipher = RsaCipherBuilder.build(Cipher.ENCRYPT_MODE, keyPair.getPublic(), padding, oaepHash, mgf1Hash);
        return cipher.doFinal(plain);
    }

    private byte[] decrypt(byte[] enc, String padding, String oaepHash, String mgf1Hash) throws Exception {
        Cipher cipher = RsaCipherBuilder.build(Cipher.DECRYPT_MODE, keyPair.getPrivate(), padding, oaepHash, mgf1Hash);
        return cipher.doFinal(enc);
    }

    // Reproduces the JS forge configuration: RSA-OAEP with SHA-256 for both the
    // OAEP digest and the MGF1 digest.
    @Test
    public void oaepSha256RoundTrip() throws Exception {
        byte[] plain = "Sup3rS3cr3tP@ssw0rd".getBytes("UTF-8");

        byte[] enc = encrypt(plain, RsaCipherBuilder.PADDING_OAEP, "SHA-256", "SHA-256");
        byte[] dec = decrypt(enc, RsaCipherBuilder.PADDING_OAEP, "SHA-256", "SHA-256");

        assertArrayEquals(plain, dec);
    }

    // OAEP is randomised, so two encryptions of the same plaintext must differ.
    @Test
    public void oaepIsRandomised() throws Exception {
        byte[] plain = "hello".getBytes("UTF-8");

        byte[] enc1 = encrypt(plain, RsaCipherBuilder.PADDING_OAEP, "SHA-256", "SHA-256");
        byte[] enc2 = encrypt(plain, RsaCipherBuilder.PADDING_OAEP, "SHA-256", "SHA-256");

        assertFalse(Arrays.equals(enc1, enc2));
    }

    // Independently selectable digests: SHA-1 OAEP digest with SHA-256 MGF1.
    @Test
    public void oaepMixedDigestsRoundTrip() throws Exception {
        byte[] plain = "mixed-digests".getBytes("UTF-8");

        byte[] enc = encrypt(plain, RsaCipherBuilder.PADDING_OAEP, "SHA-1", "SHA-256");
        byte[] dec = decrypt(enc, RsaCipherBuilder.PADDING_OAEP, "SHA-1", "SHA-256");

        assertArrayEquals(plain, dec);
    }

    // Backwards compatible default path still works.
    @Test
    public void pkcs1RoundTrip() throws Exception {
        byte[] plain = "legacy".getBytes("UTF-8");

        byte[] enc = encrypt(plain, RsaCipherBuilder.PADDING_PKCS1, null, null);
        byte[] dec = decrypt(enc, RsaCipherBuilder.PADDING_PKCS1, null, null);

        assertArrayEquals(plain, dec);
    }

    // A reduced provider list (no OAEP) must still yield an OAEP option.
    @Test
    public void keyStorePaddingsAddsOaepWhenMissing() {
        List<String> paddings = Arrays.asList(
                RsaCipherBuilder.keyStorePaddings(new String[] { "PKCS1PADDING" }));

        assertTrue(paddings.contains("PKCS1PADDING"));
        assertTrue(paddings.contains(RsaCipherBuilder.PADDING_OAEP));
    }

    // A provider that already advertises OAEP variants is left untouched (no duplicate generic entry).
    @Test
    public void keyStorePaddingsKeepsProviderOaep() {
        List<String> paddings = Arrays.asList(RsaCipherBuilder.keyStorePaddings(
                new String[] { "NOPADDING", "PKCS1PADDING", "OAEPWITHSHA-256ANDMGF1PADDING" }));

        assertTrue(paddings.contains("OAEPWITHSHA-256ANDMGF1PADDING"));
        assertFalse(paddings.contains(RsaCipherBuilder.PADDING_OAEP));
    }

    // An empty provider list still produces usable choices.
    @Test
    public void keyStorePaddingsHandlesEmpty() {
        List<String> paddings = Arrays.asList(RsaCipherBuilder.keyStorePaddings(new String[0]));

        assertTrue(paddings.contains(RsaCipherBuilder.PADDING_PKCS1));
        assertTrue(paddings.contains(RsaCipherBuilder.PADDING_OAEP));
    }
}
