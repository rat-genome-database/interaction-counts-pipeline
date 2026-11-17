# interaction-counts-pipeline

Computes interaction counts for genes and proteins and stores them in the INTERACTION_COUNTS table. Only non-zero interaction counts are stored.

## What it does

1. Loads all active genes and proteins, all interactions, and the protein-to-gene associations from RGD.
2. In a single pass over the interactions, counts for each gene the interactions involving any protein of that gene
   (an interaction counts once per gene, even when both of its proteins belong to that gene).
3. In the same way, counts for each protein the interactions it takes part in (a self-interaction counts once).
4. Inserts new counts or updates existing ones in the INTERACTION_COUNTS table.
5. Deletes entries whose interaction count has dropped to zero, or whose gene or protein is no longer active.

The run report is emailed by `run.sh`: "OK" with the summary log, or "FAILED" with `run.log` when the pipeline fails.

## Output

- `interaction_counts` table rows with: RGD ID and interaction count
- Pipeline logs with statistics: records up-to-date, updated, inserted, and zero-count deletions
