package hellfirepvp.astralsorcery.common.network.channel;

import net.minecraftforge.network.simple.SimpleChannel;

public class BufferedReplyChannel extends SimpleSendChannel {

    public BufferedReplyChannel(SimpleChannel channel) {
        super(channel);
    }

}
