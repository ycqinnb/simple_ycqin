package yc.ycqin.nb.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class ParasiteEvolutionPacket implements IMessage {
    private int dimension;
    private int phase;
    private int totalPoints;
    private int nextPoints;   // 下一阶段所需点数

    public ParasiteEvolutionPacket() {}

    public ParasiteEvolutionPacket(int dimension, int phase, int totalPoints, int nextPoints) {
        this.dimension = dimension;
        this.phase = phase;
        this.totalPoints = totalPoints;
        this.nextPoints = nextPoints;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        dimension = buf.readInt();
        phase = buf.readInt();
        totalPoints = buf.readInt();
        nextPoints = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimension);
        buf.writeInt(phase);
        buf.writeInt(totalPoints);
        buf.writeInt(nextPoints);
    }

    public int getDimension() { return dimension; }
    public int getPhase() { return phase; }
    public int getTotalPoints() { return totalPoints; }
    public int getNextPoints() { return nextPoints; }
}