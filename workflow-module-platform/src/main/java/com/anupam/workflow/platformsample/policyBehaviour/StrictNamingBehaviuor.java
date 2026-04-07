package com.anupam.workflow.platformsample.policyBehaviour;

import java.util.Date;
import java.text.SimpleDateFormat;

import org.alfresco.model.ContentModel;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.method.P;

import lombok.Setter;
public class StrictNamingBehaviuor implements NodeServicePolicies.OnCreateNodePolicy,InitializingBean{
    @Setter
    private PolicyComponent policyComponent;
    @Setter
    private NodeService nodeService;
  
    @Setter
    private NamespaceService namespaceService;
    //private String targetPath="/app:company_home/st:sites/cm:anupam/cm:documentLibrary/cm:archiveFolder";

    @Override
    public void afterPropertiesSet() throws Exception {
    
        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT ,
            new JavaBehaviour(this,"onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT)
        );
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {
     
     NodeRef nodeRef=childAssocRef.getChildRef();
     if(!nodeService.exists(nodeRef))return;
     
     if(!isInTargetPath(nodeRef))return;
     
     String originalName=(String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
     if (originalName == null) return;
     if (originalName.matches("^\\d{4}-\\d{2}-.*"))return;
    String prefixString=new SimpleDateFormat("yyyy-MM").format(new Date());
    String newName=prefixString+"-"+originalName;
    nodeService.setProperty(nodeRef,ContentModel.PROP_NAME,newName);
    }

    private boolean isInTargetPath(NodeRef nodeRef) {
       
        Path path=nodeService.getPath(nodeRef);
         String fullPath = path.toPrefixString((NamespacePrefixResolver)namespaceService); 
         return fullPath.contains("anupam")&&fullPath.contains("archiveFolder"); 
    }

}
