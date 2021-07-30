package org.broadband_forum.obbaa.dmyang.entities;

import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NETWORK_MANAGER;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.NS;
import static org.broadband_forum.obbaa.dmyang.entities.DeviceManagerNSConstants.REVISION;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

@Entity
@YangContainer(name = NETWORK_MANAGER, namespace = NS , revision = REVISION)
public class NetworkManager {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        NetworkManager that = (NetworkManager) other;

        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) {
            return false;
        }
        return schemaPath != null ? schemaPath.equals(that.schemaPath) : that.schemaPath == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (schemaPath != null ? schemaPath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetworkManager{");
        sb.append("parentId='").append(parentId).append('\'');
        sb.append(", schemaPath='").append(schemaPath).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
