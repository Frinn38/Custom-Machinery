package fr.frinn.custommachinery.impl;

import fr.frinn.custommachinery.api.utils.ICMConfig;
import fr.frinn.custommachinery.common.integration.config.CMConfig;

public class CMConfigImpl implements ICMConfig {

    public static final ICMConfig INSTANCE = new CMConfigImpl();

    @Override
    public boolean logMissingOptional() {
        return CMConfig.get().logMissingOptional;
    }

    @Override
    public boolean logFirstEitherError() {
        return CMConfig.get().logFirstEitherError;
    }
}
