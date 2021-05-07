package fr.frinn.custommachinery.common.network.sync;

import java.util.function.Consumer;

public interface ISyncableStuff {

    void getStuffToSync(Consumer<ISyncable<?, ?>> container);
}
