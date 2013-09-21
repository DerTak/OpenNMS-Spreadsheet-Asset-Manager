/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.forge.spreadsheetassetmanager.utils;

import java.util.ArrayList;
import java.util.List;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;

/**
 * @author Markus@OpenNMS.com
 */
public class NodeToAssetMapping {
    String nodeLabel;
    List<RequisitionAsset> addAssets = new ArrayList<RequisitionAsset>();
    List<RequisitionAsset> removeAssets = new ArrayList<RequisitionAsset>();

    public NodeToAssetMapping(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public NodeToAssetMapping(String nodeLabel, List<RequisitionAsset> addAssets, List<RequisitionAsset> removeAssets) {
        this.nodeLabel = nodeLabel;
        this.addAssets = addAssets;
        this.removeAssets = removeAssets;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public List<RequisitionAsset> getAddAssets() {
        return addAssets;
    }

    public void setAddCategories(List<RequisitionAsset> addAssets) {
        this.addAssets = addAssets;
    }

    public List<RequisitionAsset> getRemoveAssets() {
        return removeAssets;
    }

    public void setRemoveCategories(List<RequisitionAsset> removeAssets) {
        this.removeAssets = removeAssets;
    }

    @Override
    public String toString() {
        return "NodeToAssetMapping{" + "nodeLabel=" + nodeLabel + ", addAssets=" + addAssets + ", removeAssets=" + removeAssets + '}';
    }
}
