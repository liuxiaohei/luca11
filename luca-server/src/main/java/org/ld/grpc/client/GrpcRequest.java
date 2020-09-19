package org.ld.grpc.client;

import com.google.protobuf.*;

/**
 * The request message containing the user's name.
 */
public final class GrpcRequest extends GeneratedMessageV3 {
    private static final long serialVersionUID = 0L;
    private static final GrpcRequest DEFAULT_INSTANCE;
    private static final Parser<GrpcRequest> PARSER = new AbstractParser<>() {
        public GrpcRequest parsePartialFrom(
                CodedInputStream input,
                ExtensionRegistryLite extensionRegistry)
                throws InvalidProtocolBufferException {
            return new GrpcRequest(input, extensionRegistry);
        }
    };

    static {
        DEFAULT_INSTANCE = new GrpcRequest();
    }

    private volatile Object params_;
    private byte memoizedIsInitialized = -1;

    public GrpcRequest(String value) {
        params_ = value;
    }

    private GrpcRequest() {
        params_ = "";
    }

    private GrpcRequest(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
        this();
        if (extensionRegistry == null) {
            throw new NullPointerException();
        }
        UnknownFieldSet.Builder unknownFields =
                UnknownFieldSet.newBuilder();
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
                        params_ = input.readStringRequireUtf8();
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

    public static GrpcRequest getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    @Override
    public final UnknownFieldSet getUnknownFields() {
        return this.unknownFields;
    }

    public FieldAccessorTable internalGetFieldAccessorTable() {
        return null;
    }

    public String getParams() {
        Object ref = params_;
        if (ref instanceof String) {
            return (String) ref;
        } else {
            ByteString bs =
                    (ByteString) ref;
            String s = bs.toStringUtf8();
            params_ = s;
            return s;
        }
    }

    public ByteString getParamsBytes() {
        Object ref = params_;
        if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String) ref);
            params_ = b;
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

    public void writeTo(CodedOutputStream output) throws java.io.IOException {
        if (!getParamsBytes().isEmpty()) {
            GeneratedMessageV3.writeString(output, 1, params_);
        }
        unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1) return size;
        size = 0;
        if (!getParamsBytes().isEmpty()) {
            size += GeneratedMessageV3.computeStringSize(1, params_);
        }
        size += unknownFields.getSerializedSize();
        memoizedSize = size;
        return size;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof GrpcRequest)) {
            return super.equals(obj);
        }
        GrpcRequest other = (GrpcRequest) obj;
        boolean result;
        result = getParams().equals(other.getParams());
        result = result && unknownFields.equals(other.unknownFields);
        return result;
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
    public Parser<GrpcRequest> getParserForType() {
        return PARSER;
    }

    public GrpcRequest getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }
}

