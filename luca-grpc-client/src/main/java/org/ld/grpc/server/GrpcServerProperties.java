package org.ld.grpc.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("grpc.server")
public class GrpcServerProperties {
    /**
     * Security options for transport security. Defaults to disabled.
     */
    private final Security security = new Security();
    /**
     * Server port to listen on. Defaults to 9090.
     */
    private int port = 9090;
    /**
     * Bind address for the server. Defaults to 0.0.0.0.
     */
    private String address = "0.0.0.0";
    /**
     * The maximum message size allowed to be received for the server.
     */
    private int maxMessageSize;

    public Security getSecurity() {
        return security;
    }

    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    public void setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static class Security {

        /**
         * Flag that controls whether transport security is used
         */
        private Boolean enabled = false;

        /**
         * Path to SSL certificate chain
         */
        private String certificateChainPath = "";

        /**
         * Path to SSL certificate
         */
        private String certificatePath = "";

        public boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getCertificateChainPath() {
            return certificateChainPath;
        }

        public void setCertificateChainPath(String certificateChainPath) {
            this.certificateChainPath = certificateChainPath;
        }

        public String getCertificatePath() {
            return certificatePath;
        }

        public void setCertificatePath(String certificatePath) {
            this.certificatePath = certificatePath;
        }
    }
}
