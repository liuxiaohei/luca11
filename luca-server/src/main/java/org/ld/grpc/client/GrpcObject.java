package org.ld.grpc.client;

import com.google.protobuf.*;

import java.io.IOException;

/**
 * grpc通信传输的对象
 */
public final class GrpcObject extends GeneratedMessageV3 {
    private static final long serialVersionUID = 0L;

    private volatile Object value;
    private byte memoizedIsInitialized = -1;

    public GrpcObject(String value) {
        this.value = value;
    }

    public GrpcObject() {
        value = "";
    }

    private GrpcObject(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        value = "";
        UnknownFieldSet.Builder unknownFields = UnknownFieldSet.newBuilder();
        try {
            boolean done = false;
            while (!done) {
                int tag = input.readTag();
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

    public String getValue() {
        Object ref = value;
        if (ref instanceof String) {
            return (String) ref;
        } else {
            ByteString bs = (ByteString) ref;
            String s = bs.toStringUtf8();
            value = s;
            return s;
        }
    }

    private ByteString getMessageBytes() {
        Object ref = value;
        if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String) ref);
            value = b;
            return b;
        } else {
            return (ByteString) ref;
        }
    }

    public final boolean isInitialized() {
        byte isInitialized = memoizedIsInitialized;
        if (isInitialized == 1) return true;
        if (isInitialized == 0) return false;
        memoizedIsInitialized = 1;
        return true;
    }

    public void writeTo(CodedOutputStream output) throws IOException {
        if (!getMessageBytes().isEmpty()) {
            GeneratedMessageV3.writeString(output, 1, value);
        }
        unknownFields.writeTo(output);
    }

    /**
     * 不能删 会卡住
     */
    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1) return size;
        size = 0;
        if (!getMessageBytes().isEmpty()) {
            size += GeneratedMessageV3.computeStringSize(1, value);
        }
        size += unknownFields.getSerializedSize();
        memoizedSize = size;
        return size;
    }

    @Override
    public Parser<GrpcObject> getParserForType() {
        return new AbstractParser<>() {
            public GrpcObject parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                return new GrpcObject(input, extensionRegistry);
            }
        };
    }

    public GrpcObject getDefaultInstanceForType() {
        return null;
    }

    @Deprecated
    public Builder newBuilderForType() {
        return null;
    }

    @Deprecated
    public Builder toBuilder() {
        return null;
    }

    @Deprecated
    protected Builder newBuilderForType(BuilderParent parent) {
        return null;
    }

    @Deprecated
    protected FieldAccessorTable internalGetFieldAccessorTable() {
        return null;
    }

}

