package me.hashemalayan;

import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
public class BootstrapProperties {
    private final int port = Integer.parseInt(System.getenv("PORT"));
    private final boolean useSSL = Boolean.parseBoolean(System.getenv("USE_SSL"));
    private final Path sslCertificatePath = Paths.get(System.getenv("SSL_CERT_PATH"));
    private final Path privateKeyPath = Paths.get(System.getenv("PVT_KEY_PATH"));
    private final Path certificateAuthorityPath = Paths.get(System.getenv("CA_PATH"));
}
