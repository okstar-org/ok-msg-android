package eu.siacs.conversations.crypto;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLPeerUnverifiedException;

public interface DomainHostnameVerifier extends HostnameVerifier {

    boolean verify(String domain, String hostname, SSLSession sslSession) throws SSLPeerUnverifiedException;
}