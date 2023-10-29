package at.uibk.dps.rm.util.validation;

import at.uibk.dps.rm.util.toscamapping.TOSCAFile;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import lombok.experimental.UtilityClass;

/**
 * A utility class that can be used to validate a TOSCA file
 *
 *
 */
@UtilityClass
public class ToscaFileValidator {

    /**
     * Check whether the TOSCA file contains has the right version and contains at least a single node
     *
     * @param toscaFile input file
     * @return a Completable
     */
    public static Completable checkToscaFile(TOSCAFile toscaFile) {
        return Maybe.just(toscaFile)
                .flatMapCompletable(file -> {
                    if(file.getTosca_definitions_version()==null || !file.getTosca_definitions_version().equals("tosca_simple_yaml_1_3")) {
                        return Completable.error(new Throwable("Unsupported TOSCA version"));
                    }
                    if(file.getTopology_template().getNode_templates().isEmpty()) {
                        return Completable.error(new Throwable("Empty TOSCA FILE"));
                    }
                            return Completable.complete();
                        }
                );
// TODO: more checks if format is valid, etc.
    }


}

