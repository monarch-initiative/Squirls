package org.monarchinitiative.sss.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 */
@ConfigurationProperties("sss.datasource")
public class DataSourceProperties {

    /**
     * Path to the H2 database file.
     */
    private String path;

    /**
     * Login user of the database.
     */
    private String username;

    /**
     * Login password of the database.
     */
    private String password;

    private String startupArgs;

    public String getStartupArgs() {
        return startupArgs;
    }

    public void setStartupArgs(String startupArgs) {
        this.startupArgs = startupArgs;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "DataSourceProperties{" +
                "path='" + path + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", startupArgs='" + startupArgs + '\'' +
                '}';
    }
}
