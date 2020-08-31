package org.ld.grpc.client;

import com.google.protobuf.*;

import java.io.IOException;

/**
 * The request message containing the user's name.
 */
public final class GrpcRequest extends GeneratedMessageV3 implements MessageOrBuilder {
    public static final int PARAMS_FIELD_NUMBER = 1;
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

    private GrpcRequest(GeneratedMessageV3.Builder<?> builder) {
        super(builder);
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

    public static Descriptors.Descriptor getDescriptor() {
        return GrpcProto.internal_static_GrpcRequest_descriptor;
    }

    public static Builder newBuilder() {
        return DEFAULT_INSTANCE.toBuilder();
    }

    public static Builder newBuilder(GrpcRequest prototype) {
        return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }

    public static GrpcRequest getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    @Override
    public final UnknownFieldSet getUnknownFields() {
        return this.unknownFields;
    }

    protected FieldAccessorTable internalGetFieldAccessorTable() {
        return GrpcProto.internal_static_GrpcRequest_fieldAccessorTable.ensureFieldAccessorsInitialized(GrpcRequest.class, Builder.class);
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

    @Override
    public int hashCode() {
        if (memoizedHashCode != 0) {
            return memoizedHashCode;
        }
        int hash = 41;
        hash = (19 * hash) + getDescriptor().hashCode();
        hash = (37 * hash) + PARAMS_FIELD_NUMBER;
        hash = (53 * hash) + getParams().hashCode();
        hash = (29 * hash) + unknownFields.hashCode();
        memoizedHashCode = hash;
        return hash;
    }

    public Builder newBuilderForType() {
        return newBuilder();
    }

    public Builder toBuilder() {
        return this == DEFAULT_INSTANCE
                ? new Builder()
                : new Builder().mergeFrom(this);
    }

    @Override
    protected Builder newBuilderForType(BuilderParent parent) {
        return new Builder(parent);
    }

    @Override
    public Parser<GrpcRequest> getParserForType() {
        return PARSER;
    }

    public GrpcRequest getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }

    public static final class Builder extends GeneratedMessageV3.Builder<Builder> implements MessageOrBuilder {
        private Object params_ = "";

        private Builder() {
        }

        private Builder(BuilderParent parent) {
            super(parent);
        }

        public static Descriptors.Descriptor getDescriptor() {
            return GrpcProto.internal_static_GrpcRequest_descriptor;
        }

        protected FieldAccessorTable internalGetFieldAccessorTable() {
            return GrpcProto.internal_static_GrpcRequest_fieldAccessorTable.ensureFieldAccessorsInitialized(GrpcRequest.class, Builder.class);
        }

        public Builder clear() {
            super.clear();
            params_ = "";
            return this;
        }

        public Descriptors.Descriptor getDescriptorForType() {
            return GrpcProto.internal_static_GrpcRequest_descriptor;
        }

        public GrpcRequest getDefaultInstanceForType() {
            return GrpcRequest.getDefaultInstance();
        }

        public GrpcRequest build() {
            GrpcRequest result = buildPartial();
            if (!result.isInitialized()) {
                throw newUninitializedMessageException(result);
            }
            return result;
        }

        public GrpcRequest buildPartial() {
            GrpcRequest result = new GrpcRequest(this);
            result.params_ = params_;
            onBuilt();
            return result;
        }

        public Builder clone() {
            return super.clone();
        }

        public Builder setField(Descriptors.FieldDescriptor field, Object value) {
            return super.setField(field, value);
        }

        public Builder clearField(Descriptors.FieldDescriptor field) {
            return super.clearField(field);
        }

        public Builder clearOneof(Descriptors.OneofDescriptor oneof) {
            return super.clearOneof(oneof);
        }

        public Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
            return super.setRepeatedField(field, index, value);
        }

        public Builder addRepeatedField(
                Descriptors.FieldDescriptor field,
                Object value) {
            return super.addRepeatedField(field, value);
        }

        public Builder mergeFrom(Message other) {
            if (other instanceof GrpcRequest) {
                return mergeFrom((GrpcRequest) other);
            } else {
                super.mergeFrom(other);
                return this;
            }
        }

        public Builder mergeFrom(GrpcRequest other) {
            if (other == GrpcRequest.getDefaultInstance()) return this;
            if (!other.getParams().isEmpty()) {
                params_ = other.params_;
                onChanged();
            }
            this.mergeUnknownFields(other.unknownFields);
            onChanged();
            return this;
        }

        public final boolean isInitialized() {
            return true;
        }

        public Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            GrpcRequest parsedMessage = null;
            try {
                parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException e) {
                parsedMessage = (GrpcRequest) e.getUnfinishedMessage();
                throw e.unwrapIOException();
            } finally {
                if (parsedMessage != null) {
                    mergeFrom(parsedMessage);
                }
            }
            return this;
        }

        public String getParams() {
            Object ref = params_;
            if (!(ref instanceof String)) {
                ByteString bs = (ByteString) ref;
                String s = bs.toStringUtf8();
                params_ = s;
                return s;
            } else {
                return (String) ref;
            }
        }

        public Builder setParams(String value) {
            if (value == null) {
                throw new NullPointerException();
            }
            params_ = value;
            onChanged();
            return this;
        }

        public Builder clearParams() {
            params_ = getDefaultInstance().getParams();
            onChanged();
            return this;
        }

        public final Builder setUnknownFields(final UnknownFieldSet unknownFields) {
            return super.setUnknownFieldsProto3(unknownFields);
        }

        public final Builder mergeUnknownFields(final UnknownFieldSet unknownFields) {
            return super.mergeUnknownFields(unknownFields);
        }
    }

}

