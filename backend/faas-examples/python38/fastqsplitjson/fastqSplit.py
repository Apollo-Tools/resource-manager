def split(seqs_per_entry: int, seq: str):
    seq_lines = seq.rstrip().split('\n')
    seqs_within = 0
    seqs = 0
    splitted_seqs = []
    curr_seq = ''
    for i, line in enumerate(seq_lines):
        curr_seq += line + '\n'
        if line.startswith('@'):
            seqs_within = 1
        else:
            seqs_within += 1

        if seqs_within == 4:
            seqs += 1
            if seqs >= seqs_per_entry or i + 1 >= len(seq_lines):
                splitted_seqs.append(curr_seq)
                curr_seq = ''
                seqs = 0
    return splitted_seqs
