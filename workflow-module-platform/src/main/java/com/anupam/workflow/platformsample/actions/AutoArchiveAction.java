package com.anupam.workflow.platformsample.actions;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AutoArchiveAction extends ActionExecuterAbstractBase {

    public static final String NAME = "auto-archiver";

    private static final Log logger = LogFactory.getLog(AutoArchiveAction.class);
    
    @Autowired
    private SearchService searchService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private FileFolderService fileFolderService;

    @Autowired
    private SiteService siteService;
    @Autowired
    private NamespaceService namespaceService;
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 2);
        Date oneYearAgo = cal.getTime();
        
        String query = "PATH:\""
    + nodeService.getPath(actionedUponNodeRef).toPrefixString(namespaceService)
    + "/*\"";
    
        ResultSet resultSet = null;

        try {
            resultSet = searchService.query(
                    actionedUponNodeRef.getStoreRef(),
                    SearchService.LANGUAGE_FTS_ALFRESCO,
                    query
            );

            for (NodeRef child : resultSet.getNodeRefs()) {
                if (nodeService.exists(child)
                        && ContentModel.TYPE_CONTENT.equals(nodeService.getType(child))) {

                    Date createdDate = (Date) nodeService.getProperty(child, ContentModel.PROP_CREATED);

                    if (createdDate != null && createdDate.before(oneYearAgo)) {
                        moveToArchive(child);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error while executing auto archive action", e);
        } finally {
            if (resultSet != null) {
                resultSet.close();
                logger.info("this part of code was executed succesfully");
            }
        }
    // logger.error("=== COUNT ACTION TRIGGERED ===");

    // int count = 0;

    // // Get all children of the node (folder)
    // List<ChildAssociationRef> children = nodeService.getChildAssocs(actionedUponNodeRef);

    // for (ChildAssociationRef childAssoc : children) {
    //     NodeRef child = childAssoc.getChildRef();

    //     // Check if it's a file (cm:content)
    //     if (nodeService.getType(child).equals(ContentModel.TYPE_CONTENT)) {
    //         count++;
    //     }
    // }

    // logger.error("Total files in folder: " + count);

    }

    private void moveToArchive(NodeRef file) throws FileExistsException, FileNotFoundException {

         logger.error("=== COUNT ACTION TRIGGERED ===");
        NodeRef archiveLib = siteService.getContainer("archive-site", "documentLibrary");

        if (archiveLib == null) {
            throw new IllegalStateException("Archive site documentLibrary not found");
        }

        NodeRef targetRoot = archiveLib;
        NodeRef sourceFolder = nodeService.getPrimaryParent(file).getParentRef();
        SiteInfo site = siteService.getSite(file);
        NodeRef sourceRoot=siteService.getContainer(site.getShortName(), "documentLibrary");
        
       
        // NodeRef fileParent = nodeService.getPrimaryParent(file).getParentRef();

        // if (!fileParent.equals(sourceFolder)) {
            NodeRef targetParent = createFolderStructure(sourceFolder,sourceRoot , archiveLib);
        // }

        fileFolderService.move(file, targetParent, null);
        logger.info("Moved file " + file + " to archive location");
          //Get archive site documentLibrary



    
    NodeRef archiveLib1 = siteService.getContainer("archive-site", "documentLibrary");

    if (archiveLib1 == null) {
        throw new IllegalStateException("Archive site documentLibrary not found");
    }

    // Get file name
    String fileName = (String) nodeService.getProperty(file, ContentModel.PROP_NAME);

    // Move file directly into archive root
    fileFolderService.move(file, archiveLib1, fileName);

    logger.info("Moved file " + fileName + " to archive documentLibrary");
    }

    private NodeRef createFolderStructure(NodeRef sourceRoot, NodeRef targetParent, NodeRef archiveRoot) {
        if (sourceRoot.equals(targetParent)) {
            return archiveRoot;
        }

        ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(targetParent);
        NodeRef parentNode = parentAssoc.getParentRef();

        NodeRef archiveParent = createFolderStructure(sourceRoot, parentNode, archiveRoot);

        String folderName = (String) nodeService.getProperty(targetParent, ContentModel.PROP_NAME);
        NodeRef existing = fileFolderService.searchSimple(archiveParent, folderName);

        if (existing != null) {
            return existing;
        }

        FileInfo createdFolder = fileFolderService.create(
                archiveParent,
                folderName,
                ContentModel.TYPE_FOLDER
        );

        return createdFolder.getNodeRef();
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        // No parameters required
    }
}