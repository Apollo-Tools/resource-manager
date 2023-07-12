package org.apollorm.function;

import java.util.ArrayList;
import java.util.List;

public class FastqSplitter {

    private final int seqsPerEntry;

    private final String seq;

    public FastqSplitter(int seqsPerEntry, String seq) {
        this.seqsPerEntry = seqsPerEntry;
        this.seq = seq;
    }

    public List<String> split() {
        String[] lines = seq.split("\n");
        int seqsWithin = 0;
        int seqs = 0;
        List<String> splittedSeqs = new ArrayList<>();
        StringBuilder currSeq = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            currSeq.append(lines[i]).append("\n");
            if (lines[i].startsWith("@")) {
                seqsWithin = 1;
            } else {
                seqsWithin++;
            }
            if (seqsWithin == 4) {
                seqs += 1;
                if (seqs >= seqsPerEntry || i+1 >= lines.length) {
                    splittedSeqs.add(currSeq.toString());
                    currSeq = new StringBuilder();
                    seqs = 0;
                }
            }
        }
        return splittedSeqs;
    }
}
