"""
This function expects a FASTQ sequence and the size of the subsequences and
then returns the subsequences of the original sequence.
"""
__author__ = "matthi-g"

import time
if __package__ is None or __package__ == '':
    from fastqSplit import split
else:
    from .fastqSplit import split


def main(json_input):
    
    start = time.time()
    
    # Prepare input
    seqs_per_entry = json_input["seqsPerEntry"]
    seq = json_input["seq"]

    # Calculate input size
    input_size_mb = len(seq.encode('utf-8')) / 1000000
    
    # Actual execution
    splitted_seqs = split(seqs_per_entry, seq)
        
    # Prepare output
    result = {
        "input_size_mb": input_size_mb
    }
    for i, split_seq in enumerate(splitted_seqs):
        result["splitted_" + str(i)] = split_seq    
    end = time.time()
    result["actual_ms"] = (end-start)*1000
    
    return result


# For local development
if __name__ == "__main__":
    sequence = "@ILMN-GA001_3_205WWAAXX_TAQ1:1:1:80:433\n" \
               "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC\n" \
               "+ILMN-GA001_3_205WWAAXX_TAQ1:1:1:80:433\n" \
               "hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh\n" \
               "@ILMN-GA001_3_205WWAAXX_TAQ1:1:1:112:249\n" \
               "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCA\n" \
               "+ILMN-GA001_3_205WWAAXX_TAQ1:1:1:112:249\n" \
               "hhhhhhhhhhhhhhhhhhhhhhhhhMNRCD>K\n" \
               "@ILMN-GA001_3_205WWAAXX_TAQ1:1:1:49:484\n" \
               "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC\n" \
               "+ILMN-GA001_3_205WWAAXX_TAQ1:1:1:49:484\n" \
               "hhhhhhhhhhhhhhhhhhhhhhhhh^h_hhhhh\n" \
               "@ILMN-GA001_3_205WWAAXX_TAQ1:1:1:226:355\n" \
               "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC\n" \
               "+ILMN-GA001_3_205WWAAXX_TAQ1:1:1:226:355\n" \
               "hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhLh\n"
    response = main({"seqsPerEntry": 4, "seq": sequence})
    print(response)
