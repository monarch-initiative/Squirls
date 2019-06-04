package org.monarchinitiative.sss.ingest.config;

/**
 *
 */
public class DbConfig {

    private String path;

    private String user;

    private String password;

    private String startupArgs;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStartupArgs() {
        return startupArgs;
    }

    public void setStartupArgs(String startupArgs) {
        this.startupArgs = startupArgs;
    }
}
