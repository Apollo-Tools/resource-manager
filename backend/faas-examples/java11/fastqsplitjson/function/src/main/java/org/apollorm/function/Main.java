package org.apollorm.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apollorm.model.FunctionHandler;
import org.apollorm.model.exception.FunctionException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This function expects a FASTQ sequence and the size of the subsequences and then returns
 * the subsequences of the original sequence.
 *
 * @author matthi-g
 */
public class Main implements FunctionHandler {

    public String main(String requestBody) throws JsonProcessingException {
        long start = System.currentTimeMillis();
        // Parse input
        ObjectMapper objectMapper = new ObjectMapper();
        Input input;
        try {
            input = objectMapper.readValue(requestBody, Input.class);
        } catch (Exception ex) {
            throw new FunctionException("bad input");
        }
        // Processing
        int inputSize = input.getSeq().getBytes(StandardCharsets.UTF_8).length;
        FastqSplitter splitter = new FastqSplitter(input.getSeqsPerEntry(), input.getSeq().strip());
        List<String> splittedSeq = splitter.split();

        // Return the result
        Map<String, String> result = new HashMap<>();
        result.put("input_size_mb", String.valueOf(inputSize  / 1000000.0));
        for (int i=0; i<splittedSeq.size(); i++) {
            result.put("splitted_" + i, splittedSeq.get(i));
        }
        long end = System.currentTimeMillis();
        result.put("actual_ms", String.valueOf((end - start) / 1000.0));
        return objectMapper.writeValueAsString(result);
    }

    /**
     * This is the entrypoint for local development of the function.
     *
     * @param args command line arguments
     * @throws JsonProcessingException when an error occurs during serialization or deserialization
     */    public static void main(String[] args) throws JsonProcessingException {
        FunctionHandler fh = new Main();
        String sequence = "@ILMN-GA001_3_205WWAAXX_TAQ1:1:1:80:433\\n" +
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC\\n" +
            "+ILMN-GA001_3_205WWAAXX_TAQ1:1:1:80:433\\n" +
            "hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh\\n" +
            "@ILMN-GA001_3_205WWAAXX_TAQ1:1:1:112:249\\n" +
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCA\\n" +
            "+ILMN-GA001_3_205WWAAXX_TAQ1:1:1:112:249\\n" +
            "hhhhhhhhhhhhhhhhhhhhhhhhhMNRCD>K\\n" +
            "@ILMN-GA001_3_205WWAAXX_TAQ1:1:1:49:484\\n" +
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC\\n" +
            "+ILMN-GA001_3_205WWAAXX_TAQ1:1:1:49:484\\n" +
            "hhhhhhhhhhhhhhhhhhhhhhhhh^h_hhhhh\\n" +
            "@ILMN-GA001_3_205WWAAXX_TAQ1:1:1:226:355\\n" +
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC\\n" +
            "+ILMN-GA001_3_205WWAAXX_TAQ1:1:1:226:355\\n" +
            "hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhLh\\n";
        String result = fh.main("{\"seqsPerEntry\": 5, \"seq\": \"" + sequence + "\"}");
        System.out.println(result);
    }
}
