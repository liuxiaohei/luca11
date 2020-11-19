package org.ld.grpc.grpc;

import com.google.protobuf.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.ld.utils.JsonUtil;

import java.io.IOException;

/**
 * grpc通信传输对象
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public final class GrpcMessage extends GeneratedMessageV3 {

    private static final long serialVersionUID = 0L;

    private volatile Object value;
    private byte memoizedIsInitialized = -1;

    public static GrpcMessage stringObj(String value) {
        return new GrpcMessage(value);
    }

    public static GrpcMessage obj(Object value) {
        return new GrpcMessage(JsonUtil.obj2Json(value));
    }

    private GrpcMessage(String value) {
        this.value = value;
    }

    public GrpcMessage() {
        value = "";
    }

    private GrpcMessage(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        value = "";
        var unknownFields = UnknownFieldSet.newBuilder();
        try {
            var done = false;
            while (!done) {
                var tag = input.readTag();
                switch (tag) {
                    case 0:
                        done = true;
                        break;
                    default: {
                        if (!parseUnknownFieldProto3(input, unknownFields, extensionRegistry, tag)) {
                            done = true;
                        }
                        break;
                    }
                    case 10: {
                        value = input.readStringRequireUtf8();
                        break;
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            throw e.setUnfinishedMessage(this);
        } catch (java.io.IOException e) {
            throw new InvalidProtocolBufferException(e).setUnfinishedMessage(this);
        } finally {
            this.unknownFields = unknownFields.build();
            makeExtensionsImmutable();
        }
    }

    @Override
    public final UnknownFieldSet getUnknownFields() {
        return this.unknownFields;
    }

    public <T> T getObj(Class<T> clazz) {
        var json = getStringObj();
        return JsonUtil.json2Obj(json, clazz);
    }

    public String getStringObj() {
        var ref = value;
        if (ref instanceof String) {
            return (String) ref;
        } else {
            var bs = (ByteString) ref;
            var s = bs.toStringUtf8();
            value = s;
            return s;
        }
    }

    private ByteString getMessageBytes() {
        var ref = value;
        if (ref instanceof String) {
            var b = ByteString.copyFromUtf8((String) ref);
            value = b;
            return b;
        } else {
            return (ByteString) ref;
        }
    }

    public final boolean isInitialized() {
        var isInitialized = memoizedIsInitialized;
        if (isInitialized == 1) return true;
        if (isInitialized == 0) return false;
        memoizedIsInitialized = 1;
        return true;
    }

    public void writeTo(CodedOutputStream output) throws IOException {
        if (!getMessageBytes().isEmpty()) {
            writeString(output, 1, value);
        }
        unknownFields.writeTo(output);
    }

    /**
     * 协议必须 不能删 否则会卡住
     */
    public int getSerializedSize() {
        var size = memoizedSize;
        if (size != -1) return size;
        size = 0;
        if (!getMessageBytes().isEmpty()) {
            size += computeStringSize(1, value);
        }
        size += unknownFields.getSerializedSize();
        memoizedSize = size;
        return size;
    }

    @Override
    public Parser<GrpcMessage> getParserForType() {
        return new AbstractParser<>() {
            public GrpcMessage parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                return new GrpcMessage(input, extensionRegistry);
            }
        };
    }

    // ↓↓ 非必须的部分 但是抽象方法必须要继承
    public GrpcMessage getDefaultInstanceForType() {
        return null;
    }

    @Deprecated
    public Builder<? extends Message.Builder> newBuilderForType() {
        return null;
    }

    @Deprecated
    public Builder<? extends Message.Builder> toBuilder() {
        return null;
    }

    @Deprecated
    protected Builder<? extends Message.Builder> newBuilderForType(BuilderParent parent) {
        return null;
    }

    @Deprecated
    protected FieldAccessorTable internalGetFieldAccessorTable() {
        return null;
    }

}

