package com.anupam.workflow.platformsample;

import java.util.*;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.anupam.workflow.platformsample.actions.AutoArchiveAction;

import lombok.Setter;

public class TopUploadersWebScript extends DeclarativeWebScript {
      private static final Log logger = LogFactory.getLog(AutoArchiveAction.class);
    @Setter
    private SearchService searchService;

    @Setter
    private SiteService siteService;

    @Setter
    private NodeService nodeService;

    /**
     * @param req
     * @param status
     * @return
     */
    @Override
    protected Map<String,Object> executeImpl(WebScriptRequest req, Status status)
    {   logger.info("this function was called");

        String siteId=req.getServiceMatch().getTemplateVars().get("siteId");
        Map<String,Object>model=new HashMap<>();
        Map<String,Integer>userCount=new HashMap<>();

        try{

            NodeRef docLib=siteService.getContainer(siteId, "documentLibrary");
            if(docLib==null)
            {
                status.setCode(Status.STATUS_NOT_FOUND);
                model.put("error","Site not found");
                return model;
            }
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -30);
            Date fromDate = cal.getTime();

            String isoDate = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(fromDate);

            String query = "PATH:\"/app:company_home/st:sites/cm:" + siteId + "/cm:documentLibrary//*\" "
             + "AND cm:created:[\"" + isoDate + "\" TO NOW]";
            SearchParameters sp=new SearchParameters();
            sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
            sp.setQuery(query);

            ResultSet rs= searchService.query(sp);
            logger.error(rs.length());
            for(ResultSetRow row:rs){
                NodeRef node=row.getNodeRef();
                String creator=(String)nodeService.getProperty(
                    node,QName.createQName("{http://www.alfresco.org/model/content/1.0}creator")
                );
                userCount.put(creator, userCount.getOrDefault(creator, 0)+1);

            }
            rs.close();

            List<Map.Entry<String,Integer>> sorted=new ArrayList<>(userCount.entrySet());
            sorted.sort((a,b)->b.getValue().compareTo(a.getValue()));
            List<Map<String, Object>> topUsers = new ArrayList<>();

            for(int i=0;i<Math.min(5,sorted.size());i++)
            {
                Map<String, Object>entry=new HashMap<>();
                entry.put("user", sorted.get(i).getKey());
                entry.put("count", sorted.get(i).getValue());
                topUsers.add(entry);
            }
              model.put("site", siteId);
            model.put("topUploaders", topUsers);
        }
        catch(Exception e)
        {   
            status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
            model.put("error", e.getMessage());
        }
        return model;
    }
}
