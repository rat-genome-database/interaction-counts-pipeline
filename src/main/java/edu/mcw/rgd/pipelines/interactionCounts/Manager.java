package edu.mcw.rgd.pipelines.interactionCounts;

import edu.mcw.rgd.process.CounterPool;
import edu.mcw.rgd.process.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    Logger log = LogManager.getLogger("status");

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

        CounterPool geneCounters = p.insertOrUpdate(geneMap);
        log.info("   gene-protein interactions"
                +"   up-to-date=" + Utils.formatThousands(geneCounters.get("up-to-date"))
                +"   updated=" + Utils.formatThousands(geneCounters.get("updated"))
                +"   inserted=" + Utils.formatThousands(geneCounters.get("inserted"))
                +"   with zero=" + Utils.formatThousands(geneCounters.get("zero")));

        CounterPool proteinCounters = p.insertOrUpdate(proteinMap);
        long time6 = System.currentTimeMillis();
        log.info(" protein-protein interactions"
                +"   up-to-date=" + Utils.formatThousands(proteinCounters.get("up-to-date"))
                +"   updated=" + Utils.formatThousands(proteinCounters.get("updated"))
                +"   inserted=" + Utils.formatThousands(proteinCounters.get("inserted"))
                +"   with zero=" + Utils.formatThousands(proteinCounters.get("zero")));

        log.info("=== LOAD OK ===   "+ Utils.formatElapsedTime(time0, time6));

        int deleteEntriesWithNoInteractions = dao.deleteEntriesWithNoInteractions();
        log.info(" deleted entries with no interactions: "+ deleteEntriesWithNoInteractions);

        log.info("=== OK ===   "+ Utils.formatElapsedTime(time0, System.currentTimeMillis()));
        log.info("");
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
