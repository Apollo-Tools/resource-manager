package org.apollorm.function;

public class Input {

    private String bucket;

    private int seqsPerFile;

    private String seqs;

    public int getSeqsPerFile() {
        return seqsPerFile;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public void setSeqsPerFile(int seqsPerFile) {
        this.seqsPerFile = seqsPerFile;
    }

    public String getSeqs() {
        return seqs;
    }

    public void setSeqs(String seqs) {
        this.seqs = seqs;
    }
}
