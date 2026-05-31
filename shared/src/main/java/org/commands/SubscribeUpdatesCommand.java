package org.commands;

/**
 * Opens a persistent server-to-client collection update subscription.
 */
public class SubscribeUpdatesCommand extends SharedCommand {

    private static final long serialVersionUID = 1L;

    @Override
    public String exec(SharedCommandContext context) {
        return "subscribed";
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}
