package edu.mcw.rgd.pipelines.interactionCounts;

import edu.mcw.rgd.datamodel.*;
import edu.mcw.rgd.process.CounterPool;

import java.util.*;
import java.util.Map; // java.util.Map, not edu.mcw.rgd.datamodel.Map

/**
 * Created by jthota on 6/9/2016.
 */
public class Process {

    private Dao dao;

    public Map<Integer, Integer> getInteractionCountsOfGenes(Collection<Integer> geneRgdIds, List<Interaction> interactions) throws Exception{

        // protein rgd id -> rgd ids of genes encoding that protein
        Map<Integer, List<Integer>> proteinToGenes = new HashMap<>();
        for (Association a : getDao().getProteinToGeneAssociations()) {
            proteinToGenes.computeIfAbsent(a.getMasterRgdId(), k -> new ArrayList<>()).add(a.getDetailRgdId());
        }

        // every active gene gets an entry, so genes without interactions are counted as zero
        Map<Integer, Integer> geneIntCountsMap = new HashMap<>();
        for (int geneRgdId : geneRgdIds) {
            geneIntCountsMap.put(geneRgdId, 0);
        }

        // single pass over the interaction list: an interaction counts once for every gene
        // encoding either of its proteins, even if both proteins belong to the same gene
        Set<Integer> genes = new HashSet<>();
        for (Interaction i : interactions) {
            genes.clear();
            genes.addAll(proteinToGenes.getOrDefault(i.getRgdId1(), List.of()));
            genes.addAll(proteinToGenes.getOrDefault(i.getRgdId2(), List.of()));
            for (int geneRgdId : genes) {
                geneIntCountsMap.computeIfPresent(geneRgdId, (k, count) -> count + 1);
            }
        }
        return geneIntCountsMap;
    }

    public Map<Integer, Integer> getInteractionCountsOfProteins(Collection<Integer> proteinRgdIds, List<Interaction> interactions) throws Exception{
        // single pass over the interaction list (O(I)) instead of O(P * I): for each interaction,
        // bump the counter for either endpoint that is in the protein set; self-interactions count once
        Set<Integer> proteinSet = (proteinRgdIds instanceof Set) ? (Set<Integer>) proteinRgdIds : new HashSet<>(proteinRgdIds);
        Map<Integer, Integer> proteinIntCountMap = new HashMap<>();
        for (Interaction i : interactions) {
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

