package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.api.network.DataType;
import fr.frinn.custommachinery.api.network.IData;

public abstract class Data<T> implements IData<T> {

    private final DataType<?, T> type;
    private final short id;

    public Data(DataType<?, T> type, short id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public short getID() {
        return this.id;
    }

    @Override
    public DataType<?, T> getType() {
        return this.type;
    }
}
