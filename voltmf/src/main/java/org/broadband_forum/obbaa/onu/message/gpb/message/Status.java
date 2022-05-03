// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tr451_vomci_nbi_message.proto

package org.broadband_forum.obbaa.onu.message.gpb.message;

/**
 * Protobuf type {@code tr451_vomci_nbi_message.v1.Status}
 */
public  final class Status extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:tr451_vomci_nbi_message.v1.Status)
    StatusOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Status.newBuilder() to construct.
  private Status(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Status() {
    statusCode_ = 0;
    error_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Status();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private Status(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    int mutable_bitField0_ = 0;
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
          case 8: {
            int rawValue = input.readEnum();

            statusCode_ = rawValue;
            break;
          }
          case 18: {
            if (!((mutable_bitField0_ & 0x00000001) != 0)) {
              error_ = new java.util.ArrayList<org.broadband_forum.obbaa.onu.message.gpb.message.Error>();
              mutable_bitField0_ |= 0x00000001;
            }
            error_.add(
                input.readMessage(org.broadband_forum.obbaa.onu.message.gpb.message.Error.parser(), extensionRegistry));
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
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
      if (((mutable_bitField0_ & 0x00000001) != 0)) {
        error_ = java.util.Collections.unmodifiableList(error_);
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return org.broadband_forum.obbaa.onu.message.gpb.message.Tr451VomciNbiMessage.internal_static_tr451_vomci_nbi_message_v1_Status_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.broadband_forum.obbaa.onu.message.gpb.message.Tr451VomciNbiMessage.internal_static_tr451_vomci_nbi_message_v1_Status_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.broadband_forum.obbaa.onu.message.gpb.message.Status.class, org.broadband_forum.obbaa.onu.message.gpb.message.Status.Builder.class);
  }

  /**
   * Protobuf enum {@code tr451_vomci_nbi_message.v1.Status.StatusCode}
   */
  public enum StatusCode
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <code>OK = 0;</code>
     */
    OK(0),
    /**
     * <code>ERROR_GENERAL = 1;</code>
     */
    ERROR_GENERAL(1),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>OK = 0;</code>
     */
    public static final int OK_VALUE = 0;
    /**
     * <code>ERROR_GENERAL = 1;</code>
     */
    public static final int ERROR_GENERAL_VALUE = 1;


    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalArgumentException(
            "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static StatusCode valueOf(int value) {
      return forNumber(value);
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     */
    public static StatusCode forNumber(int value) {
      switch (value) {
        case 0: return OK;
        case 1: return ERROR_GENERAL;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<StatusCode>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        StatusCode> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<StatusCode>() {
            public StatusCode findValueByNumber(int number) {
              return StatusCode.forNumber(number);
            }
          };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return org.broadband_forum.obbaa.onu.message.gpb.message.Status.getDescriptor().getEnumTypes().get(0);
    }

    private static final StatusCode[] VALUES = values();

    public static StatusCode valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      if (desc.getIndex() == -1) {
        return UNRECOGNIZED;
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private StatusCode(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:tr451_vomci_nbi_message.v1.Status.StatusCode)
  }

  public static final int STATUS_CODE_FIELD_NUMBER = 1;
  private int statusCode_;
  /**
   * <code>.tr451_vomci_nbi_message.v1.Status.StatusCode status_code = 1;</code>
   * @return The enum numeric value on the wire for statusCode.
   */
  public int getStatusCodeValue() {
    return statusCode_;
  }
  /**
   * <code>.tr451_vomci_nbi_message.v1.Status.StatusCode status_code = 1;</code>
   * @return The statusCode.
   */
  public org.broadband_forum.obbaa.onu.message.gpb.message.Status.StatusCode getStatusCode() {
    @SuppressWarnings("deprecation")
    org.broadband_forum.obbaa.onu.message.gpb.message.Status.StatusCode result = org.broadband_forum.obbaa.onu.message.gpb.message.Status.StatusCode.valueOf(statusCode_);
    return result == null ? org.broadband_forum.obbaa.onu.message.gpb.message.Status.StatusCode.UNRECOGNIZED : result;
  }

  public static final int ERROR_FIELD_NUMBER = 2;
  private java.util.List<org.broadband_forum.obbaa.onu.message.gpb.message.Error> error_;
  /**
   * <pre>
   *Optional: Error information
   * </pre>
   *
   * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
   */
  public java.util.List<org.broadband_forum.obbaa.onu.message.gpb.message.Error> getErrorList() {
    return error_;
  }
  /**
   * <pre>
   *Optional: Error information
   * </pre>
   *
   * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
   */
  public java.util.List<? extends org.broadband_forum.obbaa.onu.message.gpb.message.ErrorOrBuilder> 
      getErrorOrBuilderList() {
    return error_;
  }
  /**
   * <pre>
   *Optional: Error information
   * </pre>
   *
   * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
   */
  public int getErrorCount() {
    return error_.size();
  }
  /**
   * <pre>
   *Optional: Error information
   * </pre>
   *
   * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
   */
  public org.broadband_forum.obbaa.onu.message.gpb.message.Error getError(int index) {
    return error_.get(index);
  }
  /**
   * <pre>
   *Optional: Error information
   * </pre>
   *
   * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
   */
  public org.broadband_forum.obbaa.onu.message.gpb.message.ErrorOrBuilder getErrorOrBuilder(
      int index) {
    return error_.get(index);
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (statusCode_ != org.broadband_forum.obbaa.onu.message.gpb.message.Status.StatusCode.OK.getNumber()) {
      output.writeEnum(1, statusCode_);
    }
    for (int i = 0; i < error_.size(); i++) {
      output.writeMessage(2, error_.get(i));
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (statusCode_ != org.broadband_forum.obbaa.onu.message.gpb.message.Status.StatusCode.OK.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(1, statusCode_);
    }
    for (int i = 0; i < error_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, error_.get(i));
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof org.broadband_forum.obbaa.onu.message.gpb.message.Status)) {
      return super.equals(obj);
    }
    org.broadband_forum.obbaa.onu.message.gpb.message.Status other = (org.broadband_forum.obbaa.onu.message.gpb.message.Status) obj;

    if (statusCode_ != other.statusCode_) return false;
    if (!getErrorList()
        .equals(other.getErrorList())) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + STATUS_CODE_FIELD_NUMBER;
    hash = (53 * hash) + statusCode_;
    if (getErrorCount() > 0) {
      hash = (37 * hash) + ERROR_FIELD_NUMBER;
      hash = (53 * hash) + getErrorList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.broadband_forum.obbaa.onu.message.gpb.message.Status parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Status parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Status parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Status parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Status parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Status parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Status parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Status parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Status parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Status parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Status parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Status parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(org.broadband_forum.obbaa.onu.message.gpb.message.Status prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code tr451_vomci_nbi_message.v1.Status}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:tr451_vomci_nbi_message.v1.Status)
      org.broadband_forum.obbaa.onu.message.gpb.message.StatusOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.broadband_forum.obbaa.onu.message.gpb.message.Tr451VomciNbiMessage.internal_static_tr451_vomci_nbi_message_v1_Status_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.broadband_forum.obbaa.onu.message.gpb.message.Tr451VomciNbiMessage.internal_static_tr451_vomci_nbi_message_v1_Status_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.broadband_forum.obbaa.onu.message.gpb.message.Status.class, org.broadband_forum.obbaa.onu.message.gpb.message.Status.Builder.class);
    }

    // Construct using org.broadband_forum.obbaa.onu.message.gpb.message.Status.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
        getErrorFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      statusCode_ = 0;

      if (errorBuilder_ == null) {
        error_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
      } else {
        errorBuilder_.clear();
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.broadband_forum.obbaa.onu.message.gpb.message.Tr451VomciNbiMessage.internal_static_tr451_vomci_nbi_message_v1_Status_descriptor;
    }

    @java.lang.Override
    public org.broadband_forum.obbaa.onu.message.gpb.message.Status getDefaultInstanceForType() {
      return org.broadband_forum.obbaa.onu.message.gpb.message.Status.getDefaultInstance();
    }

    @java.lang.Override
    public org.broadband_forum.obbaa.onu.message.gpb.message.Status build() {
      org.broadband_forum.obbaa.onu.message.gpb.message.Status result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.broadband_forum.obbaa.onu.message.gpb.message.Status buildPartial() {
      org.broadband_forum.obbaa.onu.message.gpb.message.Status result = new org.broadband_forum.obbaa.onu.message.gpb.message.Status(this);
      int from_bitField0_ = bitField0_;
      result.statusCode_ = statusCode_;
      if (errorBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          error_ = java.util.Collections.unmodifiableList(error_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.error_ = error_;
      } else {
        result.error_ = errorBuilder_.build();
      }
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof org.broadband_forum.obbaa.onu.message.gpb.message.Status) {
        return mergeFrom((org.broadband_forum.obbaa.onu.message.gpb.message.Status)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.broadband_forum.obbaa.onu.message.gpb.message.Status other) {
      if (other == org.broadband_forum.obbaa.onu.message.gpb.message.Status.getDefaultInstance()) return this;
      if (other.statusCode_ != 0) {
        setStatusCodeValue(other.getStatusCodeValue());
      }
      if (errorBuilder_ == null) {
        if (!other.error_.isEmpty()) {
          if (error_.isEmpty()) {
            error_ = other.error_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureErrorIsMutable();
            error_.addAll(other.error_);
          }
          onChanged();
        }
      } else {
        if (!other.error_.isEmpty()) {
          if (errorBuilder_.isEmpty()) {
            errorBuilder_.dispose();
            errorBuilder_ = null;
            error_ = other.error_;
            bitField0_ = (bitField0_ & ~0x00000001);
            errorBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getErrorFieldBuilder() : null;
          } else {
            errorBuilder_.addAllMessages(other.error_);
          }
        }
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      org.broadband_forum.obbaa.onu.message.gpb.message.Status parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (org.broadband_forum.obbaa.onu.message.gpb.message.Status) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private int statusCode_ = 0;
    /**
     * <code>.tr451_vomci_nbi_message.v1.Status.StatusCode status_code = 1;</code>
     * @return The enum numeric value on the wire for statusCode.
     */
    public int getStatusCodeValue() {
      return statusCode_;
    }
    /**
     * <code>.tr451_vomci_nbi_message.v1.Status.StatusCode status_code = 1;</code>
     * @param value The enum numeric value on the wire for statusCode to set.
     * @return This builder for chaining.
     */
    public Builder setStatusCodeValue(int value) {
      statusCode_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>.tr451_vomci_nbi_message.v1.Status.StatusCode status_code = 1;</code>
     * @return The statusCode.
     */
    public org.broadband_forum.obbaa.onu.message.gpb.message.Status.StatusCode getStatusCode() {
      @SuppressWarnings("deprecation")
      org.broadband_forum.obbaa.onu.message.gpb.message.Status.StatusCode result = org.broadband_forum.obbaa.onu.message.gpb.message.Status.StatusCode.valueOf(statusCode_);
      return result == null ? org.broadband_forum.obbaa.onu.message.gpb.message.Status.StatusCode.UNRECOGNIZED : result;
    }
    /**
     * <code>.tr451_vomci_nbi_message.v1.Status.StatusCode status_code = 1;</code>
     * @param value The statusCode to set.
     * @return This builder for chaining.
     */
    public Builder setStatusCode(org.broadband_forum.obbaa.onu.message.gpb.message.Status.StatusCode value) {
      if (value == null) {
        throw new NullPointerException();
      }
      
      statusCode_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <code>.tr451_vomci_nbi_message.v1.Status.StatusCode status_code = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearStatusCode() {
      
      statusCode_ = 0;
      onChanged();
      return this;
    }

    private java.util.List<org.broadband_forum.obbaa.onu.message.gpb.message.Error> error_ =
      java.util.Collections.emptyList();
    private void ensureErrorIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        error_ = new java.util.ArrayList<org.broadband_forum.obbaa.onu.message.gpb.message.Error>(error_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        org.broadband_forum.obbaa.onu.message.gpb.message.Error, org.broadband_forum.obbaa.onu.message.gpb.message.Error.Builder, org.broadband_forum.obbaa.onu.message.gpb.message.ErrorOrBuilder> errorBuilder_;

    /**
     * <pre>
     *Optional: Error information
     * </pre>
     *
     * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
     */
    public java.util.List<org.broadband_forum.obbaa.onu.message.gpb.message.Error> getErrorList() {
      if (errorBuilder_ == null) {
        return java.util.Collections.unmodifiableList(error_);
      } else {
        return errorBuilder_.getMessageList();
      }
    }
    /**
     * <pre>
     *Optional: Error information
     * </pre>
     *
     * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
     */
    public int getErrorCount() {
      if (errorBuilder_ == null) {
        return error_.size();
      } else {
        return errorBuilder_.getCount();
      }
    }
    /**
     * <pre>
     *Optional: Error information
     * </pre>
     *
     * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
     */
    public org.broadband_forum.obbaa.onu.message.gpb.message.Error getError(int index) {
      if (errorBuilder_ == null) {
        return error_.get(index);
      } else {
        return errorBuilder_.getMessage(index);
      }
    }
    /**
     * <pre>
     *Optional: Error information
     * </pre>
     *
     * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
     */
    public Builder setError(
        int index, org.broadband_forum.obbaa.onu.message.gpb.message.Error value) {
      if (errorBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureErrorIsMutable();
        error_.set(index, value);
        onChanged();
      } else {
        errorBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <pre>
     *Optional: Error information
     * </pre>
     *
     * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
     */
    public Builder setError(
        int index, org.broadband_forum.obbaa.onu.message.gpb.message.Error.Builder builderForValue) {
      if (errorBuilder_ == null) {
        ensureErrorIsMutable();
        error_.set(index, builderForValue.build());
        onChanged();
      } else {
        errorBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     *Optional: Error information
     * </pre>
     *
     * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
     */
    public Builder addError(org.broadband_forum.obbaa.onu.message.gpb.message.Error value) {
      if (errorBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureErrorIsMutable();
        error_.add(value);
        onChanged();
      } else {
        errorBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <pre>
     *Optional: Error information
     * </pre>
     *
     * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
     */
    public Builder addError(
        int index, org.broadband_forum.obbaa.onu.message.gpb.message.Error value) {
      if (errorBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureErrorIsMutable();
        error_.add(index, value);
        onChanged();
      } else {
        errorBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <pre>
     *Optional: Error information
     * </pre>
     *
     * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
     */
    public Builder addError(
        org.broadband_forum.obbaa.onu.message.gpb.message.Error.Builder builderForValue) {
      if (errorBuilder_ == null) {
        ensureErrorIsMutable();
        error_.add(builderForValue.build());
        onChanged();
      } else {
        errorBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     *Optional: Error information
     * </pre>
     *
     * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
     */
    public Builder addError(
        int index, org.broadband_forum.obbaa.onu.message.gpb.message.Error.Builder builderForValue) {
      if (errorBuilder_ == null) {
        ensureErrorIsMutable();
        error_.add(index, builderForValue.build());
        onChanged();
      } else {
        errorBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     *Optional: Error information
     * </pre>
     *
     * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
     */
    public Builder addAllError(
        java.lang.Iterable<? extends org.broadband_forum.obbaa.onu.message.gpb.message.Error> values) {
      if (errorBuilder_ == null) {
        ensureErrorIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, error_);
        onChanged();
      } else {
        errorBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <pre>
     *Optional: Error information
     * </pre>
     *
     * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
     */
    public Builder clearError() {
      if (errorBuilder_ == null) {
        error_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        errorBuilder_.clear();
      }
      return this;
    }
    /**
     * <pre>
     *Optional: Error information
     * </pre>
     *
     * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
     */
    public Builder removeError(int index) {
      if (errorBuilder_ == null) {
        ensureErrorIsMutable();
        error_.remove(index);
        onChanged();
      } else {
        errorBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <pre>
     *Optional: Error information
     * </pre>
     *
     * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
     */
    public org.broadband_forum.obbaa.onu.message.gpb.message.Error.Builder getErrorBuilder(
        int index) {
      return getErrorFieldBuilder().getBuilder(index);
    }
    /**
     * <pre>
     *Optional: Error information
     * </pre>
     *
     * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
     */
    public org.broadband_forum.obbaa.onu.message.gpb.message.ErrorOrBuilder getErrorOrBuilder(
        int index) {
      if (errorBuilder_ == null) {
        return error_.get(index);  } else {
        return errorBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <pre>
     *Optional: Error information
     * </pre>
     *
     * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
     */
    public java.util.List<? extends org.broadband_forum.obbaa.onu.message.gpb.message.ErrorOrBuilder> 
         getErrorOrBuilderList() {
      if (errorBuilder_ != null) {
        return errorBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(error_);
      }
    }
    /**
     * <pre>
     *Optional: Error information
     * </pre>
     *
     * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
     */
    public org.broadband_forum.obbaa.onu.message.gpb.message.Error.Builder addErrorBuilder() {
      return getErrorFieldBuilder().addBuilder(
          org.broadband_forum.obbaa.onu.message.gpb.message.Error.getDefaultInstance());
    }
    /**
     * <pre>
     *Optional: Error information
     * </pre>
     *
     * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
     */
    public org.broadband_forum.obbaa.onu.message.gpb.message.Error.Builder addErrorBuilder(
        int index) {
      return getErrorFieldBuilder().addBuilder(
          index, org.broadband_forum.obbaa.onu.message.gpb.message.Error.getDefaultInstance());
    }
    /**
     * <pre>
     *Optional: Error information
     * </pre>
     *
     * <code>repeated .tr451_vomci_nbi_message.v1.Error error = 2;</code>
     */
    public java.util.List<org.broadband_forum.obbaa.onu.message.gpb.message.Error.Builder> 
         getErrorBuilderList() {
      return getErrorFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        org.broadband_forum.obbaa.onu.message.gpb.message.Error, org.broadband_forum.obbaa.onu.message.gpb.message.Error.Builder, org.broadband_forum.obbaa.onu.message.gpb.message.ErrorOrBuilder> 
        getErrorFieldBuilder() {
      if (errorBuilder_ == null) {
        errorBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            org.broadband_forum.obbaa.onu.message.gpb.message.Error, org.broadband_forum.obbaa.onu.message.gpb.message.Error.Builder, org.broadband_forum.obbaa.onu.message.gpb.message.ErrorOrBuilder>(
                error_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        error_ = null;
      }
      return errorBuilder_;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:tr451_vomci_nbi_message.v1.Status)
  }

  // @@protoc_insertion_point(class_scope:tr451_vomci_nbi_message.v1.Status)
  private static final org.broadband_forum.obbaa.onu.message.gpb.message.Status DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.broadband_forum.obbaa.onu.message.gpb.message.Status();
  }

  public static org.broadband_forum.obbaa.onu.message.gpb.message.Status getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Status>
      PARSER = new com.google.protobuf.AbstractParser<Status>() {
    @java.lang.Override
    public Status parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new Status(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Status> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Status> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.broadband_forum.obbaa.onu.message.gpb.message.Status getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

