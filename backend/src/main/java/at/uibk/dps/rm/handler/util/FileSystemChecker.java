package at.uibk.dps.rm.handler.util;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.util.FilePathService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * Implements utility methods to perform operations on the vertx file_system.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class FileSystemChecker {

    private final FilePathService filePathService;


    /**
     * Create an instance from the filePathService
     *
     * @param filePathService the file path service
     */
    public FileSystemChecker(FilePathService filePathService) {
        this.filePathService = filePathService;
    }

    /**
     * Check whether a template path exists or not.
     *
     * @param templatePath the template path
     * @return a Completable
     */
    public Completable checkTemplatePathExists(String templatePath) {
        Single<Boolean> existsTemplatePath = filePathService.templatePathExists(templatePath);
        return ErrorHandler.handleExistsOne(existsTemplatePath).ignoreElement();
    }

    /**
     * Get a file template by its template path
     *
     * @param templatePath the template path
     * @return a Single that emits the string content of the found file template
     */
    public Single<String> checkGetFileTemplate(String templatePath) {
        Single<String> getFileTemplate = filePathService.getRuntimeTemplate(templatePath);
        return ErrorHandler.handleTemplateHasContent(getFileTemplate);
    }
}
