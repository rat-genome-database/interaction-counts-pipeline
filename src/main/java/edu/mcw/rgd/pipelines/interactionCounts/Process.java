package edu.mcw.rgd.pipelines.interactionCounts;

import edu.mcw.rgd.datamodel.*;

import java.util.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jthota on 6/9/2016.
 */
public class Process {

    private Dao dao;

    // build assoc map for: gene-rgd-id ==> list-of-protein-rgd-ids
    public Map<Integer, List<Integer>> getAssociationsMap(Collection<Integer> geneRgdIds) throws Exception{
        List<Association> assocs = getDao().getAssociationsByType("protein_to_gene");
        Map<Integer, List<Integer>> assocMap = new HashMap<>((geneRgdIds.size()*5)/4);

        // initialize map for every gene
        for( Integer geneRgdId: geneRgdIds ) {
            assocMap.put(geneRgdId, new ArrayList<Integer>());
        }

        for( Association a: assocs ){
            int rgdId = a.getDetailRgdId();
            if( geneRgdIds.contains(rgdId) ) {
                List<Integer> proteinRgdIds = assocMap.get(rgdId);
                proteinRgdIds.add(a.getMasterRgdId());
            }
        }
        return assocMap;
    }

    public Map<Integer, Integer> getInteractionCountsOfGenes(Collection<Integer> geneRgdIds) throws Exception{

        Map<Integer, Integer> geneIntCountsMap = new ConcurrentHashMap<>();
        Map<Integer, List<Integer>> associationsMap = this.getAssociationsMap(geneRgdIds);

        // use JDK8 parallel streams to load interaction counts for genes
        associationsMap.entrySet().parallelStream().forEach( (entry) -> {
            int geneRgdId = entry.getKey();
            List<Integer> associatedProteinRgdIds = entry.getValue();
            int count;
            try {
                count = getDao().getInteractionCountByRgdIdsList(associatedProteinRgdIds);
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
            geneIntCountsMap.put(geneRgdId, count);
        });

        return geneIntCountsMap;
    }

    public Map<Integer, Integer> getInteractionCountsOfProteins(Collection<Integer> proteinRgdIds) throws Exception{
        Map<Integer, Integer> proteinIntCountMap = new ConcurrentHashMap<>();
        List<Interaction> interactionList = getDao().getInteractions();
        List<Integer> listOfProteinRgdIds = new ArrayList<>(proteinRgdIds);

        listOfProteinRgdIds.parallelStream().forEach( (rgdId) -> {
            List<Interaction> interactions= new ArrayList<>();
            for(Interaction i: interactionList){
                if(i.getRgdId1()==rgdId || i.getRgdId2()==rgdId){
                    interactions.add(i);
                }
            }
            int count= interactions.size();
            proteinIntCountMap.put(rgdId, count);
        });

        return proteinIntCountMap;
    }

    // return: [0]-inserted, [1]-updated, [2]-up-to-date
    public int[] insertOrUpdate(Map<Integer, Integer> map) throws Exception{
        int[] insertOrUpdateCount=new int[3];
        for(Map.Entry<Integer,Integer> entry: map.entrySet()){
            InteractionCount i= new InteractionCount();
            i.setRgdId(entry.getKey());
            i.setCount(entry.getValue());
            int affectedRows = getDao().upsertInteractionCount(i);
            if( affectedRows<0 ) {
                insertOrUpdateCount[0]++; // insert
            } else if( affectedRows>0 ) {
                insertOrUpdateCount[1]++; // update
            } else {
                insertOrUpdateCount[2]++; // up-to-date
            }
        }
        return insertOrUpdateCount;
    }

    public Dao getDao() {
        return dao;
    }

    public void setDao(Dao dao) {
        this.dao = dao;
    }
}

