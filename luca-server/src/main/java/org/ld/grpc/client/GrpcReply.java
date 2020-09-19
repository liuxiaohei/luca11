package org.ld.grpc.client;

import com.google.protobuf.*;

import java.io.IOException;

/**
 * The response message containing the greetings
 */
public final class GrpcReply extends GeneratedMessageV3 implements MessageOrBuilder {
    private static final long serialVersionUID = 0L;
    private static final GrpcReply DEFAULT_INSTANCE;
    private static final Parser<GrpcReply> PARSER = new AbstractParser<>() {
        public GrpcReply parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new GrpcReply(input, extensionRegistry);
        }
    };

    static {
        DEFAULT_INSTANCE = new GrpcReply();
    }

    private volatile Object message_;
    private byte memoizedIsInitialized = -1;

    public GrpcReply(String value) {
        message_ = value;
    }

    private GrpcReply() {
        message_ = "";
    }

    private GrpcReply(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        this();
        if (extensionRegistry == null) {
            throw new NullPointerException();
        }
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
                        if (!parseUnknownFieldProto3(
                                input, unknownFields, extensionRegistry, tag)) {
                            done = true;
                        }
                        break;
                    }
                    case 10: {
                        message_ = input.readStringRequireUtf8();
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

    public static Descriptors.Descriptor getDescriptor() {
        return null;
    }

    public static GrpcReply getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    @Override
    public final UnknownFieldSet getUnknownFields() {
        return this.unknownFields;
    }

    protected FieldAccessorTable internalGetFieldAccessorTable() {
        return null;
    }

    public String getMessage() {
        Object ref = message_;
        if (ref instanceof String) {
            return (String) ref;
        } else {
            ByteString bs = (ByteString) ref;
            String s = bs.toStringUtf8();
            message_ = s;
            return s;
        }
    }

    public ByteString getMessageBytes() {
        Object ref = message_;
        if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String) ref);
            message_ = b;
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
            GeneratedMessageV3.writeString(output, 1, message_);
        }
        unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1) return size;
        size = 0;
        if (!getMessageBytes().isEmpty()) {
            size += GeneratedMessageV3.computeStringSize(1, message_);
        }
        size += unknownFields.getSerializedSize();
        memoizedSize = size;
        return size;
    }

    public Builder newBuilderForType() {
        return null;
    }

    public Builder toBuilder() {
        return null;
    }

    @Override
    protected Builder newBuilderForType(BuilderParent parent) {
        return null;
    }

    @Override
    public Parser<GrpcReply> getParserForType() {
        return PARSER;
    }

    public GrpcReply getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }

}

