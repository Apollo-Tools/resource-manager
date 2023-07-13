def split(seqs_per_entry: int, seq: str, temp_path: str) -> list:
    splitted_seqs = []
    i = 0
    with open(seq, 'r') as input_file:
        while write_to_file(i, seqs_per_entry, input_file, temp_path):
            splitted_seqs.append(f'seq_{i}')
            i += 1
        splitted_seqs.append(f'seq_{i}')
    return splitted_seqs


def write_to_file(i: int, seqs_per_entry: int, input_file, temp_path: str) -> \
        bool:
    seqs = 0
    seqs_within = 0
    with open(f'{temp_path}/seq_{i}', 'w+') as output_file:
        while line := input_file.readline():
            output_file.write(line)
            if line.startswith('@'):
                seqs_within = 1
            else:
                seqs_within += 1

            if seqs_within == 4:
                seqs += 1
                if seqs >= seqs_per_entry:
                    return True
    return False
