package at.uibk.dps.rm.handler.util;

import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.util.FilePathService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class FileSystemChecker {

    private final FilePathService filePathService;


    public FileSystemChecker(FilePathService filePathService) {
        this.filePathService = filePathService;
    }

    public Completable checkTemplatePathExists(String templatePath) {
        Single<Boolean> existsTemplatePath = filePathService.templatePathExists(templatePath);
        return ErrorHandler.handleExistsOne(existsTemplatePath).ignoreElement();
    }

    public Single<String> checkGetFileTemplate(String templatePath) {
        Single<String> getFileTemplate = filePathService.getRuntimeTemplate(templatePath);
        return ErrorHandler.handleTemplateHasContent(getFileTemplate);
    }
}
