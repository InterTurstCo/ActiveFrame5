package ru.intertrust.cm.core.dao.impl.filenet;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestFileNet {

    String SECURITY_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    String PASSWORD_TYPE =
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText";
    String AUTH_PREFIX = "wss";

    public static void main(String[] args) {
        try {
            TestFileNet testFileNet = new TestFileNet();
            testFileNet.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private byte[] createLageContent(int size) throws Exception{
        String file = "test.pdf";
        byte[] fileContent = readFile(file);
        ByteArrayOutputStream saveStream = new ByteArrayOutputStream();
        while(saveStream.size() < size * 1024 * 1024){
            saveStream.write(fileContent);
        }
        return saveStream.toByteArray();
    }

    private void testTime(FileNetAdapter adapter) throws Exception {

        byte[] fileContent = createLageContent(20);

        ByteArrayOutputStream saveStream = new ByteArrayOutputStream();
        saveStream.write(fileContent, 0, 100 * 1024);
        byte[] saveContent = saveStream.toByteArray();
        int iteration = 1;
        int size = 100 * 1024;
        while (fileContent.length > size) {
            saveStream.reset();
            saveStream.write(fileContent, 0, size);
            saveContent = saveStream.toByteArray();
            
            long start = System.currentTimeMillis();
            String path = adapter.save(saveContent);
            long save = System.currentTimeMillis() - start;
            start = System.currentTimeMillis();

            InputStream in = adapter.load(path);
            byte[] loadContent = readStream(in);
            long load = System.currentTimeMillis() - start;
            start = System.currentTimeMillis();

            adapter.delete(path);
            long delete = System.currentTimeMillis() - start;
            
            System.out.println("Size=" + saveContent.length + "\tSave=" + save + "\tLoad=" + load + "\tDelete=" + delete);
            
            boolean compareRes = compareContent(saveContent, loadContent);
            if (!compareRes){
                throw new Exception("Error compare");
            }
            
            
            //Увеличиваем размер контента логарифмически
            int pow = (int) (Math.pow(2, iteration));
            size = 100 * 1024 * pow;
            iteration++;
        }
    }    
    
    private void execute() throws Exception {
        FileNetAdapter adapter = new FileNetAdapterWS("vm-fn-01:9443", "p8admin", "Welcome777", "OS", "/CM5");
        byte[] saveContent = createLageContent(1);
        String path = adapter.save(saveContent);
        System.out.println("Save OK " + saveContent.length);
        InputStream in = adapter.load(path);
        byte[] loadContent = readStream(in);
        boolean comareResult = compareContent(saveContent, loadContent);
        System.out.println("Load " + comareResult + " " + loadContent.length);
        adapter.delete(path);

        try {
            adapter.load(path);
        } catch (Exception ex) {
            System.out.println("Delete OK");
        }
        
        
        testTime(adapter);
    }

    private boolean compareContent(byte[] saveContent, byte[] loadContent) {
        if (saveContent.length != loadContent.length) {
            return false;
        }
        for (int i = 0; i < loadContent.length; i++) {
            if (saveContent[i] != loadContent[i]) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Получение файла в виде массива байт
     * @param file
     * @return
     * @throws IOException
     */
    protected byte[] readFile(String path) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream input = null;
        try {
            input = new FileInputStream(path);
            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = input.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } finally {
            input.close();
        }
    }

    protected byte[] readStream(InputStream inStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = inStream.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } finally {
            out.close();
        }
    }
}
