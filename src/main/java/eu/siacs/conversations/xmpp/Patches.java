package eu.siacs.conversations.xmpp;


import java.util.Arrays;
import java.util.List;

public class Patches {
    public static final List<String> DISCO_EXCEPTIONS = Arrays.asList(
            "nimbuzz.com"
    );
    public static final List<String> ENCRYPTION_EXCEPTIONS = Arrays.asList(
            "support@monocles.de"
    );
}
