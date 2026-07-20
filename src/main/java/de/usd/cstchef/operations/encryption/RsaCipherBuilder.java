package de.usd.cstchef.operations.encryption;

import java.security.Key;
import java.util.LinkedHashSet;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import java.security.spec.MGF1ParameterSpec;

/**
 * Central place that knows how to turn a user selected RSA padding (and, for
 * OAEP, the two message digests) into an initialised {@link Cipher}.
 *
 * Keeping this logic in one small class means the UI operations
 * ({@code RsaEncryption} / {@code RsaDecryption}) only have to read their combo
 * boxes and hand the values over. Supporting a new padding or digest in the
 * future is a one line change to the constants below.
 */
public class RsaCipherBuilder {

    /** Selectable padding modes shown in the operations' "Padding" combo box. */
    public static final String PADDING_PKCS1 = "PKCS1Padding";
    public static final String PADDING_OAEP = "OAEPPadding";

    public static final String[] PADDINGS = new String[] {
            PADDING_PKCS1,
            PADDING_OAEP
    };

    /**
     * Message digests offered for the OAEP hash and the MGF1 hash. These are
     * the digests SunJCE accepts inside an {@link OAEPParameterSpec}. Add new
     * entries here to make them available in both RSA operations at once.
     */
    public static final String[] DIGESTS = new String[] {
            "SHA-1",
            "SHA-224",
            "SHA-256",
            "SHA-384",
            "SHA-512"
    };

    public static final String DEFAULT_DIGEST = "SHA-256";

    /**
     * Whether a padding name selects OAEP. Matches both the simple PEM choice
     * ({@link #PADDING_OAEP}) and the provider's named variants used in KeyStore
     * mode (e.g. {@code OAEPWITHSHA-256ANDMGF1PADDING}).
     */
    public static boolean isOaep(String padding) {
        return padding != null && padding.toUpperCase().contains("OAEP");
    }

    /**
     * Build the padding list for KeyStore mode. We keep whatever the security
     * provider advertises (so previously saved recipes still match) but make
     * sure an OAEP option is always present. Some JVMs / Burp's bundled JRE
     * advertise a reduced RSA padding list, which otherwise leaves the user
     * with no way to pick OAEP even though we support it.
     */
    public static String[] keyStorePaddings(String[] providerPaddings) {
        LinkedHashSet<String> paddings = new LinkedHashSet<>();
        if (providerPaddings != null) {
            for (String padding : providerPaddings) {
                if (padding != null && !padding.isEmpty()) {
                    paddings.add(padding);
                }
            }
        }

        // Fall back to PKCS1 if the provider advertised nothing usable.
        if (paddings.isEmpty()) {
            paddings.add(PADDING_PKCS1);
        }

        boolean hasOaep = false;
        for (String padding : paddings) {
            if (isOaep(padding)) {
                hasOaep = true;
                break;
            }
        }
        if (!hasOaep) {
            paddings.add(PADDING_OAEP);
        }

        return paddings.toArray(new String[0]);
    }

    /**
     * Build and initialise an RSA {@link Cipher}.
     *
     * @param mode      {@link Cipher#ENCRYPT_MODE} or {@link Cipher#DECRYPT_MODE}
     * @param key       the public (encrypt) or private (decrypt) key
     * @param padding   one of {@link #PADDINGS}
     * @param oaepHash  OAEP digest, one of {@link #DIGESTS} (ignored for PKCS1)
     * @param mgf1Hash  MGF1 digest, one of {@link #DIGESTS} (ignored for PKCS1)
     */
    public static Cipher build(int mode, Key key, String padding, String oaepHash, String mgf1Hash) throws Exception {
        if (PADDING_OAEP.equals(padding)) {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            OAEPParameterSpec spec = new OAEPParameterSpec(
                    oaepHash,
                    "MGF1",
                    new MGF1ParameterSpec(mgf1Hash),
                    PSource.PSpecified.DEFAULT);
            cipher.init(mode, key, spec);
            return cipher;
        }

        // Default / backwards compatible behaviour: RSA with PKCS#1 v1.5 padding.
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(mode, key);
        return cipher;
    }
}
