package edu.mcw.rgd.pipelines.interactionCounts;

import edu.mcw.rgd.process.Utils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;

import java.util.*;

/**
 * Created by jthota on 6/9/2016.
 */
public class Manager {
    private String version;
    Dao dao = new Dao();

    public static void main(String[] args) throws Exception {
        DefaultListableBeanFactory bf= new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(new FileSystemResource("properties/AppConfigure.xml"));
        Manager manager= (Manager) (bf.getBean("manager"));
        System.out.println(manager.getVersion());
        try{
            manager.run();
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }
	
    public void run() throws Exception{
        long time0 = System.currentTimeMillis();
        Process p = new Process();
        p.setDao(dao);

        Collection<Integer> geneRgdIds = dao.getActiveGeneRgdIds();
        long time1 = System.currentTimeMillis();
        System.out.println("Total Active Genes: " + geneRgdIds.size()+"      elapsed "+Utils.formatElapsedTime(time0, time1));

        Collection<Integer> proteinRgdIds = dao.getActiveProteinRgdIds();
        long time2 = System.currentTimeMillis();
        System.out.println("Total Active Proteins: " + proteinRgdIds.size()+"   elapsed "+Utils.formatElapsedTime(time1, time2));

        // Map of Gene RgdId and its count
        Map<Integer, Integer> geneMap = p.getInteractionCountsOfGenes(geneRgdIds);
        long time3 = System.currentTimeMillis();
        System.out.println("geneMap Size: " + geneMap.size()+"   elapsed "+Utils.formatElapsedTime(time2, time3));

        // Map of Protein RgdId and its count
        Map<Integer, Integer> proteinMap = p.getInteractionCountsOfProteins(proteinRgdIds);
        long time4 = System.currentTimeMillis();
        System.out.println("proteinMap Size: " + proteinMap.size()+"   elapsed "+Utils.formatElapsedTime(time3, time4));

        int[] insertedGeneCount=p.insertOrUpdate(geneMap);
        long time5 = System.currentTimeMillis();
        System.out.println("   gene-protein interactions"
                +"  up-to-date=" + insertedGeneCount[2]
                +"  updated=" + insertedGeneCount[1]
                +"  inserted=" + insertedGeneCount[0]
                +"  elapsed "+Utils.formatElapsedTime(time4, time5));

        int[] insertedProteinCount=p.insertOrUpdate(proteinMap);
        long time6 = System.currentTimeMillis();
        System.out.println(" protein-protein interactions"
                +"  up-to-date=" + insertedProteinCount[2]
                +"  updated=" + insertedProteinCount[1]
                +"  inserted=" + insertedProteinCount[0]
                +"  elapsed "+Utils.formatElapsedTime(time5, time6));

        System.out.println("=== OK === "+ Utils.formatElapsedTime(time0, time6));
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
