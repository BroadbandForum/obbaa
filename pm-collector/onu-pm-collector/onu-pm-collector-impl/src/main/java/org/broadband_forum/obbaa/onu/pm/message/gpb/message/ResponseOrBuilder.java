// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tr451_vomci_nbi_message.proto

package org.broadband_forum.obbaa.onu.pm.message.gpb.message;

public interface ResponseOrBuilder extends
        // @@protoc_insertion_point(interface_extends:tr451_vomci_nbi_message.v1.Response)
        com.google.protobuf.MessageOrBuilder {

    /**
     * <code>.tr451_vomci_nbi_message.v1.HelloResp hello_resp = 3;</code>
     *
     * @return Whether the helloResp field is set.
     */
    boolean hasHelloResp();

    /**
     * <code>.tr451_vomci_nbi_message.v1.HelloResp hello_resp = 3;</code>
     *
     * @return The helloResp.
     */
    org.broadband_forum.obbaa.onu.pm.message.gpb.message.HelloResp getHelloResp();

    /**
     * <code>.tr451_vomci_nbi_message.v1.HelloResp hello_resp = 3;</code>
     */
    org.broadband_forum.obbaa.onu.pm.message.gpb.message.HelloRespOrBuilder getHelloRespOrBuilder();

    /**
     * <code>.tr451_vomci_nbi_message.v1.GetDataResp get_resp = 4;</code>
     *
     * @return Whether the getResp field is set.
     */
    boolean hasGetResp();

    /**
     * <code>.tr451_vomci_nbi_message.v1.GetDataResp get_resp = 4;</code>
     *
     * @return The getResp.
     */
    org.broadband_forum.obbaa.onu.pm.message.gpb.message.GetDataResp getGetResp();

    /**
     * <code>.tr451_vomci_nbi_message.v1.GetDataResp get_resp = 4;</code>
     */
    org.broadband_forum.obbaa.onu.pm.message.gpb.message.GetDataRespOrBuilder getGetRespOrBuilder();

    /**
     * <code>.tr451_vomci_nbi_message.v1.ReplaceConfigResp replace_config_resp = 5;</code>
     *
     * @return Whether the replaceConfigResp field is set.
     */
    boolean hasReplaceConfigResp();

    /**
     * <code>.tr451_vomci_nbi_message.v1.ReplaceConfigResp replace_config_resp = 5;</code>
     *
     * @return The replaceConfigResp.
     */
    org.broadband_forum.obbaa.onu.pm.message.gpb.message.ReplaceConfigResp getReplaceConfigResp();

    /**
     * <code>.tr451_vomci_nbi_message.v1.ReplaceConfigResp replace_config_resp = 5;</code>
     */
    org.broadband_forum.obbaa.onu.pm.message.gpb.message.ReplaceConfigRespOrBuilder getReplaceConfigRespOrBuilder();

    /**
     * <code>.tr451_vomci_nbi_message.v1.UpdateConfigResp update_config_resp = 6;</code>
     *
     * @return Whether the updateConfigResp field is set.
     */
    boolean hasUpdateConfigResp();

    /**
     * <code>.tr451_vomci_nbi_message.v1.UpdateConfigResp update_config_resp = 6;</code>
     *
     * @return The updateConfigResp.
     */
    org.broadband_forum.obbaa.onu.pm.message.gpb.message.UpdateConfigResp getUpdateConfigResp();

    /**
     * <code>.tr451_vomci_nbi_message.v1.UpdateConfigResp update_config_resp = 6;</code>
     */
    org.broadband_forum.obbaa.onu.pm.message.gpb.message.UpdateConfigRespOrBuilder getUpdateConfigRespOrBuilder();

    /**
     * <code>.tr451_vomci_nbi_message.v1.RPCResp rpc_resp = 7;</code>
     *
     * @return Whether the rpcResp field is set.
     */
    boolean hasRpcResp();

    /**
     * <code>.tr451_vomci_nbi_message.v1.RPCResp rpc_resp = 7;</code>
     *
     * @return The rpcResp.
     */
    org.broadband_forum.obbaa.onu.pm.message.gpb.message.RPCResp getRpcResp();

    /**
     * <code>.tr451_vomci_nbi_message.v1.RPCResp rpc_resp = 7;</code>
     */
    org.broadband_forum.obbaa.onu.pm.message.gpb.message.RPCRespOrBuilder getRpcRespOrBuilder();

    /**
     * <code>.tr451_vomci_nbi_message.v1.ActionResp action_resp = 8;</code>
     *
     * @return Whether the actionResp field is set.
     */
    boolean hasActionResp();

    /**
     * <code>.tr451_vomci_nbi_message.v1.ActionResp action_resp = 8;</code>
     *
     * @return The actionResp.
     */
    org.broadband_forum.obbaa.onu.pm.message.gpb.message.ActionResp getActionResp();

    /**
     * <code>.tr451_vomci_nbi_message.v1.ActionResp action_resp = 8;</code>
     */
    org.broadband_forum.obbaa.onu.pm.message.gpb.message.ActionRespOrBuilder getActionRespOrBuilder();

    org.broadband_forum.obbaa.onu.pm.message.gpb.message.Response.RespTypeCase getRespTypeCase();
}
