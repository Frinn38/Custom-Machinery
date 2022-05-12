package fr.frinn.custommachinery.common.config;

import fr.frinn.custommachinery.api.utils.ICMConfig;

public class CMConfigImpl implements ICMConfig {

    public static final ICMConfig INSTANCE = new CMConfigImpl();

    @Override
    public boolean logMissingOptional() {
        return CMConfig.INSTANCE.logMissingOptional.get();
    }

    @Override
    public boolean logFirstEitherError() {
        return CMConfig.INSTANCE.logFirstEitherError.get();
    }
}
