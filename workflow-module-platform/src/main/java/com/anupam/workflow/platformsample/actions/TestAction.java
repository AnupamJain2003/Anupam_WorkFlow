package com.anupam.workflow.platformsample.actions;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;

public class TestAction extends ActionExecuterAbstractBase{
    
    public static final String NAME="test-action";

    private static final Log logger=LogFactory.getLog(TestAction.class);

    @Autowired
    private SearchService searchService;
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }
    @Autowired
    private NodeService nodeService;
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
    @Autowired
    private FileFolderService fileFolderService;
    public void setFileFolderService(FileFolderService fileFolderService)
    { this.fileFolderService=fileFolderService;}
    @Autowired
    private SiteService siteService;
    public void setSiteService(SiteService siteService)
    {this.siteService=siteService;}
    @Autowired
    private NamespaceService namespaceService;
    public void setNamespaceSevice(NamespaceService namespaceService)
    {this.namespaceService=namespaceService;}
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        // TODO Auto-generated method stub
         logger.error("=== COUNT ACTION TRIGGERED ===");

    int count = 0;

    // Get all children of the node (folder)
    List<ChildAssociationRef> children = nodeService.getChildAssocs(actionedUponNodeRef);

    for (ChildAssociationRef childAssoc : children) {
        NodeRef child = childAssoc.getChildRef();

        // Check if it's a file (cm:content)
        if (nodeService.getType(child).equals(ContentModel.TYPE_CONTENT)) {
            count++;
        }
    }

    logger.error("Total files in folder: " + count);
// 
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        // TODO Auto-generated method stub
        
    }

    


}
