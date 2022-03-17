/*
 * Copyright (c) 2018, Daniel Gultsch All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package eu.siacs.conversations.ui.util;

import android.content.ActivityNotFoundException;
import android.content.Intent;

import java.util.regex.Matcher;

import eu.siacs.conversations.R;
import eu.siacs.conversations.entities.DownloadableFile;
import eu.siacs.conversations.entities.Message;
import eu.siacs.conversations.persistance.FileBackend;
import eu.siacs.conversations.ui.ConversationsActivity;
import eu.siacs.conversations.ui.XmppActivity;
import eu.siacs.conversations.utils.Patterns;
import eu.siacs.conversations.utils.XmppUri;
import eu.siacs.conversations.xmpp.Jid;
import me.drakeet.support.toast.ToastCompat;

public class ShareUtil {

    public static void share(XmppActivity activity, Message message, String user) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        if (message.isGeoUri()) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, message.getBody());
            shareIntent.setType("text/plain");
            shareIntent.putExtra(ConversationsActivity.EXTRA_AS_QUOTE, false);
        } else if (!message.isFileOrImage()) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, message.getMergedBody().toString());
            shareIntent.setType("text/plain");
            shareIntent.putExtra(ConversationsActivity.EXTRA_AS_QUOTE, true);
            shareIntent.putExtra(ConversationsActivity.EXTRA_USER, user);
        } else {
            final DownloadableFile file = activity.xmppConnectionService.getFileBackend().getFile(message);
            try {
                shareIntent.putExtra(Intent.EXTRA_STREAM, FileBackend.getUriForFile(activity, file));
            } catch (SecurityException e) {
                ToastCompat.makeText(activity, activity.getString(R.string.no_permission_to_access_x, file.getAbsolutePath()), ToastCompat.LENGTH_SHORT).show();
                return;
            }
            shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String mime = message.getMimeType();
            if (mime == null) {
                mime = "*/*";
            }
            shareIntent.setType(mime);
        }
        try {
            activity.startActivity(Intent.createChooser(shareIntent, activity.getText(R.string.share_with)));
            activity.overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
        } catch (ActivityNotFoundException e) {
            //This should happen only on faulty androids because normally chooser is always available
            ToastCompat.makeText(activity, R.string.no_application_found_to_open_file, ToastCompat.LENGTH_SHORT).show();
        }
    }

    public static void copyToClipboard(XmppActivity activity, Message message) {
        if (activity.copyTextToClipboard(message.getMergedBody().toString(), R.string.message)) {
            ToastCompat.makeText(activity, R.string.message_copied_to_clipboard, ToastCompat.LENGTH_SHORT).show();
        }
    }

    public static void copyUrlToClipboard(XmppActivity activity, Message message) {
        final String url;
        final int resId;
        if (message.isGeoUri()) {
            resId = R.string.location;
            url = message.getBody();
        } else if (message.hasFileOnRemoteHost()) {
            resId = R.string.file_url;
			url = message.getFileParams().url;
        } else {
			final Message.FileParams fileParams = message.getFileParams();
			url = (fileParams != null && fileParams.url != null) ? fileParams.url : message.getBody().trim();
            resId = R.string.file_url;
        }
        if (activity.copyTextToClipboard(url, resId)) {
            ToastCompat.makeText(activity, R.string.url_copied_to_clipboard, ToastCompat.LENGTH_SHORT).show();
        }
    }

    public static void copyLinkToClipboard(XmppActivity activity, Message message) {
        String body = message.getMergedBody().toString();
        Matcher xmppPatternMatcher = Patterns.XMPP_PATTERN.matcher(body);
        if (xmppPatternMatcher.find()) {
            try {
                Jid jid = new XmppUri(body.substring(xmppPatternMatcher.start(), xmppPatternMatcher.end())).getJid();
                if (activity.copyTextToClipboard(jid.asBareJid().toString(), R.string.account_settings_jabber_id)) {
                    ToastCompat.makeText(activity, R.string.jabber_id_copied_to_clipboard, ToastCompat.LENGTH_SHORT).show();
                }
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        Matcher webUrlPatternMatcher = Patterns.WEB_URL.matcher(body);
        if (webUrlPatternMatcher.find()) {
            String url = body.substring(webUrlPatternMatcher.start(), webUrlPatternMatcher.end());
            if (activity.copyTextToClipboard(url, R.string.web_address)) {
                ToastCompat.makeText(activity, R.string.url_copied_to_clipboard, ToastCompat.LENGTH_SHORT).show();
            }
        }
    }

    public static boolean containsXmppUri(String body) {
        Matcher xmppPatternMatcher = Patterns.XMPP_PATTERN.matcher(body);
        if (xmppPatternMatcher.find()) {
            try {
                return new XmppUri(body.substring(xmppPatternMatcher.start(), xmppPatternMatcher.end())).isValidJid();
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}