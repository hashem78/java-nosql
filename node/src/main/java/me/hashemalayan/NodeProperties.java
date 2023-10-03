package me.hashemalayan;

import com.google.inject.Inject;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
public class NodeProperties {
    private final int signalingPort = Integer.parseInt(System.getenv("SIGNALING_PORT"));
    private final int port = Integer.parseInt(System.getenv("PORT"));
    private final String name = System.getenv("NAME");
    private final boolean useSsl = Boolean.parseBoolean(System.getenv("USE_SSL"));
    private final Path sslCertificatePath = Paths.get(System.getenv("SSL_CERT_PATH"));
    private final Path privateKeyPath = Paths.get(System.getenv("PVT_KEY_PATH"));
    private final Path certificateAuthorityPath = Paths.get(System.getenv("CA_PATH"));

    @Inject
    public NodeProperties() {

    }
}
