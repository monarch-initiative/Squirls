package org.monarchinitiative.threes.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author Daniel Danis <daniel.danis@jax.org>
 */
@ConfigurationProperties(prefix = "sss")
public class ThreeSProperties {

    @NestedConfigurationProperty
    private DataSourceProperties datasource = new DataSourceProperties();

    private String dataDirectory;

    private String genomeFastaPath;

    private String genomeFastaFaiPath;

    public ThreeSProperties() {
    }

    public String getGenomeFastaPath() {
        return genomeFastaPath;
    }

    public void setGenomeFastaPath(String genomeFastaPath) {
        this.genomeFastaPath = genomeFastaPath;
    }

    public DataSourceProperties getDatasource() {
        return datasource;
    }

    public void setDatasource(DataSourceProperties datasource) {
        this.datasource = datasource;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    @Override
    public String toString() {
        return "ThreeSProperties{" +
                "dataDirectory='" + dataDirectory + '\'' +
                ", genomeFastaPath='" + genomeFastaPath + '\'' +
                ", genomeFastaFaiPath='" + genomeFastaFaiPath + '\'' +
                ", datasource=" + datasource +
                '}';
    }

    public String getGenomeFastaFaiPath() {
        return genomeFastaFaiPath;
    }

    public void setGenomeFastaFaiPath(String genomeFastaFaiPath) {
        this.genomeFastaFaiPath = genomeFastaFaiPath;
    }
}
