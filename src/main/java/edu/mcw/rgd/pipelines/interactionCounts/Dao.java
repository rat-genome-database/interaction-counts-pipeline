package edu.mcw.rgd.pipelines.interactionCounts;

import edu.mcw.rgd.dao.impl.AssociationDAO;
import edu.mcw.rgd.dao.impl.InteractionCountsDAO;
import edu.mcw.rgd.dao.impl.InteractionsDAO;
import edu.mcw.rgd.dao.impl.RGDManagementDAO;
import edu.mcw.rgd.datamodel.Association;
import edu.mcw.rgd.datamodel.Interaction;
import edu.mcw.rgd.datamodel.InteractionCount;
import edu.mcw.rgd.datamodel.RgdId;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mtutaj on 1/31/2017.
 */
public class Dao {

    private AssociationDAO adao= new AssociationDAO();
    private InteractionsDAO idao= new InteractionsDAO();
    private InteractionCountsDAO countsDAO= new InteractionCountsDAO();
    private RGDManagementDAO rgdDAO = new RGDManagementDAO();

    public Collection<Integer> getActiveRgdIds(int objectKey) throws Exception {

        Set<Integer> activeRgdIds = new HashSet<>();
        for( RgdId id: rgdDAO.getRgdIds(objectKey) ) {
            if( id.getObjectStatus().equals("ACTIVE") ) {
                activeRgdIds.add(id.getRgdId());
            }
        }
        return activeRgdIds;
    }

    public Collection<Integer> getActiveGeneRgdIds() throws Exception {

        return getActiveRgdIds(RgdId.OBJECT_KEY_GENES);
    }

    public Collection<Integer> getActiveProteinRgdIds() throws Exception {

        return getActiveRgdIds(RgdId.OBJECT_KEY_PROTEINS);
    }

    public List<Association> getAssociationsByType(String assocType) throws Exception {
        return adao.getAssociationsByType(assocType);
    }

    public List<Interaction> getInteractions() throws Exception {
        return idao.getInteractions();
    }

    public int getInteractionCountByRgdIdsList(List<Integer> rgdIds) throws Exception {
        if( rgdIds==null || rgdIds.isEmpty() ) {
            return 0;
        }
        return idao.getInteractionCountByRgdIdsList(rgdIds);
    }

    // return: -1: inserted, 1-updated, 0-up-to-date
    public int upsertInteractionCount(InteractionCount ic) throws Exception {
        InteractionCount inRgd = countsDAO.getInteractionCount(ic.getRgdId());
        if( inRgd==null ) {
            return -countsDAO.insert(ic);
        }
        return countsDAO.update(ic);
    }
}
