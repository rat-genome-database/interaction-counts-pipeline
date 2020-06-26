package edu.mcw.rgd.pipelines.interactionCounts;

import edu.mcw.rgd.dao.impl.InteractionCountsDAO;
import edu.mcw.rgd.dao.impl.InteractionsDAO;
import edu.mcw.rgd.dao.impl.RGDManagementDAO;
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

    private InteractionsDAO idao= new InteractionsDAO();
    private InteractionCountsDAO countsDAO= new InteractionCountsDAO();
    private RGDManagementDAO rgdDAO = new RGDManagementDAO();

    public String getConnectionInfo() {
        return idao.getConnectionInfo();
    }

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

    public List<Interaction> getInteractions() throws Exception {
        return idao.getInteractions();
    }

    public int getInteractionCountByGeneRgdId(int geneRgdId) throws Exception {
        return idao.getInteractionCountByGeneRgdId(geneRgdId);
    }

    // return: -1: inserted, 1-updated, 0-up-to-date
    public int upsertInteractionCount(InteractionCount ic) throws Exception {
        InteractionCount inRgd = countsDAO.getInteractionCount(ic.getRgdId());
        if( inRgd==null ) {
            // insert only non-zero entries
            return -countsDAO.insert(ic);
        }
        return countsDAO.update(ic);
    }

    /**
     * delete interaction counts with value of 0 ('zero')
     * @return count of deleted interactions
     */
    public int deleteEntriesWithNoInteractions() throws Exception {
        String sql = "DELETE FROM interaction_counts WHERE interactions_count=0";
        return idao.update(sql);
    }
}
