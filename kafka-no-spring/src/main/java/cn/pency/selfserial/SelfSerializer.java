package cn.pency.selfserial;

import cn.pency.vo.DemoUser;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * @author pency
 * 类说明：
 */
public class SelfSerializer implements Serializer<DemoUser> {
    public void configure(Map<String, ?> configs, boolean isKey) {
        //do nothing
    }

    public byte[] serialize(String topic, DemoUser data) {
        try {
            byte[] name;
            int nameSize;
            if(data==null){
                return null;
            }
            if(data.getName()!=null){
                name = data.getName().getBytes("UTF-8");
                //字符串的长度
                nameSize = data.getName().length();
            }else{
                name = new byte[0];
                nameSize = 0;
            }
            /*id的长度4个字节，字符串的长度描述4个字节，
            字符串本身的长度nameSize个字节*/
            ByteBuffer buffer = ByteBuffer.allocate(4+4+nameSize);
            buffer.putInt(data.getId());//4
            buffer.putInt(nameSize);//4
            buffer.put(name);//nameSize
            return buffer.array();
        } catch (Exception e) {
            throw new SerializationException("Error serialize DemoUser:"+e);
        }
    }

    public void close() {
        //do nothing
    }
}
