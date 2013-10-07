/**
 * 
 */
package ru.intertrust.cm.performance.dataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import javax.xml.datatype.Duration;


/**
 * Утилитарный класс для генерации случайных величин. 
 * @author Олег Еренцов
 */
public class RandomGenerators {
    
    private static RandomGenerators instance = null;
    private ArrayList<String> vocabulary;
    private Random generator;
    
    private RandomGenerators() throws IOException{
        this.vocabulary = new ArrayList<String>(5000);
        this.generator = new Random();
        InputStream stream = getClass().getResourceAsStream("/vocabulary.txt");
        InputStreamReader reader = new InputStreamReader(stream);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String value = null;
        while((value = bufferedReader.readLine()) != null){
            vocabulary.add(value);
        }
    };
    
    public static RandomGenerators getInstance() throws IOException{
        if(instance == null) {
            instance = new RandomGenerators();           
        }
        return instance;
    }
    
    /**
     * Случайная величина с биномиальным распределением.
     * 
     * @param minValue
     * @param maxValue
     * @param p
     * @return
     */
    public int getBinomial(int minValue, int maxValue, double p){
        if(p <= 0) return minValue;
        if(p >= 1) return maxValue;
        int n = maxValue - minValue;
        int result = minValue;
        for(int i = 0; i < n; i++){
            if(Math.random() < p) result++;
        }
        return result;
    }
    
    /**
     * Случайная величина с биномиальным распределением и явно заданным математическим ожиданием.
     * @param minValue
     * @param maxValue
     * @param expectation
     * @return
     */
    public int getBinomialWithExpectation(int minValue, int maxValue, double expectation){
        return getBinomial(minValue, maxValue, expectation/(maxValue - minValue));
    }
    
    /**
     * Случайная величина с равномерным распределением.
     * @param minValue
     * @param maxValue
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getUniform(T minValue, T maxValue){
        Double m = Math.random();
        if(minValue instanceof Long){
            Long result = (long) ((Long) minValue + ((Long) maxValue - (Long) minValue) * m);
            return (T) result;
        } else if(minValue instanceof Integer){
            Integer result = (int) ((Integer) minValue + ((Integer) maxValue - (Integer) minValue) * m);
            return (T) result;
        }else if(minValue instanceof BigDecimal){
            BigDecimal result = ((BigDecimal) maxValue)
                    .subtract((BigDecimal) minValue)
                    .multiply(new BigDecimal(m))
                    .add((BigDecimal) minValue);
            return (T) result;
        } else if(minValue instanceof Date){
           Date result = new Date( ((Date) minValue).getTime() + (long) ((((Date) maxValue).getTime() - ((Date) minValue).getTime()) * m));
           return (T) result;
        } else if(minValue instanceof Duration){
            Duration result = ((Duration) maxValue).subtract((Duration) minValue).multiply(new BigDecimal(m)).add((Duration) minValue);
            return (T) result;
        }
        throw new RuntimeException("Unsupported type: " + minValue.getClass());
    } 
    
    /**
     * Случайный псевдотекст (генерируется по словарю).
     * @param minLength
     * @param maxLength
     * @return
     */
    public String getText(int minLength, int maxLength){
        StringBuffer result = new StringBuffer();
        while(result.length() < maxLength){
            if(result.length() > 0){
                result.append(" ");
            }
            result.append(getSentence());
        }
        result.setLength(getUniform(minLength, maxLength));
        return result.toString();
    }
    
    private String getSentence(){
        int numWords = getBinomialWithExpectation(1, 30, 10.38);
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < numWords; i++){
            String word = vocabulary.get(generator.nextInt(vocabulary.size()));
            if(i > 0){
                buffer.append(" ");
            } else buffer.append(capitalize(word));
            buffer.append(word);
        }
        buffer.append(".");
        return buffer.toString();
    }       
    
    private static String capitalize(String word) {
        char[] buffer = word.toCharArray();
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = Character.toTitleCase(buffer[i]);
        }
        return new String(buffer);
    }
    
}
