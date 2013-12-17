package ru.intertrust.cm.core.business.load;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import ru.intertrust.cm.core.business.load.ImportSystemData.FileGraf;
import ru.intertrust.cm.core.business.load.ImportSystemData.FileNode;

public class TestImportDataFileDependences {
    private Random rnd = new Random();
    
    @Test
    public void testImportCorrectFilesGraf() {
        ImportSystemData importSystemData = new ImportSystemData();
        FileGraf graf = importSystemData.new FileGraf();

        String[] testBaseNodes = new String[]{"1:5", "2:1,5", "3", "4", "5:3,4", "6:2,5"};        
        Reestr reestr = fillGraf(graf, testBaseNodes);
        
        graf.check();

        String correctOrder1 = "435126";
        String correctOrder2 = "345126";
        String orderedFiles = "";
        while (graf.hasMoreElements()) {
            FileNode file = graf.nextElement();
            orderedFiles += reestr.getAlias(file.getName());
        }
        Assert.assertTrue(
                orderedFiles.equals(correctOrder1) ||
                orderedFiles.equals(correctOrder2));                
    }

    @Test
    public void testImportCyclicFilesGraf() {
        ImportSystemData importSystemData = new ImportSystemData();
        FileGraf graf = importSystemData.new FileGraf();

        String[] testBaseNodes = new String[]{"1", "2:1", "3", "4:6", "5:3,4", "6:2,5"};        
        fillGraf(graf, testBaseNodes);
        try{
            graf.check();
        }catch(Exception ex){
            Assert.assertTrue(ex.getMessage().startsWith("Check dependenses"));
            return;
        }
        //Не должны доходить до этой строки
        Assert.assertTrue(false);
    }
    
    
    /**
     * Заполняет в случайном порядке
     * @param graf
     * @param nodeGroups
     */
    private Reestr fillGraf(FileGraf graf, String[]... nodeGroups) {
        Reestr reestr = new Reestr(); 
        
        List<String> items = new ArrayList<String>();
        for (String[] nodeGroup : nodeGroups) {
            for (String node : nodeGroup) {
                items.add(node);
            }
        }
        
        
        //Перемешиваем добавление
        Collections.shuffle(items);
        for (String node : items) {
            String[] nodeDesc = node.split(":");
            String[] nodeDep = null;
            if (nodeDesc.length > 1){
                nodeDep = nodeDesc[1].split(",");
                for (int i = 0; i < nodeDep.length; i++) {
                    nodeDep[i] = reestr.getName(nodeDep[i]);
                }
            }
            
            graf.addFileNode(reestr.getName(nodeDesc[0]) , nodeDep);
        }        
        return reestr;
    }
    
    private class Reestr{
        Hashtable<String, String> nameForAlias = new Hashtable<String, String>();
        Hashtable<String, String> aliasForName = new Hashtable<String, String>();
        
        private String getName(String alias){
            String result = nameForAlias.get(alias);
            if (result == null){
                result = generateTestName();
                nameForAlias.put(alias, result);
                aliasForName.put(result, alias);                
            }
            return result;
        }

        public String getAlias(String name) {
            return aliasForName.get(name);
        }

        private String generateTestName(){
            return String.valueOf(rnd.nextLong());
        }

    }
}
