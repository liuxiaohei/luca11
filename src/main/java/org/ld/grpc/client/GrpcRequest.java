package org.ld.grpc.client;

/**
 * <pre>
 * The request message containing the user's name.
 * </pre>
 * <p>
 * Protobuf type {@code GrpcRequest}
 */
public final class GrpcRequest extends
        com.google.protobuf.GeneratedMessageV3 implements
        com.google.protobuf.MessageOrBuilder {
    public static final int PARAMS_FIELD_NUMBER = 1;
    private static final long serialVersionUID = 0L;
    private static final GrpcRequest DEFAULT_INSTANCE;
    private static final com.google.protobuf.Parser<GrpcRequest>
            PARSER = new com.google.protobuf.AbstractParser<>() {
        public GrpcRequest parsePartialFrom(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return new GrpcRequest(input, extensionRegistry);
        }
    };

    static {
        DEFAULT_INSTANCE = new GrpcRequest();
    }

    private volatile Object params_;
    private byte memoizedIsInitialized = -1;

    private GrpcRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
        super(builder);
    }

    private GrpcRequest() {
        params_ = "";
    }

    private GrpcRequest(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
        this();
        if (extensionRegistry == null) {
            throw new NullPointerException();
        }
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
                com.google.protobuf.UnknownFieldSet.newBuilder();
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
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            throw e.setUnfinishedMessage(this);
        } catch (java.io.IOException e) {
            throw new com.google.protobuf.InvalidProtocolBufferException(
                    e).setUnfinishedMessage(this);
        } finally {
            this.unknownFields = unknownFields.build();
            makeExtensionsImmutable();
        }
    }

    public static com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
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
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
        return this.unknownFields;
    }

    protected FieldAccessorTable
    internalGetFieldAccessorTable() {
        return GrpcProto.internal_static_GrpcRequest_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        GrpcRequest.class, Builder.class);
    }

    /**
     * <code>string params = 1;</code>
     */
    public String getParams() {
        Object ref = params_;
        if (ref instanceof String) {
            return (String) ref;
        } else {
            com.google.protobuf.ByteString bs =
                    (com.google.protobuf.ByteString) ref;
            String s = bs.toStringUtf8();
            params_ = s;
            return s;
        }
    }

    /**
     * <code>string params = 1;</code>
     */
    public com.google.protobuf.ByteString
    getParamsBytes() {
        Object ref = params_;
        if (ref instanceof String) {
            com.google.protobuf.ByteString b =
                    com.google.protobuf.ByteString.copyFromUtf8(
                            (String) ref);
            params_ = b;
            return b;
        } else {
            return (com.google.protobuf.ByteString) ref;
        }
    }

    public final boolean isInitialized() {
        byte isInitialized = memoizedIsInitialized;
        if (isInitialized == 1) return true;
        if (isInitialized == 0) return false;

        memoizedIsInitialized = 1;
        return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
            throws java.io.IOException {
        if (!getParamsBytes().isEmpty()) {
            com.google.protobuf.GeneratedMessageV3.writeString(output, 1, params_);
        }
        unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1) return size;

        size = 0;
        if (!getParamsBytes().isEmpty()) {
            size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, params_);
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
        result = getParams()
                .equals(other.getParams());
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
                ? new Builder() : new Builder().mergeFrom(this);
    }

    @Override
    protected Builder newBuilderForType(
            BuilderParent parent) {
        return new Builder(parent);
    }

    @Override
    public com.google.protobuf.Parser<GrpcRequest> getParserForType() {
        return PARSER;
    }

    public GrpcRequest getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }

    /**
     * <pre>
     * The request message containing the user's name.
     * </pre>
     * <p>
     * Protobuf type {@code GrpcRequest}
     */
    public static final class Builder extends
            com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
            com.google.protobuf.MessageOrBuilder {
        private Object params_ = "";

        private Builder() {
            maybeForceBuilderInitialization();
        }

        private Builder(
                BuilderParent parent) {
            super(parent);
            maybeForceBuilderInitialization();
        }

        public static com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
            return GrpcProto.internal_static_GrpcRequest_descriptor;
        }

        protected FieldAccessorTable
        internalGetFieldAccessorTable() {
            return GrpcProto.internal_static_GrpcRequest_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            GrpcRequest.class, Builder.class);
        }

        private void maybeForceBuilderInitialization() {
        }

        public Builder clear() {
            super.clear();
            params_ = "";

            return this;
        }

        public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
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

        public Builder setField(
                com.google.protobuf.Descriptors.FieldDescriptor field,
                Object value) {
            return super.setField(field, value);
        }

        public Builder clearField(
                com.google.protobuf.Descriptors.FieldDescriptor field) {
            return super.clearField(field);
        }

        public Builder clearOneof(
                com.google.protobuf.Descriptors.OneofDescriptor oneof) {
            return super.clearOneof(oneof);
        }

        public Builder setRepeatedField(
                com.google.protobuf.Descriptors.FieldDescriptor field,
                int index, Object value) {
            return super.setRepeatedField(field, index, value);
        }

        public Builder addRepeatedField(
                com.google.protobuf.Descriptors.FieldDescriptor field,
                Object value) {
            return super.addRepeatedField(field, value);
        }

        public Builder mergeFrom(com.google.protobuf.Message other) {
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

        public Builder mergeFrom(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            GrpcRequest parsedMessage = null;
            try {
                parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                parsedMessage = (GrpcRequest) e.getUnfinishedMessage();
                throw e.unwrapIOException();
            } finally {
                if (parsedMessage != null) {
                    mergeFrom(parsedMessage);
                }
            }
            return this;
        }

        /**
         * <code>string params = 1;</code>
         */
        public String getParams() {
            Object ref = params_;
            if (!(ref instanceof String)) {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                String s = bs.toStringUtf8();
                params_ = s;
                return s;
            } else {
                return (String) ref;
            }
        }

        /**
         * <code>string params = 1;</code>
         */
        public Builder setParams(
                String value) {
            if (value == null) {
                throw new NullPointerException();
            }

            params_ = value;
            onChanged();
            return this;
        }

        /**
         * <code>string params = 1;</code>
         */
        public com.google.protobuf.ByteString
        getParamsBytes() {
            Object ref = params_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (String) ref);
                params_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public Builder clearParams() {

            params_ = getDefaultInstance().getParams();
            onChanged();
            return this;
        }

        public final Builder setUnknownFields(
                final com.google.protobuf.UnknownFieldSet unknownFields) {
            return super.setUnknownFieldsProto3(unknownFields);
        }

        public final Builder mergeUnknownFields(
                final com.google.protobuf.UnknownFieldSet unknownFields) {
            return super.mergeUnknownFields(unknownFields);
        }
    }

}

