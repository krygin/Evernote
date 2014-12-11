package ru.bmstu.evernote.account;

import com.evernote.edam.type.User;

import org.scribe.exceptions.OAuthException;
import org.scribe.model.Token;
import org.scribe.utils.OAuthEncoder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ivan on 11.12.2014.
 */
public class EvernoteAuthToken extends Token {

    private static final long serialVersionUID = -6892516333656106315L;

    private static final Pattern NOTESTORE_REGEX = Pattern.compile("edam_noteStoreUrl=([^&]+)");
    private static final Pattern WEBAPI_REGEX = Pattern.compile("edam_webApiUrlPrefix=([^&]+)");
    private static final Pattern USERID_REGEX = Pattern.compile("edam_userId=([^&]+)");

    private String mNoteStoreUrl;
    private String mWebApiUrlPrefix;
    private int mUserId;
    private User mUser;
    private boolean mAppLinkedNotebook;


    public EvernoteAuthToken(Token token, User user) {
        super(token.getToken(), token.getSecret(), token.getRawResponse());
        this.mNoteStoreUrl = extract(getRawResponse(), NOTESTORE_REGEX);
        this.mWebApiUrlPrefix = extract(getRawResponse(), WEBAPI_REGEX);
        this.mUserId = Integer.parseInt(extract(getRawResponse(), USERID_REGEX));
        this.mUser = user;
    }


    private String extract(String response, Pattern p) {
        Matcher matcher = p.matcher(response);
        if (matcher.find() && matcher.groupCount() >= 1) {
            return OAuthEncoder.decode(matcher.group(1));
        } else {
            throw new OAuthException("Response body is incorrect. " +
                    "Can't extract token and secret from this: '" + response + "'", null);
        }
    }

    /**
     * Get the Evernote web service NoteStore URL from the OAuth access token response.
     */
    public String getNoteStoreUrl() {
        return mNoteStoreUrl;
    }

    /**
     * Get the Evernote web API URL prefix from the OAuth access token response.
     */
    public String getWebApiUrlPrefix() {
        return mWebApiUrlPrefix;
    }

    /**
     * Get the numeric Evernote user ID from the OAuth access token response.
     */
    public int getUserId() {
        return mUserId;
    }

    /**
     * Indicates whether this account is limited to accessing a single notebook, and
     * that notebook is a linked notebook
     */
    public boolean isAppLinkedNotebook() { return mAppLinkedNotebook; }

    public User getUser() {
        return mUser;
    }
}
