package fr.frinn.custommachinery.common.network.sync.data;

public abstract class Data<T> implements IData<T> {

    private DataType<?, T> type;
    private short id;

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
