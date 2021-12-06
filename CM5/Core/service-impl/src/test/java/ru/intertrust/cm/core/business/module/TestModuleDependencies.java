package ru.intertrust.cm.core.business.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import ru.intertrust.cm.core.config.impl.ModuleServiceImpl;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.model.FatalException;

public class TestModuleDependencies {
    private final Random rnd = new Random();
    
    @Test
    public void testModuleOrder() {
        ModuleServiceImpl moduleService = new ModuleServiceImpl();
        ModuleServiceImpl.ModuleGraph graf = moduleService.new ModuleGraph();

        String[] testBaseNodes = new String[]{"1:5", "2:1,5", "3", "4", "5:3,4", "6:2,5"};        
        Reestr reestr = fillGraph(graf, testBaseNodes);
        
        graf.check();

        String correctOrder1 = "435126";
        String correctOrder2 = "345126";
        StringBuilder orderedFiles = new StringBuilder();
        while (graf.hasMoreElements()) {
            ModuleServiceImpl.ModuleNode file = graf.nextElement();
            orderedFiles.append(reestr.getAlias(file.getName()));
        }
        Assert.assertTrue(
                orderedFiles.toString().equals(correctOrder1) ||
                orderedFiles.toString().equals(correctOrder2));
    }

    @Test (expected = FatalException.class)
    public void testCyclicModules() {
        ModuleServiceImpl moduleService = new ModuleServiceImpl();
        ModuleServiceImpl.ModuleGraph graph = moduleService.new ModuleGraph();

        String[] testBaseNodes = new String[] {"1", "2:1", "3", "4:6", "5:3,4", "6:2,5"};
        fillGraph(graph, testBaseNodes);
        try {
            graph.check();
        } catch(FatalException ex) {
            Assert.assertTrue(ex.getMessage().startsWith("Check dependencies"));
            throw ex;
        }
    }
    
    
    /**
     * Заполняет в случайном порядке
     * @param graph
     * @param nodeGroups
     */
    private Reestr fillGraph(ModuleServiceImpl.ModuleGraph graph, String[]... nodeGroups) {
        Reestr reestr = new Reestr(); 
        
        List<String> items = new ArrayList<>();
        for (String[] nodeGroup : nodeGroups) {
            Collections.addAll(items, nodeGroup);
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
            
            ModuleConfiguration conf = new ModuleConfiguration();
            conf.setName(reestr.getName(nodeDesc[0]));
            graph.addModuleConfiguration(conf , nodeDep);
        }        
        return reestr;
    }
    
    private class Reestr{
        Hashtable<String, String> nameForAlias = new Hashtable<>();
        Hashtable<String, String> aliasForName = new Hashtable<>();
        
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
