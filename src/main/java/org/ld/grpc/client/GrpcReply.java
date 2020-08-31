package org.ld.grpc.client;

import com.google.protobuf.*;

/**
 * The response message containing the greetings
 */
public final class GrpcReply extends GeneratedMessageV3 implements MessageOrBuilder {
    public static final int MESSAGE_FIELD_NUMBER = 1;
    private static final long serialVersionUID = 0L;
    private static final GrpcReply DEFAULT_INSTANCE;
    private static final Parser<GrpcReply>
            PARSER = new AbstractParser<>() {
        public GrpcReply parsePartialFrom(
                CodedInputStream input,
                ExtensionRegistryLite extensionRegistry)
                throws InvalidProtocolBufferException {
            return new GrpcReply(input, extensionRegistry);
        }
    };

    static {
        DEFAULT_INSTANCE = new GrpcReply();
    }

    private volatile Object message_;
    private byte memoizedIsInitialized = -1;

    private GrpcReply(GeneratedMessageV3.Builder<?> builder) {
        super(builder);
    }

    private GrpcReply() {
        message_ = "";
    }

    private GrpcReply(
            CodedInputStream input,
            ExtensionRegistryLite extensionRegistry)
            throws InvalidProtocolBufferException {
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

    public static Descriptors.Descriptor
    getDescriptor() {
        return GrpcProto.internal_static_GrpcReply_descriptor;
    }

    public static Builder newBuilder() {
        return DEFAULT_INSTANCE.toBuilder();
    }

    public static Builder newBuilder(GrpcReply prototype) {
        return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }

    public static GrpcReply getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    @Override
    public final UnknownFieldSet
    getUnknownFields() {
        return this.unknownFields;
    }

    protected FieldAccessorTable
    internalGetFieldAccessorTable() {
        return GrpcProto.internal_static_GrpcReply_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        GrpcReply.class, Builder.class);
    }

    public String getMessage() {
        Object ref = message_;
        if (ref instanceof String) {
            return (String) ref;
        } else {
            ByteString bs =
                    (ByteString) ref;
            String s = bs.toStringUtf8();
            message_ = s;
            return s;
        }
    }

    public ByteString
    getMessageBytes() {
        Object ref = message_;
        if (ref instanceof String) {
            ByteString b =
                    ByteString.copyFromUtf8(
                            (String) ref);
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

    public void writeTo(CodedOutputStream output)
            throws java.io.IOException {
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

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof GrpcReply)) {
            return super.equals(obj);
        }
        GrpcReply other = (GrpcReply) obj;
        boolean result;
        result = getMessage().equals(other.getMessage());
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
        hash = (37 * hash) + MESSAGE_FIELD_NUMBER;
        hash = (53 * hash) + getMessage().hashCode();
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
    protected Builder newBuilderForType(
            BuilderParent parent) {
        return new Builder(parent);
    }

    @Override
    public Parser<GrpcReply> getParserForType() {
        return PARSER;
    }

    public GrpcReply getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }

    public static final class Builder extends
            GeneratedMessageV3.Builder<Builder> implements
            MessageOrBuilder {
        private Object message_ = "";

        private Builder() {
            maybeForceBuilderInitialization();
        }

        private Builder(
                BuilderParent parent) {
            super(parent);
            maybeForceBuilderInitialization();
        }

        public static Descriptors.Descriptor
        getDescriptor() {
            return GrpcProto.internal_static_GrpcReply_descriptor;
        }

        protected FieldAccessorTable
        internalGetFieldAccessorTable() {
            return GrpcProto.internal_static_GrpcReply_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            GrpcReply.class, Builder.class);
        }

        private void maybeForceBuilderInitialization() {
        }

        public Builder clear() {
            super.clear();
            message_ = "";

            return this;
        }

        public Descriptors.Descriptor
        getDescriptorForType() {
            return GrpcProto.internal_static_GrpcReply_descriptor;
        }

        public GrpcReply getDefaultInstanceForType() {
            return GrpcReply.getDefaultInstance();
        }

        public GrpcReply build() {
            GrpcReply result = buildPartial();
            if (!result.isInitialized()) {
                throw newUninitializedMessageException(result);
            }
            return result;
        }

        public GrpcReply buildPartial() {
            GrpcReply result = new GrpcReply(this);
            result.message_ = message_;
            onBuilt();
            return result;
        }

        public Builder clone() {
            return super.clone();
        }

        public Builder setField(
                Descriptors.FieldDescriptor field,
                Object value) {
            return super.setField(field, value);
        }

        public Builder clearField(
                Descriptors.FieldDescriptor field) {
            return super.clearField(field);
        }

        public Builder clearOneof(
                Descriptors.OneofDescriptor oneof) {
            return super.clearOneof(oneof);
        }

        public Builder setRepeatedField(
                Descriptors.FieldDescriptor field,
                int index, Object value) {
            return super.setRepeatedField(field, index, value);
        }

        public Builder addRepeatedField(
                Descriptors.FieldDescriptor field,
                Object value) {
            return super.addRepeatedField(field, value);
        }

        public Builder mergeFrom(Message other) {
            if (other instanceof GrpcReply) {
                return mergeFrom((GrpcReply) other);
            } else {
                super.mergeFrom(other);
                return this;
            }
        }

        public Builder mergeFrom(GrpcReply other) {
            if (other == GrpcReply.getDefaultInstance()) return this;
            if (!other.getMessage().isEmpty()) {
                message_ = other.message_;
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
                CodedInputStream input,
                ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            GrpcReply parsedMessage = null;
            try {
                parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException e) {
                parsedMessage = (GrpcReply) e.getUnfinishedMessage();
                throw e.unwrapIOException();
            } finally {
                if (parsedMessage != null) {
                    mergeFrom(parsedMessage);
                }
            }
            return this;
        }

        public String getMessage() {
            Object ref = message_;
            if (!(ref instanceof String)) {
                ByteString bs =
                        (ByteString) ref;
                String s = bs.toStringUtf8();
                message_ = s;
                return s;
            } else {
                return (String) ref;
            }
        }

        public Builder setMessage(
                String value) {
            if (value == null) {
                throw new NullPointerException();
            }

            message_ = value;
            onChanged();
            return this;
        }

        public Builder clearMessage() {

            message_ = getDefaultInstance().getMessage();
            onChanged();
            return this;
        }

        public final Builder setUnknownFields(
                final UnknownFieldSet unknownFields) {
            return super.setUnknownFieldsProto3(unknownFields);
        }

        public final Builder mergeUnknownFields(
                final UnknownFieldSet unknownFields) {
            return super.mergeUnknownFields(unknownFields);
        }

    }

}

