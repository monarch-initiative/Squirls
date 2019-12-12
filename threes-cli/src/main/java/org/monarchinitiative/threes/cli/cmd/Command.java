package org.monarchinitiative.threes.cli.cmd;

import net.sourceforge.argparse4j.inf.Namespace;

public abstract class Command {

    public abstract void run(Namespace namespace) throws CommandException;

}
