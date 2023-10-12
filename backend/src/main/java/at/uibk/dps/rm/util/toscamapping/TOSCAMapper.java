package at.uibk.dps.rm.util.toscamapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class TOSCAMapper {

    public static TOSCAFile readTOSCA(String toscaString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        TOSCAFile toscaFile = objectMapper.readValue(toscaString,TOSCAFile.class);
        return toscaFile;
    }
}



