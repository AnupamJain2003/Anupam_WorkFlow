package com.anupam.workflow.platformsample.actions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.alfresco.model.ContentModel;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RenameFilesAction extends ActionExecuterAbstractBase {
    private static final Log logger = LogFactory.getLog(AutoArchiveAction.class);
    
    private NodeService nodeService;
    private Random random= new Random();
    public void setNodeService(NodeService ns)
    {
        this.nodeService=ns;
    }
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        // TODO Auto-generated method stub
        if(!nodeService.getType(actionedUponNodeRef).equals(ContentModel.TYPE_FOLDER))
        {   logger.error("no folder exist");
            return;}
            List<ChildAssociationRef> children=nodeService.getChildAssocs(actionedUponNodeRef);
            String date=new SimpleDateFormat("yyyyMMdd").format(new Date());
            for(ChildAssociationRef child:children)
            {
                NodeRef childRef=child.getChildRef();
                if(nodeService.getType(childRef).equals(ContentModel.TYPE_CONTENT))
                {
                     String randomNumber = String.format("%05d", random.nextInt(100000));
                String newName = date + "_" + randomNumber;
                    nodeService.setProperty(childRef,ContentModel.PROP_NAME , newName);
                }
            }
    }
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        // TODO Auto-generated method stub
        
    }
    
}
