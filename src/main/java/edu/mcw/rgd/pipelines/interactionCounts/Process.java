package edu.mcw.rgd.pipelines.interactionCounts;

import edu.mcw.rgd.datamodel.*;
import edu.mcw.rgd.process.CounterPool;
import edu.mcw.rgd.process.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jthota on 6/9/2016.
 */
public class Process {

    private Dao dao;
    Logger log = LogManager.getLogger("status");

    public Map<Integer, Integer> getInteractionCountsOfGenes(Collection<Integer> geneRgdIds) throws Exception{

        Map<Integer, Integer> geneIntCountsMap = new ConcurrentHashMap<>();

        // use JDK8 parallel streams to load interaction counts for genes
        geneRgdIds.parallelStream().forEach( geneRgdId -> {
            int count;
            try {
                count = getDao().getInteractionCountByGeneRgdId(geneRgdId);
            } catch(Exception e) {
                Utils.printStackTrace(e, log);
                throw new RuntimeException(e);
            }
            geneIntCountsMap.put(geneRgdId, count);
        });

        return geneIntCountsMap;
    }

    public Map<Integer, Integer> getInteractionCountsOfProteins(Collection<Integer> proteinRgdIds) throws Exception{
        // single pass over the interaction list (O(I)) instead of O(P * I): for each interaction,
        // bump the counter for either endpoint that is in the protein set; self-interactions count once
        Set<Integer> proteinSet = (proteinRgdIds instanceof Set) ? (Set<Integer>) proteinRgdIds : new HashSet<>(proteinRgdIds);
        Map<Integer, Integer> proteinIntCountMap = new HashMap<>();
        for (Interaction i : getDao().getInteractions()) {
            int r1 = i.getRgdId1();
            int r2 = i.getRgdId2();
            if (proteinSet.contains(r1)) {
                proteinIntCountMap.merge(r1, 1, Integer::sum);
            }
            if (r2 != r1 && proteinSet.contains(r2)) {
                proteinIntCountMap.merge(r2, 1, Integer::sum);
            }
        }
        return proteinIntCountMap;
    }

    /// @return: counts for 'inserted', 'updated', 'up-to-date'
    public CounterPool insertOrUpdate(Map<Integer, Integer> map) {
        CounterPool counters = new CounterPool();
        map.entrySet().forEach( entry -> {
            InteractionCount i= new InteractionCount();
            i.setRgdId(entry.getKey());
            i.setCount(entry.getValue());

            // do not process entries with zero interactions count
            if( i.getCount()==0 ) {
                counters.increment("zero");
                return;
            }

            int affectedRows;
            try {
                affectedRows = getDao().upsertInteractionCount(i);
            } catch( Exception e ) {
                throw new RuntimeException(e);
            }
            if( affectedRows<0 ) {
                counters.increment("inserted");
            } else if( affectedRows>0 ) {
                counters.increment("updated");
            } else {
                counters.increment("up-to-date");
            }
        });
        return counters;
    }

    public Dao getDao() {
        return dao;
    }

    public void setDao(Dao dao) {
        this.dao = dao;
    }
}

