package org.apollorm.function;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        List<String> splittedSeqs = new ArrayList<>();
        try {
            int i = 0;
            BufferedReader reader = Files.newBufferedReader(Path.of(Tempdir.getTempDir(), seq));
            while(writeToFile(i, reader)) {
                splittedSeqs.add("seq_" + i);
                i++;
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return splittedSeqs;
    }

    private boolean writeToFile(int i, BufferedReader input) throws IOException {
        int seqs = 0;
        int seqsWithin = 0;
        BufferedWriter writer = Files.newBufferedWriter(Path.of(Tempdir.getTempDir(), "seq_" + i));
        String line = input.readLine();
        while (line != null) {
            writer.write(line + "\n");
            if (line.startsWith("@")) {
                seqsWithin = 1;
            } else {
                seqsWithin++;
            }
            if (seqsWithin == 4) {
                seqs += 1;
                if (seqs >= seqsPerEntry) {
                    writer.close();
                    return true;
                }
            }
            line = input.readLine();
        }
        writer.close();
        return false;
    }
}
