package org.monarchinitiative.sss.exomiser_bridge;

import org.monarchinitiative.exomiser.core.genome.dao.PathogenicityDao;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;

import javax.sql.DataSource;

/**
 *
 */
public class SplicingPathogenicityDao implements PathogenicityDao {

    private final DataSource dataSource;

    public SplicingPathogenicityDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public PathogenicityData getPathogenicityData(Variant variant) {
        return null;
    }
}
