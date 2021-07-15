package fr.frinn.custommachinery.api.network;

import java.util.function.Consumer;

public interface ISyncableStuff {

    void getStuffToSync(Consumer<ISyncable<?, ?>> container);
}
