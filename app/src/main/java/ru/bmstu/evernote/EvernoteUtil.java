package ru.bmstu.evernote;

import com.evernote.edam.type.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Ivan on 17.12.2014.
 */
public class EvernoteUtil {

    /**
     * The ENML preamble to every Evernote note.
     * Note content goes between <en-note> and </en-note>
     */
    public static final String NOTE_PREFIX =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">" +
                    "<en-note>";

    /**
     * The ENML postamble to every Evernote note
     */
    public static final String NOTE_SUFFIX = "</en-note>";

    /**
     * One-way hashing function used for providing a checksum of EDAM data
     */
    private static final String EDAM_HASH_ALGORITHM = "MD5";

    /**
     * Create an ENML &lt;en-media&gt; tag for the specified Resource object.
     */
    public static String createEnMediaTag(Resource resource) {
        return "<en-media hash=\"" + bytesToHex(resource.getData().getBodyHash()) +
                "\" type=\"" + resource.getMime() + "\"/>";
    }

    /**
     * Returns an MD5 checksum of the provided array of bytes.
     */
    public static byte[] hash(byte[] body) {
        try {
            return MessageDigest.getInstance(EDAM_HASH_ALGORITHM).digest(body);
        } catch (NoSuchAlgorithmException e) {
            throw new EvernoteUtilException(EDAM_HASH_ALGORITHM + " not supported", e);
        }
    }

    /**
     * Returns an MD5 checksum of the contents of the provided InputStream.
     */
    public static byte[] hash(InputStream in) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(EDAM_HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new EvernoteUtilException(EDAM_HASH_ALGORITHM + " not supported", e);
        }
        byte[] buf = new byte[1024];
        int n;
        while ((n = in.read(buf)) != -1) {
            digest.update(buf, 0, n);
        }
        return digest.digest();
    }

    /**
     * Converts the provided byte array into a hexadecimal string
     * with two characters per byte.
     */
    public static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, false);
    }

    /**
     * Takes the provided byte array and converts it into a hexadecimal string
     * with two characters per byte.
     *
     * @param withSpaces if true, include a space character between each hex-rendered
     *                   byte for readability.
     */
    public static String bytesToHex(byte[] bytes, boolean withSpaces) {
        StringBuilder sb = new StringBuilder();
        for (byte hashByte : bytes) {
            int intVal = 0xff & hashByte;
            if (intVal < 0x10) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(intVal));
            if (withSpaces) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    /**
     * Takes a string in hexadecimal format and converts it to a binary byte
     * array. This does no checking of the format of the input, so this should
     * only be used after confirming the format or origin of the string. The input
     * string should only contain the hex data, two characters per byte.
     */
    public static byte[] hexToBytes(String hexString) {
        byte[] result = new byte[hexString.length() / 2];
        for (int i = 0; i < result.length; ++i) {
            int offset = i * 2;
            result[i] = (byte) Integer.parseInt(hexString.substring(offset,
                    offset + 2), 16);
        }
        return result;
    }

    /**
     * A runtime exception that will be thrown when we hit an error that should
     * "never" occur ... e.g. if the JVM doesn't know about UTF-8 or MD5.
     */
    @SuppressWarnings("serial")
    private static final class EvernoteUtilException extends RuntimeException {
        public EvernoteUtilException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
