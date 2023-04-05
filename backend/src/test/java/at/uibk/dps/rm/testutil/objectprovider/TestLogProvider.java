package at.uibk.dps.rm.testutil.objectprovider;

import at.uibk.dps.rm.entity.model.Log;

public class TestLogProvider {

    public static Log createLog(long id) {
        Log log = new Log();
        log.setLogId(id);
        log.setLogValue("log");
        return log;
    }
}
