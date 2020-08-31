package org.ld.grpc.client;

import com.google.protobuf.Descriptors;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.GeneratedMessageV3;

public final class GrpcProto {
    static final Descriptors.Descriptor
            internal_static_GrpcRequest_descriptor;
    static final
    GeneratedMessageV3.FieldAccessorTable
            internal_static_GrpcRequest_fieldAccessorTable;
    static final Descriptors.Descriptor
            internal_static_GrpcReply_descriptor;
    static final
    GeneratedMessageV3.FieldAccessorTable
            internal_static_GrpcReply_fieldAccessorTable;
    private static Descriptors.FileDescriptor
            descriptor;

    static {
        String[] descriptorData = {
                "\n\013hello.proto\"\035\n\013GrpcRequest\022\016\n\006params\030\001" +
                        " \001(\t\"\034\n\tGrpcReply\022\017\n\007message\030\001 \001(\t24\n\007Gr" +
                        "eeter\022)\n\013sendMessage\022\014.GrpcRequest\032\n.Grp" +
                        "cReply\"\000B\023B\tGrpcProtoP\001\242\002\003HLWb\006proto3"
        };
        Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
                new Descriptors.FileDescriptor.InternalDescriptorAssigner() {
                    public ExtensionRegistry assignDescriptors(
                            Descriptors.FileDescriptor root) {
                        descriptor = root;
                        return null;
                    }
                };
        Descriptors.FileDescriptor
                .internalBuildGeneratedFileFrom(descriptorData,
                        new Descriptors.FileDescriptor[]{
                        }, assigner);
        internal_static_GrpcRequest_descriptor =
                getDescriptor().getMessageTypes().get(0);
        internal_static_GrpcRequest_fieldAccessorTable = new
                GeneratedMessageV3.FieldAccessorTable(
                internal_static_GrpcRequest_descriptor,
                new String[]{"Params",});
        internal_static_GrpcReply_descriptor =
                getDescriptor().getMessageTypes().get(1);
        internal_static_GrpcReply_fieldAccessorTable = new
                GeneratedMessageV3.FieldAccessorTable(
                internal_static_GrpcReply_descriptor,
                new String[]{"Message",});
    }

    private GrpcProto() {
    }

    public static Descriptors.FileDescriptor getDescriptor() {
        return descriptor;
    }

}
