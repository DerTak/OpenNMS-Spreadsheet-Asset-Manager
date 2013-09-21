/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with OpenNMS(R). If not, see:
 * http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org> http://www.opennms.org/ http://www.opennms.com/
 * *****************************************************************************
 */
package org.opennms.forge.spreadsheetassetmanager;

import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableColumn;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;
import org.opennms.forge.spreadsheetassetmanager.utils.NodeToAssetMapping;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;

/**
 * <p>SpreadsheetAssetReader class.</p>
 *
 * @author <a href="mailto:markus@opennms.org">Markus Neumann</a>
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 * @version 1.0-SNAPSHOT
 * @since 1.0-SNAPSHOT
 */
public class SpreadsheetAssetReader {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(SpreadsheetAssetReader.class);

    /**
     * Spreadsheet with nodes and category mapping
     */
    private final File m_odsFile;

    public SpreadsheetAssetReader(File odsFile) throws IOException {
        this.m_odsFile = odsFile;

        if (!(this.m_odsFile.exists() && this.m_odsFile.canRead())) {
            // The file does not exist and is not readable
            logger.error("The file '{}' doesn't exist or is not readable.", this.m_odsFile.getName());
            throw new IOException("File " + this.m_odsFile.getName() + " isn't readable or doesn't exist");
        }
    }

    public Collection<NodeToAssetMapping> getNodeToAssetMappingsFromFile() {
        Map<String, NodeToAssetMapping> nodesToAssets = new HashMap<String, NodeToAssetMapping>();

        try {
            OdfSpreadsheetDocument spreadsheet = OdfSpreadsheetDocument.loadDocument(this.m_odsFile);

            for (OdfTable table : spreadsheet.getTableList()) {
                nodesToAssets = getNodeToAssetMappingsFromTable(nodesToAssets, table);
            }

        } catch (Exception ex) {
            logger.error("Reading spreadsheet went wrong", ex);
        }

        return nodesToAssets.values();
    }

    private Map<String, NodeToAssetMapping> getNodeToAssetMappingsFromTable(Map<String, NodeToAssetMapping> nodesToAssets, OdfTable table) {
        logger.info("Reading Nodes and Categories from '{}'", table.getTableName());
        OdfTableColumn nodeColumn = table.getColumnByIndex(0);
        OdfTableRow categoryRow = table.getRowByIndex(0);
        NodeToAssetMapping nodeToAssetMapping;

        //Build a list of all Categories
        List<String> assetNames = new LinkedList<String>();
        int assetIndex = 1;
        while (!categoryRow.getCellByIndex(assetIndex).getDisplayText().equals("")) {
            assetNames.add(categoryRow.getCellByIndex(assetIndex).getDisplayText().trim());
            assetIndex++;
        }

        //Build a list of all Nodes with AddAssets and RemoveAssets
        int rowIndex = 1;
        while (!nodeColumn.getCellByIndex(rowIndex).getDisplayText().equals("")) {
            //Use already existing nodeToCategoryMapping objects if possible
            String nodeLabel = nodeColumn.getCellByIndex(rowIndex).getDisplayText().trim();
            if (nodesToAssets.containsKey(nodeLabel)) {
                nodeToAssetMapping = nodesToAssets.get(nodeLabel);
            } else {
                nodeToAssetMapping = new NodeToAssetMapping(nodeLabel);
                nodesToAssets.put(nodeLabel, nodeToAssetMapping);
            }
            
            //TODO change from categories to assets... so there are values now....
            for (int cellId = 1; cellId <= assetNames.size(); cellId++) {
                if (table.getRowByIndex(rowIndex).getCellByIndex(cellId).getDisplayText().equals("")) {
                    nodeToAssetMapping.getRemoveAssets().add(new RequisitionAsset(assetNames.get(cellId - 1).trim(), ""));
                    logger.debug("Node '{}' found removeAsset '{}'", nodeToAssetMapping.getNodeLabel(), assetNames.get(cellId - 1));
                } else {
                    String assetValue = table.getRowByIndex(rowIndex).getCellByIndex(cellId).getDisplayText().trim();
                    nodeToAssetMapping.getAddAssets().add(new RequisitionAsset(assetNames.get(cellId - 1).trim(), assetValue));
                    logger.debug("Node '{}' found addAsset    '{}' with '{}'", nodeToAssetMapping.getNodeLabel(), assetNames.get(cellId - 1), assetValue);
                }
            }
            rowIndex++;
        }
        return nodesToAssets;
    }
}