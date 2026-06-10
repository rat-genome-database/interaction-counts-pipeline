package edu.mcw.rgd.pipelines.interactionCounts;

import edu.mcw.rgd.dao.impl.InteractionCountsDAO;
import edu.mcw.rgd.dao.impl.InteractionsDAO;
import edu.mcw.rgd.dao.impl.RGDManagementDAO;
import edu.mcw.rgd.dao.spring.IntListQuery;
import edu.mcw.rgd.datamodel.Interaction;
import edu.mcw.rgd.datamodel.InteractionCount;
import edu.mcw.rgd.datamodel.RgdId;
import edu.mcw.rgd.process.Utils;

import java.util.ArrayList;
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
     * delete interaction_counts rows whose rgd_id is no longer in the set of non-zero counts
     * produced by this run -- i.e. counts that dropped to zero, or objects no longer active
     * @param keptRgdIds rgd_ids that still have a non-zero interaction count and must be kept
     * @return count of deleted rows
     */
    public int deleteStaleCounts(Set<Integer> keptRgdIds) throws Exception {
        List<Integer> staleIds = new ArrayList<>();
        for( int rgdId: IntListQuery.execute(countsDAO, "SELECT rgd_id FROM interaction_counts") ) {
            if( !keptRgdIds.contains(rgdId) ) {
                staleIds.add(rgdId);
            }
        }
        // Oracle limits an IN list to 1000 items, so delete in chunks
        for( int i=0; i<staleIds.size(); i+=1000 ) {
            List<Integer> chunk = staleIds.subList(i, Math.min(i+1000, staleIds.size()));
            countsDAO.update("DELETE FROM interaction_counts WHERE rgd_id IN (" + Utils.concatenate(chunk, ",") + ")");
        }
        return staleIds.size();
    }
}
