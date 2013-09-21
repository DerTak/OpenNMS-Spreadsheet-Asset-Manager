/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.forge.spreadsheetassetmanager;

import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableColumn;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;

/**
 * <p>SpreadsheetAssetReader class.</p>
 * <p/>
 * Create a ODS spreadsheet from an OpenNMS.
 *
 * @author <a href="mailto:markus@opennms.org">Markus Neumann</a>
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 * @version 1.0-SNAPSHOT
 * @since 1.0-SNAPSHOT
 */
public class SpreadsheetAssetWriter {

    /**
     * Logging
     */
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(SpreadsheetAssetReader.class);

    /**
     * Tag used as surveillance category prefix and spreadsheet table suffix
     */
    private static final String ASSET_TAG = "Asset";

    /**
     * Identifier for spreadsheet table for surveillance categories
     */
    private static final String SURVEILLANCE_CATEGORIES = "CATEGORIES";

    /**
     * Extension for the default output ODS file to java.io.tmpdir
     */
    private static final String ODS_EXTENSION = ".ods";

    /**
     * Name of the predefined default template for the spreadsheet
     */
    private static final String DEFAUlT_ODS_TEMPLATE = "template.ods";

    /**
     * Character to mark if a node is not assigned to this surveillance category
     */
    private static final String MARK_EMPTY_ASSET = "";

    /**
     * Concatenate tags and surveillance category names
     */
    private static final String CONCATENATE_TAG = "-";

    /**
     * <p>getSpreadsheetFromRequisition</p>
     * <p/>
     * Build spreadsheet file from a give OpenNMS requisition.
     *
     * @param requisition Requisition for generating the spreadsheet {@link org.opennms.netmgt.provision.persist.requisition.Requisition}
     * @param templateOds Ods template file to use for output, pass null to use default template.
     * @return ODS file with exported data from OpenNMS as {@link java.io.File}
     */
    public File getSpreadsheetFromRequisition(Requisition requisition, File templateOds) {
        String outputFilename = System.getProperty("java.io.tmpdir") + File.separator + requisition.getForeignSource() + ODS_EXTENSION;
        File odsOutput = getSpreadsheetFromRequisition(requisition, outputFilename, templateOds);
        return odsOutput;
    }

    /**
     * <p>getSpreadsheetFromRequisition</p>
     * <p/>
     * Build spreadsheet file from a give OpenNMS requisition.
     *
     * @param requisition    Requisition for generating the spreadsheet {@link org.opennms.netmgt.provision.persist.requisition.Requisition}
     * @param outputFilename User defined path and file name for output as {@link java.lang.String}
     * @param templateOds Ods template file to use for output, pass null to use default template.
     * @return ODS file with exported data from OpenNMS as {@link java.io.File}
     */
    public File getSpreadsheetFromRequisition(Requisition requisition, String outputFilename, File templateOds) {
        if (requisition == null) {
            logger.error("Requisition was null");
            return null;
        }
        InputStream template = null;
        if (templateOds != null) {
            try {
                template = new FileInputStream(templateOds);
            } catch (FileNotFoundException ex) {
                logger.error("Reading TemplateFile '{}' went wrong.", templateOds, ex);
                logger.error("Fallback to default template");
                template = this.getClass().getClassLoader().getResourceAsStream(DEFAUlT_ODS_TEMPLATE);
            }
        } else {
            template = this.getClass().getClassLoader().getResourceAsStream(DEFAUlT_ODS_TEMPLATE);
        }

        File odsOutFile = new File(outputFilename);
        try {
            OdfSpreadsheetDocument spreadsheet = OdfSpreadsheetDocument.loadDocument(template);
            OdfTable assetTable = spreadsheet.getTableList().get(0);
            assetTable.setTableName(requisition.getForeignSource() + " " + ASSET_TAG);

            Map<String, RequisitionNode> reqNodes = new TreeMap<String, RequisitionNode>();
            Set<String> assets = new TreeSet<String>();

            //load preset assets from template and add to assets lists
            Integer assetCellIndex = 1;
            while (!assetTable.getRowByIndex(0).getCellByIndex(assetCellIndex).getDisplayText().isEmpty()) {
                assets.add(assetTable.getRowByIndex(0).getCellByIndex(assetCellIndex).getDisplayText().trim());
                assetCellIndex++;
            }
            
            for (RequisitionNode reqNode : requisition.getNodes()) {
                reqNodes.put(reqNode.getNodeLabel(), reqNode);
                for (RequisitionAsset reqAsset : reqNode.getAssets()) {
                    assets.add(reqAsset.getName());
                }
            }

            writeAssetsIntoSheet(assetTable.getRowByIndex(0), assets, requisition.getForeignSource());
            writeNodesIntoSheet(assetTable, reqNodes, assets);

            spreadsheet.save(odsOutFile);
            logger.info("saved '{}'", odsOutFile);

        } catch (Exception ex) {
            logger.error("Building Spreadsheet went wrong", ex);
        }
        return odsOutFile;
    }

    /**
     * Fill first row with assets.
     *
     * @param assetRow   Row of the table for surveillance category names as {@link org.odftoolkit.odfdom.doc.table.OdfTableRow}
     * @param assets    Name of the assets {@link java.util.Set}
     * @param foreignSource Foreign source for the 0:0 cell as {@link java.lang.String}
     */
    private void writeAssetsIntoSheet(OdfTableRow assetRow, Set<String> assets, String foreignSource) {
        // Set foreign source name in cell 0:0
        assetRow.getCellByIndex(0).setDisplayText(foreignSource);

        // Start filling every row with a surveillance category name
        int assetCellIndex = 1;
        for (String asset : assets) {
            assetRow.getCellByIndex(assetCellIndex).setDisplayText(asset);
            assetCellIndex++;
        }
    }

    /**
     * <p>writeNodesIntoSheet</p>
     * <p/>
     * Fill first column with node labels. To create the nodes and assign all assets we need the whole table and not just a column
     *
     * @param table      Table which has to be filled as {@link org.odftoolkit.odfdom.doc.table.OdfTable}
     * @param reqNodes   Tree map with node label as key and the requisition node object as {@link java.util.Map}
     * @param assets Set of assets which has to be set for the node as {@link java.lang.String}
     */
    private void writeNodesIntoSheet(OdfTable table, Map<String, RequisitionNode> reqNodes, Set<String> assets) {
        OdfTableColumn nodeColumn = table.getColumnByIndex(0);
        OdfTableRow assetRow = table.getRowByIndex(0);
        int nodeCellIndex = 1;
        for (RequisitionNode reqNode : reqNodes.values()) {
            nodeColumn.getCellByIndex(nodeCellIndex).setDisplayText(reqNode.getNodeLabel());
            if (assets.size() > 0) {
                for (int assetIndex = 1; assetIndex <= assets.size(); assetIndex++) {
                    if (reqNode.getAssets().contains(new RequisitionAsset(assetRow.getCellByIndex(assetIndex).getDisplayText(), ""))) {
                        Integer indexOfAsset = reqNode.getAssets().indexOf(new RequisitionAsset(assetRow.getCellByIndex(assetIndex).getDisplayText(), ""));
                        String assetValue = reqNode.getAssets().get(indexOfAsset).getValue();
                        table.getRowByIndex(nodeCellIndex).getCellByIndex(assetIndex).setDisplayText(assetValue);
                    } else {
                        table.getRowByIndex(nodeCellIndex).getCellByIndex(assetIndex).setDisplayText(MARK_EMPTY_ASSET);
                    }
                }
            }
            nodeCellIndex++;
        }
    }
}
