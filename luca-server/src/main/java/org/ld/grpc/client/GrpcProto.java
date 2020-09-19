package org.ld.grpc.client;

import com.google.protobuf.Descriptors;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.GeneratedMessageV3;

public final class GrpcProto {
    static final Descriptors.Descriptor internal_static_GrpcRequest_descriptor;
    static final GeneratedMessageV3.FieldAccessorTable internal_static_GrpcRequest_fieldAccessorTable;
    static final Descriptors.Descriptor internal_static_GrpcReply_descriptor;
    static final GeneratedMessageV3.FieldAccessorTable internal_static_GrpcReply_fieldAccessorTable;

    static {
        internal_static_GrpcRequest_descriptor = getDescriptor().getMessageTypes().get(0);
        internal_static_GrpcRequest_fieldAccessorTable = new GeneratedMessageV3.FieldAccessorTable(internal_static_GrpcRequest_descriptor,
                new String[]{"Params",});
        internal_static_GrpcReply_descriptor = getDescriptor().getMessageTypes().get(1);
        internal_static_GrpcReply_fieldAccessorTable = new GeneratedMessageV3.FieldAccessorTable(internal_static_GrpcReply_descriptor,
                new String[]{"Message",});
    }

    private GrpcProto() {
    }

    public static Descriptors.FileDescriptor getDescriptor() {
        return null;
    }

}
