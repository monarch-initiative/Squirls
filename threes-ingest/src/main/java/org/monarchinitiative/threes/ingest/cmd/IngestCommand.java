package org.monarchinitiative.threes.ingest.cmd;

import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Base CLI command within the ingest module.
 */
public abstract class IngestCommand {

    public abstract void run(Namespace args) throws Exception;
}
