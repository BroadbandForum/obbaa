// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tr451_vomci_nbi_message.proto

package org.broadband_forum.obbaa.onu.pm.message.gpb.message;

public interface ActionRespOrBuilder extends
        // @@protoc_insertion_point(interface_extends:tr451_vomci_nbi_message.v1.ActionResp)
        com.google.protobuf.MessageOrBuilder {

    /**
     * <code>.tr451_vomci_nbi_message.v1.Status status_resp = 1;</code>
     *
     * @return Whether the statusResp field is set.
     */
    boolean hasStatusResp();

    /**
     * <code>.tr451_vomci_nbi_message.v1.Status status_resp = 1;</code>
     *
     * @return The statusResp.
     */
    org.broadband_forum.obbaa.onu.pm.message.gpb.message.Status getStatusResp();

    /**
     * <code>.tr451_vomci_nbi_message.v1.Status status_resp = 1;</code>
     */
    org.broadband_forum.obbaa.onu.pm.message.gpb.message.StatusOrBuilder getStatusRespOrBuilder();

    /**
     * <code>bytes output_data = 2;</code>
     *
     * @return The outputData.
     */
    com.google.protobuf.ByteString getOutputData();
}
