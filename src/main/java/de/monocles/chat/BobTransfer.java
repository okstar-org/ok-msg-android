package de.monocles.chat;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import io.ipfs.cid.Cid;

import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.entities.Account;
import eu.siacs.conversations.entities.DownloadableFile;
import eu.siacs.conversations.entities.Message;
import eu.siacs.conversations.entities.Transferable;
import eu.siacs.conversations.http.AesGcmURL;
import eu.siacs.conversations.services.AbstractConnectionManager;
import eu.siacs.conversations.services.XmppConnectionService;
import eu.siacs.conversations.utils.CryptoHelper;
import eu.siacs.conversations.utils.MimeUtils;
import eu.siacs.conversations.xml.Element;
import eu.siacs.conversations.xmpp.Jid;
import eu.siacs.conversations.xmpp.stanzas.IqPacket;

public class BobTransfer implements Transferable {
    protected int status = Transferable.STATUS_OFFER;
    protected URI uri;
    protected Account account;
    protected Jid to;
    protected XmppConnectionService xmppConnectionService;

    public static Cid cid(URI uri) {
        if (!uri.getScheme().equals("cid")) return null;
        String bobCid = uri.getSchemeSpecificPart();
        if (!bobCid.contains("@") || !bobCid.contains("+")) return null;
        String[] cidParts = bobCid.split("@")[0].split("\\+");
        try {
            return CryptoHelper.cid(CryptoHelper.hexToBytes(cidParts[1]), cidParts[0]);
        } catch (final NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static URI uri(Cid cid) throws NoSuchAlgorithmException, URISyntaxException {
        return new URI("cid", CryptoHelper.multihashAlgo(cid.getType()) + "+" + CryptoHelper.bytesToHex(cid.getHash()) + "@bob.xmpp.org", null);
    }

    public BobTransfer(URI uri, Account account, Jid to, XmppConnectionService xmppConnectionService) {
        this.xmppConnectionService = xmppConnectionService;
        this.uri = uri;
        this.to = to;
        this.account = account;
    }

    @Override
    public boolean start() {
        if (status == Transferable.STATUS_DOWNLOADING) return true;
        File f = xmppConnectionService.getFileForCid(cid(uri));

        if (f != null && f.canRead()) {
            finish(f);
            return true;
        }

        if (xmppConnectionService.hasInternetConnection()) {
            changeStatus(Transferable.STATUS_DOWNLOADING);

            IqPacket request = new IqPacket(IqPacket.TYPE.GET);
            request.setTo(to);
            final Element dataq = request.addChild("data", "urn:xmpp:bob");
            dataq.setAttribute("cid", uri.getSchemeSpecificPart());
            xmppConnectionService.sendIqPacket(account, request, (acct, packet) -> {
                final Element data = packet.findChild("data", "urn:xmpp:bob");
                if (packet.getType() == IqPacket.TYPE.ERROR || data == null) {
                    Log.d(Config.LOGTAG, "BobTransfer failed: " + packet);
                    finish(null);
                    xmppConnectionService.showErrorToastInUi(R.string.download_failed_file_not_found);
                } else {
                    final String contentType = data.getAttribute("type");
                    String fileExtension = "dat";
                    if (contentType != null) {
                        fileExtension = MimeUtils.guessExtensionFromMimeType(contentType);
                    }

                    try {
                        final byte[] bytes = Base64.decode(data.getContent(), Base64.DEFAULT);

                        File file = xmppConnectionService.getFileBackend().getStorageLocation(new ByteArrayInputStream(bytes), fileExtension);
                        file.getParentFile().mkdirs();
                        if (!file.exists() && !file.createNewFile()) {
                            throw new IOException(file.getAbsolutePath());
                        }

                        final OutputStream outputStream = AbstractConnectionManager.createOutputStream(new DownloadableFile(file.getAbsolutePath()), false, false);
                        outputStream.write(bytes);
                        outputStream.flush();
                        outputStream.close();

                        finish(file);
                    } catch (IOException e) {
                        finish(null);
                        xmppConnectionService.showErrorToastInUi(R.string.download_failed_could_not_write_file);
                    }
                }
            });
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public Long getFileSize() {
        return null;
    }

    @Override
    public void cancel() {
        // No real way to cancel an iq in process...
        changeStatus(Transferable.STATUS_CANCELLED);
    }

    protected void changeStatus(int newStatus) {
        status = newStatus;
        xmppConnectionService.updateConversationUi();
    }

    protected void finish(File f) {
        if (f != null) xmppConnectionService.updateConversationUi();
    }

    public static class ForMessage extends BobTransfer {
        protected Message message;

        public ForMessage(Message message, XmppConnectionService xmppConnectionService) throws URISyntaxException {
            super(new URI(message.getFileParams().url), message.getConversation().getAccount(), message.getCounterpart(), xmppConnectionService);
            this.message = message;
        }

        @Override
        public void cancel() {
            super.cancel();
            message.setTransferable(null);
        }

        @Override
        protected void finish(File f) {
            if (f != null) {
                message.setRelativeFilePath(f.getAbsolutePath());
                final boolean privateMessage = message.isPrivateMessage();
                message.setType(privateMessage ? Message.TYPE_PRIVATE_FILE : Message.TYPE_FILE);
                xmppConnectionService.getFileBackend().updateFileParams(message, uri.toString(), false);
                xmppConnectionService.updateMessage(message);
            }
            message.setTransferable(null);
            super.finish(f);
        }
    }
}
