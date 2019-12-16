package edu.mcw.rgd.pipelines.interactionCounts;

import edu.mcw.rgd.process.Utils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by jthota on 6/9/2016.
 */
public class Manager {
    private String version;
    Dao dao = new Dao();
    Logger log = Logger.getLogger("status");

    public static void main(String[] args) throws Exception {
        DefaultListableBeanFactory bf= new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(new FileSystemResource("properties/AppConfigure.xml"));
        Manager manager= (Manager) (bf.getBean("manager"));
        try{
            manager.run();
        }catch (Exception e){
            Utils.printStackTrace(e, manager.log);
            throw e;
        }
    }
	
    public void run() throws Exception{
        long time0 = System.currentTimeMillis();
        log.info(getVersion());

        log.info("    "+dao.getConnectionInfo());

        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("    started at "+sdt.format(new Date()));

        Process p = new Process();
        p.setDao(dao);

        Collection<Integer> geneRgdIds = dao.getActiveGeneRgdIds();
        log.info("Total Active Genes: " + Utils.formatThousands(geneRgdIds.size()));

        Collection<Integer> proteinRgdIds = dao.getActiveProteinRgdIds();
        log.info("Total Active Proteins: " + Utils.formatThousands(proteinRgdIds.size()));

        // Map of Gene RgdId and its count
        Map<Integer, Integer> geneMap = p.getInteractionCountsOfGenes(geneRgdIds);
        log.debug("geneMap Size: " + Utils.formatThousands(geneMap.size()));

        // Map of Protein RgdId and its count
        Map<Integer, Integer> proteinMap = p.getInteractionCountsOfProteins(proteinRgdIds);
        log.debug("proteinMap Size: " + Utils.formatThousands(proteinMap.size()));

        int[] insertedGeneCount=p.insertOrUpdate(geneMap);
        log.info("   gene-protein interactions"
                +"   up-to-date=" + Utils.formatThousands(insertedGeneCount[2])
                +"   updated=" + Utils.formatThousands(insertedGeneCount[1])
                +"   inserted=" + Utils.formatThousands(insertedGeneCount[0]));

        int[] insertedProteinCount=p.insertOrUpdate(proteinMap);
        long time6 = System.currentTimeMillis();
        log.info(" protein-protein interactions"
                +"   up-to-date=" + Utils.formatThousands(insertedProteinCount[2])
                +"   updated=" + Utils.formatThousands(insertedProteinCount[1])
                +"   inserted=" + Utils.formatThousands(insertedProteinCount[0]));

        log.info("=== OK ===   "+ Utils.formatElapsedTime(time0, time6));
        log.info("");
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
